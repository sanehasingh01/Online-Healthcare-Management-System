import java.io.IOException;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO dao = new UserDAO();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || email.trim().isEmpty()
                || password == null || password.isEmpty()) {

            response.sendRedirect("login.html?error=missing");
            return;
        }

        email = email.trim();

        boolean success = dao.validateLogin(email, password);

        if (success) {

            String role = dao.getUserRole(email);

            if (role == null) {
                response.sendRedirect("login.html?error=InvalidLogin");
                return;
            }

            switch (role.toLowerCase()) {
                case "admin":
                    response.sendRedirect("admin-dashboard.html");
                    break;

                case "doctor":
                    response.sendRedirect("doctor-dashboard.html");
                    break;

                case "patient":
                    response.sendRedirect("patient-dashboard.html");
                    break;

                default:
                    response.sendRedirect("login.html?error=InvalidRole");
                    break;
            }

        } else {
            response.sendRedirect("login.html?error=InvalidLogin");
        }
    }
}