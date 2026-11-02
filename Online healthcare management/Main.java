import java.util.ArrayList;
import java.util.Scanner;



class User {

    protected int id;
    protected String name;
    protected String email;
    protected String password;
    protected String role;

    public User(int id, String name, String email,
                String password, String role) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public void displayUser() {

        System.out.println(
                "ID: " + id +
                " | Name: " + name +
                " | Email: " + email +
                " | Role: " + role
        );
    }
}


// ================= ADMIN CLASS =================

class Admin extends User {

    public Admin(int id, String name, String email,
                 String password) {

        super(id, name, email, password, "Admin");
    }
}


// ================= DOCTOR CLASS =================

class Doctor extends User {

    private String specialization;
    private String schedule;

    public Doctor(int id, String name, String email,
                  String password, String specialization) {

        super(id, name, email, password, "Doctor");

        this.specialization = specialization;
        this.schedule = "Not Set";
    }

    public String getSpecialization() {
        return specialization;
    }

    // FIXED: Getter for private schedule

    public String getSchedule() {
        return schedule;
    }

    public void updateSchedule(String schedule) {

        this.schedule = schedule;
    }

    public void displayDoctor() {

        System.out.println(
                "ID: " + id +
                " | Doctor: " + name +
                " | Specialization: " + specialization +
                " | Schedule: " + schedule
        );
    }
}


// ================= PATIENT CLASS =================

class Patient extends User {

    private String medicalHistory;

    public Patient(int id, String name, String email,
                   String password) {

        super(id, name, email, password, "Patient");

        this.medicalHistory =
                "No medical history available.";
    }

    public void updateMedicalHistory(String history) {

        this.medicalHistory = history;
    }

    public void displayMedicalHistory() {

        System.out.println("\nMedical History");
        System.out.println("-----------------------------");
        System.out.println(medicalHistory);
    }
}


// ================= APPOINTMENT CLASS =================

class Appointment {

    private int appointmentId;
    private String patientName;
    private String doctorName;
    private String date;
    private String time;
    private String status;

    public Appointment(int appointmentId,
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

    public int getAppointmentId() {

        return appointmentId;
    }

    public String getPatientName() {

        return patientName;
    }

    public String getDoctorName() {

        return doctorName;
    }

    public void setStatus(String status) {

        this.status = status;
    }

    public void displayAppointment() {

        System.out.println(
                "Appointment ID: " + appointmentId +
                " | Patient: " + patientName +
                " | Doctor: " + doctorName +
                " | Date: " + date +
                " | Time: " + time +
                " | Status: " + status
        );
    }
}


// ================= HEALTHCARE SYSTEM =================

class HealthcareSystem {

    Scanner sc = new Scanner(System.in);

    ArrayList<User> users = new ArrayList<>();

    ArrayList<Doctor> doctors = new ArrayList<>();

    ArrayList<Patient> patients = new ArrayList<>();

    ArrayList<Appointment> appointments =
            new ArrayList<>();

    int userId = 5;

    int appointmentId = 1;


    // ================= CONSTRUCTOR =================

    public HealthcareSystem() {

        Admin admin = new Admin(
                1,
                "System Admin",
                "admin@gmail.com",
                "admin123"
        );

        Doctor doctor1 = new Doctor(
                2,
                "Dr. Rahul Sharma",
                "rahul@gmail.com",
                "1234",
                "Cardiologist"
        );

        Doctor doctor2 = new Doctor(
                3,
                "Dr. Priya Singh",
                "priya@gmail.com",
                "1234",
                "Dermatologist"
        );

        Patient patient = new Patient(
                4,
                "Saneha Singh",
                "saneha@gmail.com",
                "1234"
        );

        users.add(admin);
        users.add(doctor1);
        users.add(doctor2);
        users.add(patient);

        doctors.add(doctor1);
        doctors.add(doctor2);

        patients.add(patient);
    }


    // ================= MAIN LOGIN =================

    public void startSystem() {

        while (true) {

            System.out.println("\n======================================");
            System.out.println(" ONLINE HEALTHCARE MANAGEMENT SYSTEM");
            System.out.println("======================================");

            System.out.println("1. Admin Login");
            System.out.println("2. Doctor Login");
            System.out.println("3. Patient Login");
            System.out.println("4. Exit");

            System.out.print("Enter choice: ");

            int choice = getInt();

            switch (choice) {

                case 1:
                    adminLogin();
                    break;

                case 2:
                    doctorLogin();
                    break;

                case 3:
                    patientLogin();
                    break;

                case 4:

                    System.out.println(
                            "Thank you for using the system!"
                    );

                    return;

                default:

                    System.out.println("Invalid choice!");
            }
        }
    }


