package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/healthcare";

    private static final String USER =
            "root";

    private static final String PASSWORD =
            "YOUR_PASSWORD";

    // Private constructor prevents object creation
    private DBConnection() {
    }

    
    public static Connection getConnection()
            throws SQLException {

        return DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
        );
    }
}
