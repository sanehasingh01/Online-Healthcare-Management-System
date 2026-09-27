import java.sql.*;

public class PatientDAO {

    // ADD PATIENT
    public boolean addPatient(int userId,
                              String medicalHistory) {

        String sql =
            "INSERT INTO patients(user_id, medical_history) VALUES (?, ?)";

        try {
            Connection con = DBConnection.getConnection();

            PreparedStatement ps =
                con.prepareStatement(sql);

            ps.setInt(1, userId);
            ps.setString(2, medicalHistory);

            ps.executeUpdate();

            System.out.println("✅ Patient added successfully!");

            con.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // VIEW PATIENTS
    public void viewPatients() {

        String sql = "SELECT * FROM patients";

        try {
            Connection con =
                DBConnection.getConnection();

            Statement st =
                con.createStatement();

            ResultSet rs =
                st.executeQuery(sql);

            while (rs.next()) {

                System.out.println(
                    "Patient ID: " +
                    rs.getInt("patient_id")
                );

                System.out.println(
                    "Medical History: " +
                    rs.getString("medical_history")
                );

                System.out.println("-------------------");
            }

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}