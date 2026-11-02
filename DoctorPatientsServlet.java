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
import jakarta.servlet.http.HttpSession;
import util.DBConnection;

/** Shows patient records only when the signed-in doctor has an appointment with them. */
@WebServlet("/doctor-patients")
public class DoctorPatientsServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(DoctorPatientsServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String contextPath = request.getContextPath();
        if (session == null || !"doctor".equals(session.getAttribute("userRole"))
                || !(session.getAttribute("userId") instanceof Integer)) {
            response.sendRedirect(contextPath + "/login.html?error=loginrequired");
            return;
        }

        String sql = "SELECT p.patient_id, u.name, p.medical_history, "
                   + "MAX(a.appointment_date) AS last_appointment "
                   + "FROM appointments a JOIN patients p ON p.patient_id = a.patient_id "
                   + "JOIN users u ON u.id = p.patient_id "
                   + "WHERE a.doctor_id = ? AND a.status <> 'Cancelled' "
                   + "GROUP BY p.patient_id, u.name, p.medical_history ORDER BY u.name";
        response.setContentType("text/html;charset=UTF-8");
        int doctorId = (Integer) session.getAttribute("userId");
        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "Patient records are temporarily unavailable.");
                return;
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, doctorId);
                try (ResultSet patients = statement.executeQuery()) {
                    writePage(request, response, patients);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to load doctor's patient list", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not load patient records.");
        }
    }

    private void writePage(HttpServletRequest request, HttpServletResponse response,
                           ResultSet patients) throws SQLException, IOException {
        String contextPath = request.getContextPath();
        var out = response.getWriter();
        out.write("""
                <!doctype html><html lang="en"><head><meta charset="utf-8">
                <meta name="viewport" content="width=device-width,initial-scale=1">
                <title>My Patients | MedCare</title><link rel="stylesheet" href="style.css">
                <style>
                  body{margin:0;background:#f4f9f8;color:#17364a;font:16px Arial,sans-serif}
                  header{padding:18px 6%;background:white;display:flex;justify-content:space-between;align-items:center}
                  header a{color:#087f7a;text-decoration:none;font-weight:700;margin-left:18px}
                  main{width:min(980px,92%);margin:42px auto}h1{font-size:34px;margin:0 0 8px}
                  .muted{color:#6b8190;line-height:1.5}.list{display:grid;gap:15px;margin-top:24px}
                  .patient{padding:22px;background:white;border:1px solid #dce9e8;border-radius:15px;box-shadow:0 8px 28px #17364a0b}
                  .patient-head{display:flex;justify-content:space-between;gap:16px;align-items:flex-start}.patient h2{margin:0 0 7px;font-size:20px}
                  .id{color:#6b8190;font-size:14px}.last{color:#516a77;font-size:14px}.history{margin-top:16px;border-top:1px solid #e8efef;padding-top:13px}
                  summary{cursor:pointer;color:#087f7a;font-weight:700}.history-text{white-space:pre-wrap;line-height:1.65;color:#405966}
                  .empty{padding:24px;background:white;border-radius:15px;color:#6b8190;margin-top:22px}
                  @media(max-width:560px){header{align-items:flex-start;gap:12px;flex-direction:column}header a{margin:0 14px 0 0}main{margin:30px auto}h1{font-size:29px}.patient{padding:17px}.patient-head{flex-direction:column}}
                </style></head><body>
                """);
        out.write("<header><strong>✚ MedCare</strong><nav><a href=\"");
        out.write(escape(contextPath + "/doctor-dashboard.html"));
        out.write("\">Dashboard</a><a href=\"");
        out.write(escape(contextPath + "/doctor-appointments"));
        out.write("\">Appointments</a><a href=\"");
        out.write(escape(contextPath + "/my-profile"));
        out.write("\">My Profile</a><a href=\"");
        out.write(escape(contextPath + "/logout"));
        out.write("\">Log out</a></nav></header><main><h1>My Patients</h1>");
        out.write("<p class=\"muted\">Patients who have an appointment with you.</p><section class=\"list\" aria-label=\"Patients assigned to you\">");

        boolean found = false;
        while (patients.next()) {
            found = true;
            out.write("<article class=\"patient\"><div class=\"patient-head\"><div><h2>");
            out.write(escape(patients.getString("name")));
            out.write("</h2><span class=\"id\">Patient ID: ");
            out.write(Integer.toString(patients.getInt("patient_id")));
            out.write("</span></div><span class=\"last\">Last appointment: ");
            out.write(escape(patients.getString("last_appointment")));
            out.write("</span></div><details class=\"history\"><summary>Medical history</summary><p class=\"history-text\">");
            String history = patients.getString("medical_history");
            out.write(history == null || history.isBlank() ? "No medical history provided." : escape(history));
            out.write("</p></details></article>");
        }
        out.write("</section>");
        if (!found) out.write("<div class=\"empty\">No patients with appointments are currently assigned to you.</div>");
        out.write("</main></body></html>");
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;")
                    .replace(">", "&gt;").replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}
