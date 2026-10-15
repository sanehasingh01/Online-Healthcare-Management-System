public class Main {

    public static void main(String[] args) {

        // Create Admin object
        Admin admin = new Admin(
                1,
                "Saneha",
                "admin@healthcare.com"
        );

        // Display admin information
        admin.displayAdminInfo();

        // Admin operations
        admin.manageUsers();
        admin.manageDoctors();
        admin.managePatients();
        admin.manageAppointments();
        admin.viewSystemReports();

        // Create Appointment object
        Appointment appointment = new Appointment(
                101,
                "Rahul",
                "Dr. Sharma",
                "2026-10-15",
                "10:30 AM"
        );

        // Display and update appointment
        appointment.displayAppointment();
        appointment.confirmAppointment();
        appointment.displayAppointment();
    }
}