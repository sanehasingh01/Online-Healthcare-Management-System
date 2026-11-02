import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import dao.AppointmentDAO;
import java.time.LocalDateTime;
import util.AppointmentTimeValidator;

@WebServlet("/appointment")
public class AppointmentServlet extends HttpServlet {

    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String contextPath = request.getContextPath();
        String doctorValue = request.getParameter("doctorId");
        String date = request.getParameter("date");
        String time = request.getParameter("time");

        HttpSession session = request.getSession(false);
        if (session == null || !"patient".equals(session.getAttribute("userRole"))
                || !(session.getAttribute("userId") instanceof Integer)) {
            response.sendRedirect(contextPath + "/login.html?error=loginrequired");
            return;
        }
        if (doctorValue == null || doctorValue.isBlank()
                || date == null || date.isBlank()
                || time == null || time.isBlank()) {
            response.sendRedirect(contextPath + "/appointment.html?error=missing");
            return;
        }

        int patientId = (Integer) session.getAttribute("userId");
        int doctorId;
        try {
            doctorId = Integer.parseInt(doctorValue);
        } catch (NumberFormatException e) {
            response.sendRedirect(contextPath + "/appointment.html?error=invalid");
            return;
        }

        if (doctorId <= 0
                || !AppointmentTimeValidator.isFuture(date, time, LocalDateTime.now())) {
            response.sendRedirect(contextPath + "/appointment.html?error=invalid");
            return;
        }


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
                contextPath + "/my-appointments?booked=success"
            );

        } else {

            response.sendRedirect(
                contextPath + "/appointment.html?error=failed"
            );
        }
    }
}
