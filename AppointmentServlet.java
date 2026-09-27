import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/appointment")
public class AppointmentServlet extends HttpServlet {

    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        int patientId =
            Integer.parseInt(
                request.getParameter("patientId")
            );

        int doctorId =
            Integer.parseInt(
                request.getParameter("doctorId")
            );

        String date =
            request.getParameter("date");

        String time =
            request.getParameter("time");


        AppointmentDAO dao =
            new AppointmentDAO();


        boolean success =
            dao.bookAppointment(
                patientId,
                doctorId,
                date,
                time
            );


        if (success) {

            response.sendRedirect(
                "patient-dashboard.html"
            );

        } else {

            response.sendRedirect(
                "book-appointment.html?error=failed"
            );
        }
    }
}