    // ================= ADMIN LOGIN =================

    public void adminLogin() {

        System.out.println("\n--------- ADMIN LOGIN ---------");

        System.out.print("Email: ");

        String email = sc.nextLine();

        System.out.print("Password: ");

        String password = sc.nextLine();

        for (User user : users) {

            if (user instanceof Admin &&
                    user.email.equals(email) &&
                    user.password.equals(password)) {

                adminDashboard((Admin) user);

                return;
            }
        }

        System.out.println("Invalid admin credentials!");
    }


    // ================= DOCTOR LOGIN =================

    public void doctorLogin() {

        System.out.println("\n--------- DOCTOR LOGIN ---------");

        System.out.print("Email: ");

        String email = sc.nextLine();

        System.out.print("Password: ");

        String password = sc.nextLine();

        for (Doctor doctor : doctors) {

            if (doctor.email.equals(email) &&
                    doctor.password.equals(password)) {

                doctorDashboard(doctor);

                return;
            }
        }

        System.out.println("Invalid doctor credentials!");
    }


    // ================= PATIENT LOGIN =================

    public void patientLogin() {

        System.out.println("\n--------- PATIENT LOGIN ---------");

        System.out.print("Email: ");

        String email = sc.nextLine();

        System.out.print("Password: ");

        String password = sc.nextLine();

        for (Patient patient : patients) {

            if (patient.email.equals(email) &&
                    patient.password.equals(password)) {

                patientDashboard(patient);

                return;
            }
        }

        System.out.println("Invalid patient credentials!");
    }


    // ================= ADMIN DASHBOARD =================

    public void adminDashboard(Admin admin) {

        while (true) {

            System.out.println("\n========== ADMIN DASHBOARD ==========");

            System.out.println("1. User Management");
            System.out.println("2. Appointment Management");
            System.out.println("3. System Settings");
            System.out.println("4. Performance Analytics");
            System.out.println("5. Logout");

            System.out.print("Enter choice: ");

            int choice = getInt();

            switch (choice) {

                case 1:
                    userManagement();
                    break;

                case 2:
                    appointmentManagement();
                    break;

                case 3:
                    systemSettings();
                    break;

                case 4:
                    performanceAnalytics();
                    break;

                case 5:

                    System.out.println(
                            "Logged out successfully."
                    );

                    return;

                default:

                    System.out.println("Invalid choice!");
            }
        }
    }


    // ================= USER MANAGEMENT =================

    public void userManagement() {

        while (true) {

            System.out.println("\n--------- USER MANAGEMENT ---------");

            System.out.println("1. View Users");
            System.out.println("2. Add Doctor");
            System.out.println("3. Add Patient");
            System.out.println("4. Delete User");
            System.out.println("5. Back");

            System.out.print("Enter choice: ");

            int choice = getInt();

            switch (choice) {

                case 1:
                    viewUsers();
                    break;

                case 2:
                    addDoctor();
                    break;

                case 3:
                    addPatient();
                    break;

                case 4:
                    deleteUser();
                    break;

                case 5:
                    return;

                default:

                    System.out.println("Invalid choice!");
            }
        }
    }


    // ================= VIEW USERS =================

    public void viewUsers() {

        System.out.println("\n--------------- USERS ---------------");

        if (users.isEmpty()) {

            System.out.println("No users found.");

        } else {

            for (User user : users) {

                user.displayUser();
            }
        }
    }


    // ================= ADD DOCTOR =================

    public void addDoctor() {

        System.out.println("\n--------- ADD DOCTOR ---------");

        System.out.print("Doctor Name: ");

        String name = sc.nextLine();

        System.out.print("Email: ");

        String email = sc.nextLine();

        System.out.print("Password: ");

        String password = sc.nextLine();

        System.out.print("Specialization: ");

        String specialization = sc.nextLine();

        Doctor doctor = new Doctor(
                userId++,
                name,
                email,
                password,
                specialization
        );

        doctors.add(doctor);

        users.add(doctor);

        System.out.println("Doctor added successfully!");
    }


    // ================= ADD PATIENT =================

    public void addPatient() {

        System.out.println("\n--------- ADD PATIENT ---------");

        System.out.print("Patient Name: ");

        String name = sc.nextLine();

        System.out.print("Email: ");

        String email = sc.nextLine();

        System.out.print("Password: ");

        String password = sc.nextLine();

        Patient patient = new Patient(
                userId++,
                name,
                email,
                password
        );

        patients.add(patient);

        users.add(patient);

        System.out.println("Patient added successfully!");
    }


    // ================= DELETE USER =================

