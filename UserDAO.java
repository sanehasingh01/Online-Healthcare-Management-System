package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import util.DBConnection;

public class UserDAO {

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
                System.out.println("Database connection failed.");
                return false;
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, name);
                statement.setString(2, email);
                statement.setString(3, password);
                statement.setString(4, role);

                return statement.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.out.println("Unable to register user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks whether the login credentials are valid.
     */
    public boolean validateLogin(String email, String password) {

        String sql = "SELECT id FROM users WHERE email = ? AND password = ?";

        try (Connection connection = DBConnection.getConnection()) {

            if (connection == null) {
                System.out.println("Database connection failed.");
                return false;
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, email);
                statement.setString(2, password);

                try (ResultSet result = statement.executeQuery()) {
                    return result.next();
                }
            }

        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
            return false;
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
                return false;
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, email);

                try (ResultSet result = statement.executeQuery()) {
                    return result.next();
                }
            }

        } catch (SQLException e) {
            System.out.println("Email check error: " + e.getMessage());
            return false;
        }
    }
}