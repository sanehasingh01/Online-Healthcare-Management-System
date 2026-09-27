import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {

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
                <title>Admin Dashboard</title>
                <link rel="stylesheet" href="style.css">
            </head>

            <body>

                <header class="navbar">

                    <div class="logo">
                        🩺 MediCare
                    </div>

                    <nav>
                        <a href="admin-dashboard.html">
                            Dashboard
                        </a>

                        <a href="logout">
                            Logout
                        </a>
                    </nav>

                </header>


                <main class="dashboard">

                    <div class="welcome">

                        <h1>
                            Admin Dashboard ⚙️
                        </h1>

                        <p>
                            Manage users, doctors,
                            patients and appointments.
                        </p>

                    </div>


                    <section class="dashboard-grid">

                        <div class="dashboard-card">

                            <div class="card-icon">
                                👥
                            </div>

                            <h2>
                                Manage Users
                            </h2>

                            <p>
                                Manage registered
                                system users.
                            </p>

                            <button class="btn">
                                Manage Users
                            </button>

                        </div>


                        <div class="dashboard-card">

                            <div class="card-icon">
                                👨‍⚕️
                            </div>

                            <h2>
                                Doctors
                            </h2>

                            <p>
                                Manage doctor accounts.
                            </p>

                            <button class="btn">
                                Manage Doctors
                            </button>

                        </div>


                        <div class="dashboard-card">

                            <div class="card-icon">
                                🧑‍🤝‍🧑
                            </div>

                            <h2>
                                Patients
                            </h2>

                            <p>
                                Manage registered patients.
                            </p>

                            <button class="btn">
                                Manage Patients
                            </button>

                        </div>


                        <div class="dashboard-card">

                            <div class="card-icon">
                                📅
                            </div>

                            <h2>
                                Appointments
                            </h2>

                            <p>
                                Monitor appointments.
                            </p>

                            <button class="btn">
                                View Appointments
                            </button>

                        </div>

                    </section>

                </main>

            </body>

            </html>
            """);
    }
}