package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import util.DBConnection;

public class DoctorDAO {

    // ADD DOCTOR
    public boolean addDoctor(int userId,
                             String specialization,
                             String schedule) {

        String sql = "INSERT INTO doctors(doctor_id, specialization, schedule) VALUES (?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return false;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setString(2, specialization);
                ps.setString(3, schedule);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Unable to add doctor: " + e.getMessage());
            return false;
        }
    }


    // VIEW DOCTORS
    public void viewDoctors() {

        String sql = "SELECT * FROM doctors";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (var st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {

                System.out.println(
                    "Doctor ID: " + rs.getInt("doctor_id")
                );

                System.out.println(
                    "Specialization: " +
                    rs.getString("specialization")
                );

                System.out.println(
                    "Schedule: " +
                    rs.getString("schedule")
                );

                System.out.println("-------------------");
            }

            }
        } catch (SQLException e) {
            System.err.println("Unable to view doctors: " + e.getMessage());
        }
    }
}
