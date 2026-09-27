import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/doctor")
public class DoctorServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");

        response.getWriter().println("""
            <!DOCTYPE html>
            <html>
            <head>
                <title>Doctor Dashboard</title>
                <link rel="stylesheet" href="style.css">
            </head>

            <body>

                <header class="navbar">
                    <div class="logo">🩺 MediCare</div>

                    <nav>
                        <a href="doctor-dashboard.html">Dashboard</a>
                        <a href="logout">Logout</a>
                    </nav>
                </header>

                <main class="dashboard">

                    <div class="welcome">
                        <h1>Doctor Dashboard 👨‍⚕️</h1>

                        <p>
                            Manage appointments, schedules
                            and patient information.
                        </p>
                    </div>

                    <section class="dashboard-grid">

                        <div class="dashboard-card">
                            <div class="card-icon">📅</div>

                            <h2>Appointments</h2>

                            <p>
                                View your patient appointments.
                            </p>

                            <a href="appointment.html"
                               class="btn">
                                View Appointments
                            </a>
                        </div>

                        <div class="dashboard-card">
                            <div class="card-icon">🕐</div>

                            <h2>My Schedule</h2>

                            <p>
                                Manage your working schedule.
                            </p>

                            <button class="btn">
                                Manage Schedule
                            </button>
                        </div>

                        <div class="dashboard-card">
                            <div class="card-icon">👥</div>

                            <h2>Patients</h2>

                            <p>
                                View patient information.
                            </p>

                            <button class="btn">
                                View Patients
                            </button>
                        </div>

                    </section>

                </main>

            </body>
            </html>
            """);
    }
}