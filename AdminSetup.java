import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import util.DBConnection;
import util.PasswordUtil;

/** One-time console utility for creating an administrator without public signup. */
public final class AdminSetup {
    private AdminSetup() {}

    public static void main(String[] args) {
        String name = System.getenv("HC_ADMIN_NAME");
        String email = System.getenv("HC_ADMIN_EMAIL");
        String password = System.getenv("HC_ADMIN_PASSWORD");
        if (blank(name) || blank(email) || blank(password)) {
            System.err.println("Set HC_ADMIN_NAME, HC_ADMIN_EMAIL, and HC_ADMIN_PASSWORD before running AdminSetup.");
            System.exit(2);
        }

        String find = "SELECT id FROM users WHERE email = ?";
        String insert = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, 'admin')";
        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                System.err.println("Could not connect to healthcare_db.");
                System.exit(1);
            }
            try (PreparedStatement statement = connection.prepareStatement(find)) {
                statement.setString(1, email.trim());
                try (ResultSet results = statement.executeQuery()) {
                    if (results.next()) {
                        System.out.println("That email is already registered. No account was changed.");
                        return;
                    }
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(insert)) {
                statement.setString(1, name.trim());
                statement.setString(2, email.trim());
                statement.setString(3, PasswordUtil.hash(password));
                statement.executeUpdate();
            }
            System.out.println("Administrator account created. Remove HC_ADMIN_PASSWORD from the environment after setup.");
        } catch (Exception e) {
            System.err.println("Administrator setup failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
