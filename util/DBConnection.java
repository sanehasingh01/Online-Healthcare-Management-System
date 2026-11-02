package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnection {

private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());

private static final String URL = valueOrDefault(
        "HC_DB_URL", "jdbc:mysql://localhost:3306/healthcare_db");
private static final String USER = System.getenv("HC_DB_USER");
private static final String PASSWORD = System.getenv("HC_DB_PASSWORD");

private static String valueOrDefault(String key, String defaultValue) {
    String value = System.getenv(key);
    return value == null || value.isBlank() ? defaultValue : value;
}

// Method to connect Java with MySQL
public static Connection getConnection() {

    try {

        if (USER == null || USER.isBlank()
                || PASSWORD == null || PASSWORD.isBlank()) {
            LOGGER.severe("Database credentials are missing. Set HC_DB_USER and HC_DB_PASSWORD before starting Tomcat.");
            return null;
        }

        // Explicitly register the driver for Tomcat's web-app classloader.
        Class.forName("com.mysql.cj.jdbc.Driver");

        Connection connection = DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
        );

        return connection;

    } catch (ClassNotFoundException | SQLException e) {

        LOGGER.log(Level.SEVERE, "Database connection failed", e);

        return null;
    }
}

// Test database connection
public static void main(String[] args) {

    Connection connection = getConnection();

    if (connection != null) {
        System.out.println("Healthcare database is ready!");
    } else {
        System.out.println("Could not connect to healthcare database.");
    }
}


}
