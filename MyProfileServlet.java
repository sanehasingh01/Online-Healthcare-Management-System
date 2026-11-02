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

/** Shows the signed-in user's profile without exposing other accounts. */
@WebServlet("/my-profile")
public class MyProfileServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(MyProfileServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String contextPath = request.getContextPath();
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("userId") instanceof Integer)) {
            response.sendRedirect(contextPath + "/login.html?error=loginrequired");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String role = String.valueOf(session.getAttribute("userRole"));
        String value;
        String sql;
        if ("patient".equals(role)) {
            value = request.getParameter("medicalHistory");
            if (value == null) value = "";
            sql = "UPDATE patients SET medical_history = ? WHERE patient_id = ?";
        } else if ("doctor".equals(role)) {
            value = request.getParameter("schedule");
            if (value == null || value.isBlank() || value.length() > 100) {
                response.sendRedirect(contextPath + "/my-profile?error=schedule");
                return;
            }
            sql = "UPDATE doctors SET schedule = ? WHERE doctor_id = ?";
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                response.sendRedirect(contextPath + "/my-profile?error=unavailable");
                return;
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, value.trim());
                statement.setInt(2, userId);
                response.sendRedirect(contextPath + "/my-profile?updated="
                        + (statement.executeUpdate() > 0 ? "success" : "failed"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to update signed-in user's profile", e);
            response.sendRedirect(contextPath + "/my-profile?error=failed");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String contextPath = request.getContextPath();
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("userId") instanceof Integer)) {
            response.sendRedirect(contextPath + "/login.html?error=loginrequired");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String sql = "SELECT u.id, u.name, u.email, u.role, p.medical_history, "
                   + "d.specialization, d.schedule "
                   + "FROM users u LEFT JOIN patients p ON p.patient_id = u.id "
                   + "LEFT JOIN doctors d ON d.doctor_id = u.id WHERE u.id = ?";

        response.setContentType("text/html;charset=UTF-8");
        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "Profile is temporarily unavailable.");
                return;
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, userId);
                try (ResultSet user = statement.executeQuery()) {
                    if (!user.next()) {
                        response.sendRedirect(contextPath + "/login.html?error=loginrequired");
                        return;
                    }
                    writePage(request, response, contextPath, user);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to load signed-in user profile", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not load profile.");
        }
    }

    private void writePage(HttpServletRequest request, HttpServletResponse response,
                           String contextPath, ResultSet user)
            throws SQLException, IOException {
        var out = response.getWriter();
        out.write("""
                <!doctype html><html lang="en"><head>
                <meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
                <title>My Profile | MedCare</title><link rel="stylesheet" href="style.css">
                <style>
                  body{margin:0;background:#f4f9f8;color:#17364a;font:16px Arial,sans-serif}
                  header{padding:18px 6%;background:white;display:flex;justify-content:space-between;align-items:center}
                  header a{color:#087f7a;text-decoration:none;font-weight:700;margin-left:18px}
                  main{width:min(780px,90%);margin:54px auto}h1{font-size:36px;margin-bottom:8px}
                  .muted{color:#6b8190;line-height:1.6}.profile{margin-top:26px;padding:28px;background:white;border:1px solid #dce9e8;border-radius:18px;box-shadow:0 8px 28px #17364a0b}
                  .avatar{display:grid;place-items:center;width:64px;height:64px;border-radius:20px;background:#e5f5f1;color:#066b67;font-size:28px;font-weight:bold;margin-bottom:18px}
                  dl{display:grid;grid-template-columns:180px 1fr;gap:0;margin:18px 0 0}dt,dd{padding:14px 0;border-bottom:1px solid #e8efef;margin:0;overflow-wrap:anywhere}dt{color:#6b8190}dd{font-weight:600}
                  .btn{display:inline-block;margin-top:22px;padding:12px 17px;border-radius:10px;background:#078b83;color:white;text-decoration:none;font-weight:700}.edit{margin-top:24px;padding-top:20px;border-top:1px solid #e8efef}.edit label{display:block;font-weight:700;margin-bottom:8px}.edit textarea,.edit input{width:100%;padding:12px;border:1px solid #d5e3e2;border-radius:9px;font:inherit;color:#17364a}.edit textarea{min-height:110px;resize:vertical}.edit button{margin-top:12px;padding:11px 16px;border:0;border-radius:9px;background:#078b83;color:#fff;font-weight:700;cursor:pointer}.notice{padding:12px 14px;background:#e5f5ef;color:#176448;border-radius:9px;margin:18px 0}
                  @media(max-width:560px){header{align-items:flex-start;gap:10px;flex-direction:column}header a{margin:0 14px 0 0}main{margin:34px auto}h1{font-size:30px}.profile{padding:20px}dl{grid-template-columns:1fr}dt{padding-bottom:4px;border:0}dd{padding-top:0}}
                </style></head><body>
                """);
        String role = user.getString("role");
        out.write("<header><strong>✚ MedCare</strong><nav><a href=\"");
        out.write(escape(dashboardPath(contextPath, role)));
        out.write("\">Dashboard</a>");
        if ("patient".equalsIgnoreCase(role)) {
            out.write("<a href=\""); out.write(escape(contextPath + "/my-appointments")); out.write("\">My Appointments</a>");
        } else if ("doctor".equalsIgnoreCase(role)) {
            out.write("<a href=\""); out.write(escape(contextPath + "/doctor-appointments")); out.write("\">Appointments</a>");
        } else if ("admin".equalsIgnoreCase(role)) {
            out.write("<a href=\""); out.write(escape(contextPath + "/admin")); out.write("\">System Overview</a>");
        }
        out.write("<a href=\"");
        out.write(escape(contextPath + "/logout"));
        out.write("\">Log out</a></nav></header><main><h1>My Profile</h1>");
        out.write("<p class=\"muted\">Your account details.</p><section class=\"profile\"><div class=\"avatar\" aria-hidden=\"true\">✚</div><dl>");
        field(out, "Account ID", Integer.toString(user.getInt("id")));
        field(out, "Name", user.getString("name"));
        field(out, "Email", user.getString("email"));
        field(out, "Account type", user.getString("role"));
        if ("patient".equalsIgnoreCase(user.getString("role"))) {
            field(out, "Medical history", user.getString("medical_history"));
        } else if ("doctor".equalsIgnoreCase(user.getString("role"))) {
            field(out, "Specialization", user.getString("specialization"));
            field(out, "Schedule", user.getString("schedule"));
        }
        out.write("</dl>");
        if ("success".equals(request.getParameter("updated"))) {
            out.write("<p class=\"notice\">Your changes were saved.</p>");
        }
        if (request.getParameter("error") != null) {
            out.write("<p class=\"notice\">Could not save changes. Please check the value and try again.</p>");
        }
        out.write("<form class=\"edit\" method=\"post\" action=\"");
        out.write(escape(contextPath + "/my-profile"));
        out.write("\">");
        if ("patient".equalsIgnoreCase(user.getString("role"))) {
            out.write("<label for=\"medicalHistory\">Medical history</label><textarea id=\"medicalHistory\" name=\"medicalHistory\" maxlength=\"10000\" aria-describedby=\"historyHelp\">");
            out.write(escape(user.getString("medical_history")));
            out.write("</textarea><p id=\"historyHelp\" class=\"muted\">Only you can view and update this information.</p><button type=\"submit\">Save medical history</button>");
        } else if ("doctor".equalsIgnoreCase(user.getString("role"))) {
            out.write("<label for=\"schedule\">Available schedule</label><input id=\"schedule\" name=\"schedule\" maxlength=\"100\" required value=\"");
            out.write(escape(user.getString("schedule")));
            out.write("\"><button type=\"submit\">Save schedule</button>");
        }
        out.write("</form><a class=\"btn\" href=\"");
        out.write(escape(dashboardPath(contextPath, String.valueOf(user.getString("role")))));
        out.write("\">Back to dashboard</a></section></main></body></html>");
    }

    private String dashboardPath(String contextPath, String role) {
        String page = switch (role == null ? "" : role.toLowerCase()) {
            case "doctor" -> "/doctor-dashboard.html";
            case "admin" -> "/admin-dashboard.html";
            default -> "/patient-dashboard.html";
        };
        return contextPath + page;
    }

    private void field(java.io.PrintWriter out, String label, String value) {
        out.write("<dt>");
        out.write(escape(label));
        out.write("</dt><dd>");
        String safeValue = value == null || value.isBlank() ? "Not provided" : value;
        out.write(escape(safeValue));
        out.write("</dd>");
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;")
                    .replace(">", "&gt;").replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}
