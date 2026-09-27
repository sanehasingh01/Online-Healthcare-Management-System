public class Appointment {

    private int appointmentId;
    private String patientName;
    private String doctorName;
    private String date;
    private String time;
    private String status;

    public Appointment(
            int appointmentId,
            String patientName,
            String doctorName,
            String date,
            String time) {

        this.appointmentId = appointmentId;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.date = date;
        this.time = time;
        this.status = "Pending";
    }

    public void confirmAppointment() {
        status = "Confirmed";
        System.out.println("Appointment confirmed.");
    }

    public void cancelAppointment() {
        status = "Cancelled";
        System.out.println("Appointment cancelled.");
    }

    public void displayAppointment() {

        System.out.println("\n===== APPOINTMENT =====");

        System.out.println("Appointment ID: " + appointmentId);
        System.out.println("Patient: " + patientName);
        System.out.println("Doctor: " + doctorName);
        System.out.println("Date: " + date);
        System.out.println("Time: " + time);
        System.out.println("Status: " + status);
    }
}