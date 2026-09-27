import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String name =
            request.getParameter("name");

        String email =
            request.getParameter("email");

        String password =
            request.getParameter("password");

        String role =
            request.getParameter("role");


        UserDAO dao = new UserDAO();

        boolean success =
            dao.addUser(
                name,
                email,
                password,
                role
            );


        if (success) {

            response.sendRedirect("login.html");

        } else {

            response.sendRedirect(
                "register.html?error=failed"
            );
        }
    }
}