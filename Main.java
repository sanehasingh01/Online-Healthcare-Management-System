public class Admin {

    private int adminId;
    private String name;
    private String email;

    // Constructor
    public Admin(int adminId, String name, String email) {
        this.adminId = adminId;
        this.name = name;
        this.email = email;
    }

    // Getters
    public int getAdminId() {
        return adminId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    // Admin functions

    public void manageUsers() {
        System.out.println("Admin is managing users...");
    }

    public void manageDoctors() {
        System.out.println("Admin is managing doctors...");
    }

    public void managePatients() {
        System.out.println("Admin is managing patients...");
    }

    public void manageAppointments() {
        System.out.println("Admin is managing appointments...");
    }

    public void viewSystemReports() {
        System.out.println("Admin is viewing system reports...");
    }

    // Display admin information
    public void displayAdminInfo() {

        System.out.println("\n===== ADMIN INFORMATION =====");
        System.out.println("Admin ID: " + adminId);
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
    }
}