package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // MySQL database details
    private static final String URL =
            "jdbc:mysql://localhost:3306/healthcare_db";

    private static final String USER = "root";

    // MySQL password
    private static final String PASSWORD = "YOUR_MYSQL_PASSWORD";

    // Method to connect Java with MySQL
    public static Connection getConnection() {

        try {
            Connection connection = DriverManager.getConnection(
                    URL,
                    USER,
                    PASSWORD
            );

            System.out.println("Database connected successfully!");

            return connection;

        } catch (SQLException e) {

            System.out.println("Database connection failed!");
            e.printStackTrace();

            return null;
        }
    }

    // Test the database connection
    public static void main(String[] args) {

        Connection connection = getConnection();

        if (connection != null) {
            System.out.println("Healthcare database is ready!");
        } else {
            System.out.println("Could not connect to healthcare database.");
        }
    }
}