package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import util.DBConnection;

public class PatientDAO {

    // ADD PATIENT
    public boolean addPatient(int userId,
                              String medicalHistory) {

        String sql =
            "INSERT INTO patients(patient_id, medical_history) VALUES (?, ?)";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return false;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setString(2, medicalHistory);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Unable to add patient: " + e.getMessage());
            return false;
        }
    }


    // VIEW PATIENTS
    public void viewPatients() {

        String sql = "SELECT * FROM patients";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (var st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
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
            }
        } catch (SQLException e) {
            System.err.println("Unable to view patients: " + e.getMessage());
        }
    }
}
