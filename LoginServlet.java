import java.io.IOException;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO dao = new UserDAO();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String contextPath = request.getContextPath();

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || email.trim().isEmpty()
                || password == null || password.isEmpty()) {

            response.sendRedirect(contextPath + "/login.html?error=missing");
            return;
        }

        email = email.trim();

        boolean success = dao.validateLogin(email, password);

        if (success) {

            String role = dao.getUserRole(email);

            if (role == null) {
                response.sendRedirect(contextPath + "/login.html?error=invalidlogin");
                return;
            }

            int userId = dao.getUserIdAndEnsureProfile(email, role);
            if (userId < 1) {
                response.sendRedirect(contextPath + "/login.html?error=invalidlogin");
                return;
            }

            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) oldSession.invalidate();
            HttpSession session = request.getSession(true);
            session.setAttribute("userId", userId);
            session.setAttribute("userRole", role.toLowerCase());
            session.setAttribute("userEmail", email);

            switch (role.toLowerCase()) {
                case "admin":
                    response.sendRedirect(contextPath + "/admin");
                    break;

                case "doctor":
                    response.sendRedirect(contextPath + "/doctor-dashboard.html");
                    break;

                case "patient":
                    response.sendRedirect(contextPath + "/patient-dashboard.html");
                    break;

                default:
                    response.sendRedirect(contextPath + "/login.html?error=invalidrole");
                    break;
            }

        } else {
            response.sendRedirect(contextPath + "/login.html?error=invalidlogin");
        }
    }
}