    public void deleteUser() {

        System.out.print("Enter User ID to delete: ");

        int id = getInt();

        User found = null;

        for (User user : users) {

            if (user.getId() == id) {

                found = user;

                break;
            }
        }

        if (found != null) {

            // Do not delete the main admin

            if (found instanceof Admin) {

                System.out.println(
                        "Admin cannot be deleted!"
                );

                return;
            }

            users.remove(found);

            if (found instanceof Doctor) {

                doctors.remove(found);
            }

            if (found instanceof Patient) {

                patients.remove(found);
            }

            System.out.println(
                    "User deleted successfully!"
            );

        } else {

            System.out.println("User not found!");
        }
    }


    // ================= APPOINTMENT MANAGEMENT =================

    public void appointmentManagement() {

        System.out.println("\n------ ALL APPOINTMENTS ------");

        if (appointments.isEmpty()) {

            System.out.println(
                    "No appointments available."
            );

        } else {

            for (Appointment appointment : appointments) {

                appointment.displayAppointment();
            }
        }
    }


    // ================= SYSTEM SETTINGS =================

    public void systemSettings() {

        System.out.println("\n--------- SYSTEM SETTINGS ---------");

        System.out.println("1. Hospital Name");
        System.out.println("2. Working Hours");

        System.out.print("Enter choice: ");

        int choice = getInt();

        if (choice == 1) {

            System.out.print("Enter hospital name: ");

            String hospital = sc.nextLine();

            System.out.println(
                    "Hospital name updated to: " + hospital
            );

        } else if (choice == 2) {

            System.out.print("Enter working hours: ");

            String hours = sc.nextLine();

            System.out.println(
                    "Working hours updated to: " + hours
            );

        } else {

            System.out.println("Invalid choice!");
        }
    }




    public void performanceAnalytics() {

        System.out.println(
                "\n--------- PERFORMANCE ANALYTICS ---------"
        );

        System.out.println(
                "Total Users: " + users.size()
        );

        System.out.println(
                "Total Doctors: " + doctors.size()
        );

        System.out.println(
                "Total Patients: " + patients.size()
        );

        System.out.println(
                "Total Appointments: " + appointments.size()
        );
    }


    // ================= DOCTOR DASHBOARD =================

    public void doctorDashboard(Doctor doctor) {

        while (true) {

            System.out.println(
                    "\n========== DOCTOR DASHBOARD =========="
            );

            System.out.println(
                    "Welcome Dr. " + doctor.getName()
            );

            System.out.println("1. Schedule Management");
            System.out.println("2. Patient Records");
            System.out.println("3. Appointment Overview");
            System.out.println("4. Patient Feedback");
            System.out.println("5. Logout");

            System.out.print("Enter choice: ");

            int choice = getInt();

            switch (choice) {

                case 1:
                    scheduleManagement(doctor);
                    break;

                case 2:
                    patientRecords();
                    break;

                case 3:
                    doctorAppointments(doctor);
                    break;

                case 4:
                    patientFeedback();
                    break;

                case 5:

                    System.out.println(
                            "Logged out successfully."
                    );

                    return;

                default:

                    System.out.println("Invalid choice!");
            }
        }
    }


    // ================= SCHEDULE MANAGEMENT =================

    public void scheduleManagement(Doctor doctor) {

        System.out.println(
                "\n--------- SCHEDULE MANAGEMENT ---------"
        );

        // FIXED: Use getter instead of private variable

        System.out.println(
                "Current Schedule: " + doctor.getSchedule()
        );

        System.out.print("Enter new schedule: ");

        String schedule = sc.nextLine();

        doctor.updateSchedule(schedule);

        System.out.println(
                "Schedule updated successfully!"
        );
    }


    // ================= PATIENT RECORDS =================

    public void patientRecords() {

        System.out.println("\n--------- PATIENT RECORDS ---------");

        if (patients.isEmpty()) {

            System.out.println(
                    "No patient records found."
            );

            return;
        }

        for (Patient patient : patients) {

            System.out.println(
                    "Patient ID: " +
                    patient.getId() +
                    " | Name: " +
                    patient.getName()
            );
        }

        System.out.print(
                "\nEnter Patient ID to view history: "
        );

        int id = getInt();

        for (Patient patient : patients) {

            if (patient.getId() == id) {

                patient.displayMedicalHistory();

                System.out.print(
                        "\nUpdate medical history? (yes/no): "
                );

                String answer = sc.nextLine();

                if (answer.equalsIgnoreCase("yes")) {

                    System.out.print(
                            "Enter medical history: "
                    );

                    String history = sc.nextLine();

                    patient.updateMedicalHistory(history);

                    System.out.println(
                            "Medical record updated!"
                    );
                }

                return;
            }
        }

        System.out.println("Patient not found!");
    }


