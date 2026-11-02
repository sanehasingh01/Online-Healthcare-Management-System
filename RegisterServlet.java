import java.io.IOException;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final UserDAO dao = new UserDAO();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String role = request.getParameter("role");

        String contextPath = request.getContextPath();

        // Check required fields
        if (name == null || name.trim().isEmpty()
                || email == null || email.trim().isEmpty()
                || password == null || password.isEmpty()
                || role == null || role.trim().isEmpty()) {

            response.sendRedirect(
                    contextPath + "/register.html?error=missing");
            return;
        }

        name = name.trim();
        email = email.trim();
        role = role.trim().toLowerCase();

        // Validate role
        if (!role.equals("patient")
                && !role.equals("doctor")) {

            response.sendRedirect(
                    contextPath + "/register.html?error=invalidrole");
            return;
        }

        try {
            // Check existing email
            if (dao.emailExists(email)) {
                response.sendRedirect(
                        contextPath + "/register.html?error=exists");
                return;
            }

            // Register user
            boolean success = dao.addUser(
                    name,
                    email,
                    password,
                    role
            );

            if (success) {
                response.sendRedirect(
                        contextPath + "/login.html?registered=success");
            } else {
                response.sendRedirect(
                        contextPath + "/register.html?error=failed");
            }

        } catch (RuntimeException e) {
            // Keep the browser message generic while recording the cause in Tomcat logs.
            getServletContext().log("Registration failed due to a server-side error.", e);

            response.sendRedirect(
                    contextPath + "/register.html?error=failed");
        }
    }
}
