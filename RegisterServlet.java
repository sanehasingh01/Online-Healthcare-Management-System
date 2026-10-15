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

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String role = request.getParameter("role");

        // Check that all required fields are provided
        if (name == null || name.trim().isEmpty()
                || email == null || email.trim().isEmpty()
                || password == null || password.isEmpty()
                || role == null || role.trim().isEmpty()) {

            response.sendRedirect("register.html?error=missing");
            return;
        }

        name = name.trim();
        email = email.trim();
        role = role.trim().toLowerCase();

        // Allow only the roles supported by this application
        if (!role.equals("patient")
                && !role.equals("doctor")
                && !role.equals("admin")) {

            response.sendRedirect("register.html?error=invalidrole");
            return;
        }

        // Check whether the email is already registered
        if (dao.emailExists(email)) {
            response.sendRedirect("register.html?error=exists");
            return;
        }

        // Save the new user
        boolean success = dao.addUser(
                name,
                email,
                password,
                role
        );

        if (success) {
            response.sendRedirect("login.html?registered=success");
        } else {
            response.sendRedirect("register.html?error=failed");
        }
    }
}