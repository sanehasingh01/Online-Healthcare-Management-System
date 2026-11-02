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

/** Lists and updates appointments assigned to the signed-in doctor only. */
@WebServlet("/doctor-appointments")
public class DoctorAppointmentsServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(DoctorAppointmentsServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String contextPath = request.getContextPath();
        if (!isDoctor(session)) {
            response.sendRedirect(contextPath + "/login.html?error=loginrequired");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");
        int doctorId = (Integer) session.getAttribute("userId");
        String sql = "SELECT a.appointment_id, a.appointment_date, a.appointment_time, a.status, "
                   + "u.name AS patient_name, a.patient_id "
                   + "FROM appointments a JOIN users u ON u.id = a.patient_id "
                   + "WHERE a.doctor_id = ? "
                   + "ORDER BY a.appointment_date, a.appointment_time";

        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "Appointments are temporarily unavailable.");
                return;
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, doctorId);
                try (ResultSet results = statement.executeQuery()) {
                    writePage(request, response, results);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to load doctor's appointments", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not load appointments.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String contextPath = request.getContextPath();
        if (!isDoctor(session)) {
            response.sendRedirect(contextPath + "/login.html?error=loginrequired");
            return;
        }
        int doctorId = (Integer) session.getAttribute("userId");
        String status = request.getParameter("status");
        int appointmentId;
        try {
            appointmentId = Integer.parseInt(request.getParameter("appointmentId"));
        } catch (RuntimeException e) {
            response.sendRedirect(contextPath + "/doctor-appointments?error=invalid");
            return;
        }

        if (appointmentId < 1 || !"Confirmed".equals(status)
                && !"Cancelled".equals(status) && !"Completed".equals(status)) {
            response.sendRedirect(contextPath + "/doctor-appointments?error=invalid");
            return;
        }

        String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ? "
                   + "AND doctor_id = ? AND ((status = 'Pending' AND ? IN ('Confirmed','Cancelled')) "
                   + "OR (status = 'Confirmed' AND ? IN ('Completed','Cancelled')))";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection == null ? null : connection.prepareStatement(sql)) {
            if (statement == null) {
                response.sendRedirect(contextPath + "/doctor-appointments?error=unavailable");
                return;
            }
            statement.setString(1, status);
            statement.setInt(2, appointmentId);
            statement.setInt(3, doctorId);
            statement.setString(4, status);
            statement.setString(5, status);
            response.sendRedirect(contextPath + "/doctor-appointments?updated="
                    + (statement.executeUpdate() > 0 ? "success" : "unchanged"));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to update doctor's appointment", e);
            response.sendRedirect(contextPath + "/doctor-appointments?error=failed");
        }
    }

    private boolean isDoctor(HttpSession session) {
        return session != null && "doctor".equals(session.getAttribute("userRole"))
                && session.getAttribute("userId") instanceof Integer;
    }

    private void writePage(HttpServletRequest request, HttpServletResponse response,
                           ResultSet results) throws SQLException, IOException {
        String contextPath = request.getContextPath();
        var out = response.getWriter();
        out.write("""
                <!doctype html><html lang="en"><head><meta charset="utf-8">
                <meta name="viewport" content="width=device-width,initial-scale=1">
                <title>Doctor Appointments | MedCare</title><link rel="stylesheet" href="style.css">
                <style>
                body{margin:0;background:#f4f9f8;color:#17364a;font:16px Arial,sans-serif}header{padding:18px 6%;background:#fff;display:flex;justify-content:space-between;align-items:center}header a{color:#087f7a;text-decoration:none;font-weight:700;margin-left:18px}main{width:min(1040px,92%);margin:42px auto}h1{font-size:34px;margin:0 0 8px}.muted{color:#6b8190;line-height:1.5}.notice{padding:12px 15px;border-radius:10px;background:#e5f5ef;color:#176448;margin:18px 0}.list{display:grid;gap:14px;margin-top:24px}.item{padding:20px;background:#fff;border:1px solid #dce9e8;border-radius:15px}.top{display:flex;justify-content:space-between;gap:14px}.status{font-weight:700;color:#066b67}.details{display:flex;gap:18px;flex-wrap:wrap;margin:12px 0;color:#516a77}.actions{display:flex;gap:8px;flex-wrap:wrap}.actions button{border:0;border-radius:8px;background:#078b83;color:white;padding:9px 12px;font-weight:700;cursor:pointer}.actions button.cancel{background:#a54048}.empty{margin-top:24px;background:white;padding:24px;border-radius:14px}
                @media(max-width:560px){header{align-items:flex-start;flex-direction:column;gap:12px}header a{margin:0 12px 0 0}h1{font-size:29px}.top{flex-direction:column}.item{padding:16px}}
                </style></head><body>
                """);
        out.write("<header><strong>✚ MedCare</strong><nav><a href=\"");
        out.write(escape(contextPath + "/doctor-dashboard.html"));
        out.write("\">Dashboard</a><a href=\"");
        out.write(escape(contextPath + "/my-profile"));
        out.write("\">My profile</a><a href=\"");
        out.write(escape(contextPath + "/logout"));
        out.write("\">Log out</a></nav></header><main id=\"patients\"><h1>My appointments</h1>");
        out.write("<p class=\"muted\">Review patient requests and update their status.</p>");
        String updated = request.getParameter("updated");
        if ("success".equals(updated)) out.write("<p class=\"notice\">Appointment status updated.</p>");
        else if ("unchanged".equals(updated)) out.write("<p class=\"notice\">That appointment is already updated or is not assigned to your account.</p>");
        if ("invalid".equals(request.getParameter("error"))) out.write("<p class=\"notice\">Choose a valid appointment action.</p>");

        out.write("<section class=\"list\" aria-label=\"Assigned appointments\">");
        boolean found = false;
        while (results.next()) {
            found = true;
            String status = results.getString("status");
            out.write("<article class=\"item\"><div class=\"top\"><div><strong>");
            out.write(escape(results.getString("patient_name")));
            out.write("</strong><p class=\"muted\">Patient ID: ");
            out.write(Integer.toString(results.getInt("patient_id")));
            out.write("</p></div><span class=\"status\">");
            out.write(escape(status));
            out.write("</span></div><div class=\"details\"><span>Date: ");
            out.write(escape(results.getString("appointment_date")));
            out.write("</span><span>Time: ");
            out.write(escape(results.getString("appointment_time")));
            out.write("</span></div>");
            if ("Pending".equalsIgnoreCase(status) || "Confirmed".equalsIgnoreCase(status)) {
                out.write("<div class=\"actions\">");
                if ("Pending".equalsIgnoreCase(status)) action(out, contextPath, results.getInt("appointment_id"), "Confirmed", "Confirm", false);
                if ("Confirmed".equalsIgnoreCase(status)) action(out, contextPath, results.getInt("appointment_id"), "Completed", "Mark complete", false);
                action(out, contextPath, results.getInt("appointment_id"), "Cancelled", "Cancel", true);
                out.write("</div>");
            }
            out.write("</article>");
        }
        out.write("</section>");
        if (!found) out.write("<div class=\"empty\">No appointments have been assigned to you yet.</div>");
        out.write("</main></body></html>");
    }

    private void action(java.io.PrintWriter out, String contextPath, int id,
                        String status, String label, boolean cancel) {
        out.write("<form method=\"post\" action=\"");
        out.write(escape(contextPath + "/doctor-appointments"));
        out.write("\"><input type=\"hidden\" name=\"appointmentId\" value=\"");
        out.write(Integer.toString(id));
        out.write("\"><input type=\"hidden\" name=\"status\" value=\"");
        out.write(escape(status));
        out.write("\"><button class=\"");
        if (cancel) out.write("cancel");
        out.write("\" type=\"submit\">");
        out.write(escape(label));
        out.write("</button></form>");
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;")
                    .replace(">", "&gt;").replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}
