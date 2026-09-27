package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import util.DBConnection;

/**
 * UserDAO
 * --------------------------------
 * Handles all database operations
 * related to users.
 */
public class UserDAO {

    /**
     * Adds a new user to the database.
     */
    public boolean addUser(
            String name,
            String email,
            String password,
            String role) {

        String sql = """
                INSERT INTO users
                (name, email, password, role)
                VALUES (?, ?, ?, ?)
                """;

        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, password);
            statement.setString(4, role);

            int rowsInserted =
                    statement.executeUpdate();

            return rowsInserted > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Unable to add user: "
                    + e.getMessage()
            );

            return false;
        }
    }


    /**
     * Validates user login credentials.
     */
    public boolean validateLogin(
            String email,
            String password) {

        String sql = """
                SELECT id
                FROM users
                WHERE email = ?
                AND password = ?
                """;

        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setString(1, email);
            statement.setString(2, password);

            try (ResultSet result =
                         statement.executeQuery()) {

                return result.next();
            }

        } catch (SQLException e) {

            System.out.println(
                    "Login error: "
                    + e.getMessage()
            );

            return false;
        }
    }
}