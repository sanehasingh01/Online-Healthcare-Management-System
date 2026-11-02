import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.DBConnection;

/** Returns the public doctor names needed by the appointment picker. */
@WebServlet("/doctors")
public class DoctorListServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(DoctorListServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        String sql = "SELECT d.doctor_id, u.name, d.specialization "
                   + "FROM doctors d JOIN users u ON u.id = d.doctor_id "
                   + "ORDER BY u.name";

        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "Doctor list is temporarily unavailable.");
                return;
            }
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet results = statement.executeQuery()) {
                StringBuilder json = new StringBuilder("[");
                boolean first = true;
                while (results.next()) {
                    if (!first) json.append(',');
                    first = false;
                    json.append("{\"id\":").append(results.getInt("doctor_id"))
                        .append(",\"name\":\"").append(escape(results.getString("name")))
                        .append("\",\"specialization\":\"")
                        .append(escape(results.getString("specialization"))).append("\"}");
                }
                response.getWriter().write(json.append(']').toString());
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to load doctor list", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not load doctors.");
        }
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                    .replace("\r", "\\r").replace("\n", "\\n");
    }
}
