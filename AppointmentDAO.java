package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import util.DBConnection;

/** Performs transactional appointment booking and avoids double-booking a doctor. */
public class AppointmentDAO {
    private static final Logger LOGGER = Logger.getLogger(AppointmentDAO.class.getName());

    public enum BookingResult {
        BOOKED,
        SLOT_TAKEN,
        FAILED
    }

    /**
     * Books a slot while locking the doctor row. Concurrent booking attempts
     * for one doctor are serialized until the existing-slot check and insert finish.
     */
    public BookingResult bookAppointment(int patientId, int doctorId, String date, String time) {
        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) return BookingResult.FAILED;
            connection.setAutoCommit(false);

            try {
                if (!lockDoctor(connection, doctorId)) {
                    connection.rollback();
                    return BookingResult.FAILED;
                }
                if (slotIsBooked(connection, doctorId, date, time)) {
                    connection.rollback();
                    return BookingResult.SLOT_TAKEN;
                }

                String sql = "INSERT INTO appointments "
                           + "(patient_id, doctor_id, appointment_date, appointment_time, status) "
                           + "VALUES (?, ?, ?, ?, 'Pending')";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, patientId);
                    statement.setInt(2, doctorId);
                    statement.setString(3, date);
                    statement.setString(4, time);
                    if (statement.executeUpdate() > 0) {
                        connection.commit();
                        return BookingResult.BOOKED;
                    }
                }

                connection.rollback();
                return BookingResult.FAILED;
            } catch (SQLException e) {
                rollbackQuietly(connection, e);
                LOGGER.log(Level.SEVERE, "Appointment booking failed", e);
                return BookingResult.FAILED;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to start appointment booking transaction", e);
            return BookingResult.FAILED;
        }
    }

    private boolean lockDoctor(Connection connection, int doctorId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT doctor_id FROM doctors WHERE doctor_id = ? FOR UPDATE")) {
            statement.setInt(1, doctorId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private boolean slotIsBooked(Connection connection, int doctorId, String date, String time)
            throws SQLException {
        String sql = "SELECT appointment_id FROM appointments "
                   + "WHERE doctor_id = ? AND appointment_date = ? AND appointment_time = ? "
                   + "AND COALESCE(status, '') <> 'Cancelled' LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, doctorId);
            statement.setString(2, date);
            statement.setString(3, time);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private void rollbackQuietly(Connection connection, SQLException originalError) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            originalError.addSuppressed(rollbackError);
        }
    }
}
