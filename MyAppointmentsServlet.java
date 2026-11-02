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

/** Displays only appointments belonging to the signed-in patient. */
@WebServlet("/my-appointments")
public class MyAppointmentsServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(MyAppointmentsServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String contextPath = request.getContextPath();
        HttpSession session = request.getSession(false);
        if (session == null || !"patient".equals(session.getAttribute("userRole"))
                || !(session.getAttribute("userId") instanceof Integer)) {
            response.sendRedirect(contextPath + "/login.html?error=loginrequired");
            return;
        }
        int patientId = (Integer) session.getAttribute("userId");
        int appointmentId;
        try {
            appointmentId = Integer.parseInt(request.getParameter("appointmentId"));
        } catch (RuntimeException e) {
            response.sendRedirect(contextPath + "/my-appointments?error=invalid");
            return;
        }
        String sql = "UPDATE appointments SET status = 'Cancelled' "
                   + "WHERE appointment_id = ? AND patient_id = ? AND status = 'Pending'";
        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                response.sendRedirect(contextPath + "/my-appointments?error=unavailable");
                return;
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, appointmentId);
                statement.setInt(2, patientId);
                response.sendRedirect(contextPath + "/my-appointments?cancelled="
                        + (statement.executeUpdate() > 0 ? "success" : "unchanged"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to cancel patient appointment", e);
            response.sendRedirect(contextPath + "/my-appointments?error=failed");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String contextPath = request.getContextPath();
        HttpSession session = request.getSession(false);
        if (session == null || !"patient".equals(session.getAttribute("userRole"))
                || !(session.getAttribute("userId") instanceof Integer)) {
            response.sendRedirect(contextPath + "/login.html?error=loginrequired");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");
        int patientId = (Integer) session.getAttribute("userId");
        String sql = "SELECT a.appointment_id, a.appointment_date, a.appointment_time, a.status, "
                   + "u.name AS doctor_name, d.specialization "
                   + "FROM appointments a "
                   + "JOIN doctors d ON d.doctor_id = a.doctor_id "
                   + "JOIN users u ON u.id = d.doctor_id "
                   + "WHERE a.patient_id = ? "
                   + "ORDER BY a.appointment_date DESC, a.appointment_time DESC";

        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "Appointment records are temporarily unavailable.");
                return;
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, patientId);
                try (ResultSet appointments = statement.executeQuery()) {
                    writePage(request, response, appointments);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to load patient appointments", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not load appointments.");
        }
    }

    private void writePage(HttpServletRequest request, HttpServletResponse response,
                           ResultSet appointments) throws SQLException, IOException {
        String contextPath = request.getContextPath();
        var out = response.getWriter();
        out.write("""
                <!doctype html><html lang="en"><head>
                <meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
                <title>My Appointments | MedCare</title><link rel="stylesheet" href="style.css">
                <style>
                  body{margin:0;background:#f4f9f8;color:#17364a;font:16px Arial,sans-serif}
                  header{padding:18px 6%;background:white;display:flex;justify-content:space-between;align-items:center}
                  header a{color:#087f7a;text-decoration:none;font-weight:700;margin-left:18px}
                  main{width:min(900px,90%);margin:54px auto}h1{font-size:36px;margin-bottom:8px}
                  .muted{color:#6b8190;line-height:1.6}.notice{padding:14px 16px;background:#e5f5ef;color:#176448;border-radius:10px;margin:20px 0}
                  .list{display:grid;gap:14px;margin-top:26px}.item{background:#fff;border:1px solid #dce9e8;border-radius:16px;padding:20px 22px;box-shadow:0 8px 28px #17364a0b}
                  .row{display:flex;justify-content:space-between;gap:12px;align-items:flex-start}.item h2{margin:0 0 8px;font-size:20px}
                  .meta{display:flex;flex-wrap:wrap;gap:10px 20px;color:#516a77;margin-top:14px}.status{background:#e8f6f3;color:#066b67;padding:7px 10px;border-radius:999px;font-size:13px;font-weight:700}
                  .empty{background:white;border:1px solid #dce9e8;border-radius:16px;padding:26px;margin-top:24px}
                  .btn{display:inline-block;margin-top:20px;padding:12px 17px;border-radius:10px;background:#078b83;color:white;text-decoration:none;font-weight:700}
                  .cancel-form{margin-top:16px}.cancel-form button{padding:9px 13px;border:0;border-radius:8px;background:#a54048;color:white;font-weight:700;cursor:pointer}
                  @media(max-width:560px){header{align-items:flex-start;gap:10px;flex-direction:column}header a{margin:0 14px 0 0}main{margin:34px auto}h1{font-size:30px}.item{padding:17px}}
                </style></head><body>
                """);
        out.write("<header><strong>✚ MedCare</strong><nav><a href=\"");
        out.write(escape(contextPath + "/patient-dashboard.html"));
        out.write("\">Dashboard</a><a href=\"");
        out.write(escape(contextPath + "/appointment.html"));
        out.write("\">Book appointment</a><a href=\"");
        out.write(escape(contextPath + "/logout"));
        out.write("\">Log out</a></nav></header><main><h1>My appointments</h1>");
        out.write("<p class=\"muted\">Your scheduled and requested visits are listed here.</p>");

        if ("success".equals(request.getParameter("booked"))) {
            out.write("<p class=\"notice\" role=\"status\">Your appointment request was saved.</p>");
        }
        if ("success".equals(request.getParameter("cancelled"))) {
            out.write("<p class=\"notice\" role=\"status\">Appointment cancelled.</p>");
        } else if ("unchanged".equals(request.getParameter("cancelled"))) {
            out.write("<p class=\"notice\">Only pending appointments can be cancelled here.</p>");
        }

        out.write("<section class=\"list\" aria-label=\"Appointment list\">");
        boolean found = false;
        while (appointments.next()) {
            found = true;
            out.write("<article class=\"item\"><div class=\"row\"><div><h2>Dr. ");
            out.write(escape(appointments.getString("doctor_name")));
            out.write("</h2><p class=\"muted\">");
            out.write(escape(appointments.getString("specialization")));
            out.write("</p></div><span class=\"status\">");
            out.write(escape(appointments.getString("status")));
            out.write("</span></div><div class=\"meta\"><span>Date: ");
            out.write(escape(appointments.getString("appointment_date")));
            out.write("</span><span>Time: ");
            out.write(escape(appointments.getString("appointment_time")));
            out.write("</span></div>");
            if ("Pending".equalsIgnoreCase(appointments.getString("status"))) {
                out.write("<form class=\"cancel-form\" method=\"post\" action=\"");
                out.write(escape(contextPath + "/my-appointments"));
                out.write("\"><input type=\"hidden\" name=\"appointmentId\" value=\"");
                out.write(Integer.toString(appointments.getInt("appointment_id")));
                out.write("\"><button type=\"submit\">Cancel request</button></form>");
            }
            out.write("</article>");
        }
        out.write("</section>");
        if (!found) {
            out.write("<div class=\"empty\"><p>You do not have any appointments yet.</p>");
            out.write("<a class=\"btn\" href=\"");
            out.write(escape(contextPath + "/appointment.html"));
            out.write("\">Book an appointment</a></div>");
        }
        out.write("</main></body></html>");
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;")
                    .replace(">", "&gt;").replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}
