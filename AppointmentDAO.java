package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import util.DBConnection;

/**
 * AppointmentDAO
 * --------------------------------
 * Manages appointment-related
 * database operations.
 */
public class AppointmentDAO {

    /**
     * Books a new appointment.
     */
    public boolean bookAppointment(
            int patientId,
            int doctorId,
            String date,
            String time) {

        String sql = """
                INSERT INTO appointments
                (
                    patient_id,
                    doctor_id,
                    appointment_date,
                    appointment_time,
                    status
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, patientId);
            statement.setInt(2, doctorId);
            statement.setString(3, date);
            statement.setString(4, time);
            statement.setString(5, "Pending");

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Appointment booking failed: "
                    + e.getMessage()
            );

            return false;
        }
    }
}