import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String email =
            request.getParameter("email");

        String password =
            request.getParameter("password");


        UserDAO dao = new UserDAO();

        boolean success =
            dao.loginUser(email, password);


        if (success) {

            response.sendRedirect("dashboard.html");

        } else {

            response.sendRedirect("login.html?error=InvalidLogin");
        }
    }
}