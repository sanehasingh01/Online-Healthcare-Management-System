package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import util.DBConnection;
import util.PasswordUtil;

public class UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    /**
     * Registers a new user in the database.
     */
    public boolean addUser(
            String name,
            String email,
            String password,
            String role) {

        String sql = "INSERT INTO users (name, email, password, role) "
                   + "VALUES (?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection()) {

            if (connection == null) {
                throw new IllegalStateException("Database connection is unavailable.");
            }

            connection.setAutoCommit(false);
            try (PreparedStatement statement =
                         connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                statement.setString(1, name);
                statement.setString(2, email);
                statement.setString(3, PasswordUtil.hash(password));
                statement.setString(4, role);

                if (statement.executeUpdate() == 0) {
                    connection.rollback();
                    return false;
                }
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        connection.rollback();
                        return false;
                    }
                    createRoleProfile(connection, keys.getInt(1), role);
                }
                connection.commit();
                return true;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to register user", e);
            throw new IllegalStateException("Unable to register user.", e);
        }
    }

    /** Creates the row required by role-specific foreign keys. */
    private void createRoleProfile(Connection connection, int userId, String role)
            throws SQLException {
        if ("patient".equalsIgnoreCase(role)) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO patients (patient_id, medical_history) VALUES (?, '')")) {
                statement.setInt(1, userId);
                statement.executeUpdate();
            }
        } else if ("doctor".equalsIgnoreCase(role)) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO doctors (doctor_id, specialization, schedule) VALUES (?, ?, ?)")) {
                statement.setInt(1, userId);
                statement.setString(2, "General Medicine");
                statement.setString(3, "Mon-Fri 09:00-17:00");
                statement.executeUpdate();
            }
        }
    }

    /** Returns the account ID after ensuring legacy accounts have a role profile. */
    public int getUserIdAndEnsureProfile(String email, String role) {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) return -1;
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, email);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) return -1;
                    int userId = result.getInt("id");
                    if ("patient".equalsIgnoreCase(role)) {
                        try (PreparedStatement profile = connection.prepareStatement(
                                "INSERT IGNORE INTO patients (patient_id, medical_history) VALUES (?, '')")) {
                            profile.setInt(1, userId);
                            profile.executeUpdate();
                        }
                    } else if ("doctor".equalsIgnoreCase(role)) {
                        try (PreparedStatement profile = connection.prepareStatement(
                                "INSERT IGNORE INTO doctors (doctor_id, specialization, schedule) VALUES (?, ?, ?)")) {
                            profile.setInt(1, userId);
                            profile.setString(2, "General Medicine");
                            profile.setString(3, "Mon-Fri 09:00-17:00");
                            profile.executeUpdate();
                        }
                    }
                    connection.commit();
                    return userId;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to load user profile", e);
            return -1;
        }
    }

    /**
     * Checks whether the login credentials are valid.
     */
    public boolean validateLogin(String email, String password) {

        String sql = "SELECT id, password FROM users WHERE email = ?";

        try (Connection connection = DBConnection.getConnection()) {

            if (connection == null) {
                System.out.println("Database connection failed.");
                return false;
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, email);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()
                            || !PasswordUtil.verify(password, result.getString("password"))) {
                        return false;
                    }
                    String storedPassword = result.getString("password");
                    if (!PasswordUtil.isHashed(storedPassword)) {
                        updatePasswordHash(connection, result.getInt("id"), password);
                    }
                    return true;
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Login query failed", e);
            return false;
        }
    }

    private void updatePasswordHash(Connection connection, int userId, String password)
            throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, PasswordUtil.hash(password));
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }

    /**
     * Retrieves a user's role using their email.
     */
    public String getUserRole(String email) {

        String sql = "SELECT role FROM users WHERE email = ?";

        try (Connection connection = DBConnection.getConnection()) {

            if (connection == null) {
                return null;
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, email);

                try (ResultSet result = statement.executeQuery()) {

                    if (result.next()) {
                        return result.getString("role");
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Unable to retrieve user role: "
                               + e.getMessage());
        }

        return null;
    }

    /**
     * Checks whether an email is already registered.
     */
    public boolean emailExists(String email) {

        String sql = "SELECT id FROM users WHERE email = ?";

        try (Connection connection = DBConnection.getConnection()) {

            if (connection == null) {
                throw new IllegalStateException("Database connection is unavailable.");
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, email);

                try (ResultSet result = statement.executeQuery()) {
                    return result.next();
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to check whether email exists", e);
            throw new IllegalStateException("Unable to check whether email exists.", e);
        }
    }
}
