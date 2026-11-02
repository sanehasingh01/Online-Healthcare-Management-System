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

/** Admin-only overview for system records and appointment status management. */
@WebServlet("/admin")
public class AdminServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(AdminServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String contextPath = request.getContextPath();
        if (!isAdmin(session)) {
            response.sendRedirect(contextPath + "/login.html?error=loginrequired");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");
        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "Admin overview is temporarily unavailable.");
                return;
            }
            writePage(request, response, connection);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to load admin overview", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not load the system overview.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String contextPath = request.getContextPath();
        if (!isAdmin(session)) {
            response.sendRedirect(contextPath + "/login.html?error=loginrequired");
            return;
        }
        int appointmentId;
        String status = request.getParameter("status");
        try {
            appointmentId = Integer.parseInt(request.getParameter("appointmentId"));
        } catch (RuntimeException e) {
            response.sendRedirect(contextPath + "/admin?error=invalid#appointments");
            return;
        }
        if (appointmentId < 1 || !isAllowedStatus(status)) {
            response.sendRedirect(contextPath + "/admin?error=invalid#appointments");
            return;
        }

        String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";
        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                response.sendRedirect(contextPath + "/admin?error=unavailable#appointments");
                return;
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, status);
                statement.setInt(2, appointmentId);
                response.sendRedirect(contextPath + "/admin?updated="
                        + (statement.executeUpdate() > 0 ? "success" : "unchanged")
                        + "#appointments");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to update appointment status", e);
            response.sendRedirect(contextPath + "/admin?error=failed#appointments");
        }
    }

    private void writePage(HttpServletRequest request, HttpServletResponse response,
                           Connection connection) throws SQLException, IOException {
        String contextPath = request.getContextPath();
        var out = response.getWriter();
        out.write("""
                <!doctype html><html lang="en"><head><meta charset="utf-8">
                <meta name="viewport" content="width=device-width,initial-scale=1">
                <title>Admin Overview | MedCare</title><link rel="stylesheet" href="style.css">
                <style>
                body{margin:0;background:#f4f9f8;color:#17364a;font:15px Arial,sans-serif}header{padding:18px 6%;background:#fff;display:flex;justify-content:space-between;align-items:center}header a{color:#087f7a;text-decoration:none;font-weight:700;margin-left:16px}main{width:min(1200px,94%);margin:38px auto}h1{font-size:34px;margin:0 0 8px}.muted{color:#6b8190;line-height:1.5}.stats{display:grid;grid-template-columns:repeat(auto-fit,minmax(150px,1fr));gap:13px;margin:24px 0 34px}.stat,.panel{background:white;border:1px solid #dce9e8;border-radius:14px;padding:18px}.stat b{display:block;font-size:28px;color:#087f7a;margin-top:8px}.panel{margin:20px 0;overflow:hidden}.panel h2{margin:0 0 15px;font-size:21px}nav.sections{display:flex;gap:10px;flex-wrap:wrap;margin:22px 0}nav.sections a{color:#087f7a;text-decoration:none;font-weight:700}.scroll{overflow:auto}table{width:100%;border-collapse:collapse;min-width:680px}th,td{text-align:left;padding:11px 10px;border-bottom:1px solid #e8efef;vertical-align:top}th{font-size:13px;color:#607783;background:#f8fbfb}.status-form{display:flex;gap:7px;align-items:center}.status-form select,.status-form button{padding:7px;border:1px solid #d4e2e2;border-radius:7px}.status-form button{background:#078b83;color:#fff;border:0;font-weight:700;cursor:pointer}.notice{padding:12px 14px;border-radius:9px;margin:14px 0;background:#e5f5ef;color:#176448}.notice.error{background:#fff0f0;color:#8d2930}.back{display:inline-block;margin-top:10px;color:#087f7a;text-decoration:none;font-weight:700}
                @media(max-width:560px){header{align-items:flex-start;flex-direction:column;gap:12px}header a{margin:0 12px 0 0}h1{font-size:29px}main{margin:28px auto}.panel{padding:14px}}
                </style></head><body>
                """);
        out.write("<header><strong>✚ MedCare Admin</strong><nav><a href=\"");
        out.write(escape(contextPath + "/admin-dashboard.html"));
        out.write("\">Dashboard</a><a href=\"");
        out.write(escape(contextPath + "/logout"));
        out.write("\">Log out</a></nav></header><main><h1>System overview</h1>");
        out.write("<p class=\"muted\">Live records from the healthcare database.</p>");

        String updated = request.getParameter("updated");
        if ("success".equals(updated)) out.write("<p class=\"notice\">Appointment status updated.</p>");
        else if ("unchanged".equals(updated)) out.write("<p class=\"notice\">No appointment was changed.</p>");
        if (request.getParameter("error") != null) out.write("<p class=\"notice error\">The requested update could not be completed.</p>");

        out.write("<section class=\"stats\" id=\"analytics\">");
        stat(out, "Users", count(connection, "SELECT COUNT(*) FROM users"));
        stat(out, "Doctors", count(connection, "SELECT COUNT(*) FROM doctors"));
        stat(out, "Patients", count(connection, "SELECT COUNT(*) FROM patients"));
        stat(out, "Appointments", count(connection, "SELECT COUNT(*) FROM appointments"));
        out.write("</section><nav class=\"sections\"><a href=\"#users\">Users</a><a href=\"#doctors\">Doctors</a><a href=\"#patients\">Patients</a><a href=\"#appointments\">Appointments</a></nav>");

        panelStart(out, "users", "Registered users");
        tableStart(out, "ID", "Name", "Email", "Role");
        renderQuery(connection, out, "SELECT id, name, email, role FROM users ORDER BY id DESC", "id", "name", "email", "role");
        panelEnd(out);

        panelStart(out, "doctors", "Doctors");
        tableStart(out, "ID", "Name", "Email", "Specialization", "Schedule");
        renderQuery(connection, out, "SELECT d.doctor_id, u.name, u.email, d.specialization, d.schedule FROM doctors d JOIN users u ON u.id=d.doctor_id ORDER BY u.name", "doctor_id", "name", "email", "specialization", "schedule");
        panelEnd(out);

        panelStart(out, "patients", "Patients");
        tableStart(out, "ID", "Name", "Email");
        renderQuery(connection, out, "SELECT p.patient_id, u.name, u.email FROM patients p JOIN users u ON u.id=p.patient_id ORDER BY u.name", "patient_id", "name", "email");
        panelEnd(out);

        panelStart(out, "appointments", "Appointments");
        tableStart(out, "ID", "Patient", "Doctor", "Date", "Time", "Status", "Update");
        String appointmentSql = "SELECT a.appointment_id, p.name AS patient_name, d.name AS doctor_name, "
                + "a.appointment_date, a.appointment_time, a.status FROM appointments a "
                + "JOIN users p ON p.id=a.patient_id JOIN users d ON d.id=a.doctor_id "
                + "ORDER BY a.appointment_date DESC, a.appointment_time DESC";
        renderAppointments(connection, out, contextPath, appointmentSql);
        panelEnd(out);

        out.write("<a class=\"back\" href=\"");
        out.write(escape(contextPath + "/admin-dashboard.html"));
        out.write("\">Back to dashboard</a></main></body></html>");
    }

    private long count(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            return result.next() ? result.getLong(1) : 0;
        }
    }

    private void stat(java.io.PrintWriter out, String label, long value) {
        out.write("<div class=\"stat\">");
        out.write(escape(label));
        out.write("<b>");
        out.write(Long.toString(value));
        out.write("</b></div>");
    }

    private void panelStart(java.io.PrintWriter out, String id, String title) {
        out.write("<section class=\"panel\" id=\"");
        out.write(escape(id));
        out.write("\"><h2>");
        out.write(escape(title));
        out.write("</h2><div class=\"scroll\">");
    }

    private void panelEnd(java.io.PrintWriter out) { out.write("</tbody></table></div></section>"); }

    private void tableStart(java.io.PrintWriter out, String... headings) {
        out.write("<table><thead><tr>");
        for (String heading : headings) {
            out.write("<th>"); out.write(escape(heading)); out.write("</th>");
        }
        out.write("</tr></thead><tbody>");
    }

    private void renderQuery(Connection connection, java.io.PrintWriter out, String sql,
                              String... columns) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            boolean found = false;
            while (rows.next()) {
                found = true;
                out.write("<tr>");
                for (String column : columns) {
                    out.write("<td>"); out.write(escape(rows.getString(column))); out.write("</td>");
                }
                out.write("</tr>");
            }
            if (!found) out.write("<tr><td colspan=\"" + columns.length + "\">No records.</td></tr>");
        }
    }

    private void renderAppointments(Connection connection, java.io.PrintWriter out,
                                    String contextPath, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            boolean found = false;
            while (rows.next()) {
                found = true;
                int id = rows.getInt("appointment_id");
                out.write("<tr><td>" + id + "</td><td>");
                out.write(escape(rows.getString("patient_name")));
                out.write("</td><td>"); out.write(escape(rows.getString("doctor_name")));
                out.write("</td><td>"); out.write(escape(rows.getString("appointment_date")));
                out.write("</td><td>"); out.write(escape(rows.getString("appointment_time")));
                out.write("</td><td>"); out.write(escape(rows.getString("status")));
                out.write("</td><td><form class=\"status-form\" method=\"post\" action=\"");
                out.write(escape(contextPath + "/admin"));
                out.write("\"><input type=\"hidden\" name=\"appointmentId\" value=\"" + id + "\"><select name=\"status\">");
                for (String status : new String[]{"Pending", "Confirmed", "Completed", "Cancelled"}) {
                    out.write("<option value=\""); out.write(status); out.write("\"");
                    if (status.equals(rows.getString("status"))) out.write(" selected");
                    out.write(">"); out.write(status); out.write("</option>");
                }
                out.write("</select><button type=\"submit\">Save</button></form></td></tr>");
            }
            if (!found) out.write("<tr><td colspan=\"7\">No appointments.</td></tr>");
        }
    }

    private boolean isAdmin(HttpSession session) {
        return session != null && "admin".equals(session.getAttribute("userRole"));
    }

    private boolean isAllowedStatus(String status) {
        return "Pending".equals(status) || "Confirmed".equals(status)
                || "Completed".equals(status) || "Cancelled".equals(status);
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;")
                    .replace(">", "&gt;").replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}