    // ================= DOCTOR APPOINTMENTS =================

    public void doctorAppointments(Doctor doctor) {

        System.out.println(
                "\n--------- MY APPOINTMENTS ---------"
        );

        boolean found = false;

        for (Appointment appointment : appointments) {

            if (appointment.getDoctorName()
                    .equalsIgnoreCase(doctor.getName())) {

                appointment.displayAppointment();

                found = true;
            }
        }

        if (!found) {

            System.out.println(
                    "No appointments found."
            );
        }
    }


    // ================= PATIENT FEEDBACK =================

    public void patientFeedback() {

        System.out.println(
                "\n--------- PATIENT FEEDBACK ---------"
        );

        System.out.println("1. Excellent");
        System.out.println("2. Good");
        System.out.println("3. Average");
        System.out.println("4. Poor");

        System.out.println(
                "Patient feedback section."
        );
    }


    // ================= PATIENT DASHBOARD =================

    public void patientDashboard(Patient patient) {

        while (true) {

            System.out.println(
                    "\n========== PATIENT DASHBOARD =========="
            );

            System.out.println(
                    "Welcome " + patient.getName()
            );

            System.out.println("1. Book Appointment");
            System.out.println("2. Appointment History");
            System.out.println("3. Medical History");
            System.out.println("4. Profile Management");
            System.out.println("5. Logout");

            System.out.print("Enter choice: ");

            int choice = getInt();

            switch (choice) {

                case 1:
                    bookAppointment(patient);
                    break;

                case 2:
                    appointmentHistory(patient);
                    break;

                case 3:
                    patient.displayMedicalHistory();
                    break;

                case 4:
                    profileManagement(patient);
                    break;

                case 5:

                    System.out.println(
                            "Logged out successfully."
                    );

                    return;

                default:

                    System.out.println("Invalid choice!");
            }
        }
    }


    // ================= BOOK APPOINTMENT =================

    public void bookAppointment(Patient patient) {

        System.out.println(
                "\n--------- BOOK APPOINTMENT ---------"
        );

        if (doctors.isEmpty()) {

            System.out.println(
                    "No doctors available."
            );

            return;
        }

        System.out.println("\nAvailable Doctors:");

        for (Doctor doctor : doctors) {

            doctor.displayDoctor();
        }

        System.out.print("\nEnter Doctor ID: ");

        int doctorId = getInt();

        Doctor selectedDoctor = null;

        for (Doctor doctor : doctors) {

            if (doctor.getId() == doctorId) {

                selectedDoctor = doctor;

                break;
            }
        }

        if (selectedDoctor == null) {

            System.out.println(
                    "Doctor not found!"
            );

            return;
        }

        System.out.print("Enter preferred date: ");

        String date = sc.nextLine();

        System.out.print("Enter preferred time: ");

        String time = sc.nextLine();

        Appointment appointment = new Appointment(
                appointmentId++,
                patient.getName(),
                selectedDoctor.getName(),
                date,
                time
        );

        appointments.add(appointment);

        System.out.println(
                "\nAppointment booked successfully!"
        );

        appointment.displayAppointment();
    }


    // ================= APPOINTMENT HISTORY =================

    public void appointmentHistory(Patient patient) {

        System.out.println(
                "\n--------- APPOINTMENT HISTORY ---------"
        );

        boolean found = false;

        for (Appointment appointment : appointments) {

            if (appointment.getPatientName()
                    .equalsIgnoreCase(patient.getName())) {

                appointment.displayAppointment();

                found = true;
            }
        }

        if (!found) {

            System.out.println(
                    "No appointment history found."
            );
        }
    }


    // ================= PROFILE MANAGEMENT =================

    public void profileManagement(Patient patient) {

        System.out.println(
                "\n--------- PROFILE MANAGEMENT ---------"
        );

        System.out.println(
                "Current Name: " + patient.getName()
        );

        System.out.println(
                "Current Email: " + patient.getEmail()
        );

        System.out.println(
                "\nProfile management feature."
        );

        System.out.println(
                "Profile update can be connected to database."
        );
    }


    

    public int getInt() {

        while (true) {

            try {

                int value = Integer.parseInt(
                        sc.nextLine()
                );

                return value;

            } catch (Exception e) {

                System.out.print(
                        "Please enter a valid number: "
                );
            }
        }
    }
}


// ================= MAIN CLASS =================

public class Main {

    public static void main(String[] args) {

        HealthcareSystem system =
                new HealthcareSystem();

        system.startSystem();
    }
}