package tests;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import dao.AppointmentDAO;
import dao.UserDAO;
import util.DBConnection;

/** Runs against the disposable MySQL service configured by GitHub Actions. */
public final class AppointmentBookingIntegrationTest {
    private AppointmentBookingIntegrationTest() {}

    public static void main(String[] args) throws Exception {
        String suffix = UUID.randomUUID().toString();
        int patientOne = 0;
        int patientTwo = 0;
        int doctorId = 0;

        try {
            patientOne = createUser("patient-one-" + suffix, "patient");
            patientTwo = createUser("patient-two-" + suffix, "patient");
            doctorId = createUser("doctor-" + suffix, "doctor");
            final int firstPatientId = patientOne;
            final int secondPatientId = patientTwo;
            final int testDoctorId = doctorId;
            AppointmentDAO dao = new AppointmentDAO();
            String firstDate = LocalDate.now().plusDays(3).toString();
            assertResult(dao.bookAppointment(patientOne, doctorId, firstDate, "10:00"),
                    AppointmentDAO.BookingResult.BOOKED, "first booking succeeds");
            assertResult(dao.bookAppointment(patientTwo, doctorId, firstDate, "10:00"),
                    AppointmentDAO.BookingResult.SLOT_TAKEN, "active duplicate booking is rejected");

            cancelPending(patientOne, doctorId, firstDate, "10:00");
            assertResult(dao.bookAppointment(patientTwo, doctorId, firstDate, "10:00"),
                    AppointmentDAO.BookingResult.BOOKED, "cancelled slot can be booked again");

            String concurrentDate = LocalDate.now().plusDays(4).toString();
            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);
            ExecutorService pool = Executors.newFixedThreadPool(2);
            try {
                Future<AppointmentDAO.BookingResult> one = pool.submit(() -> {
                    ready.countDown();
                    start.await();
                    return dao.bookAppointment(firstPatientId, testDoctorId, concurrentDate, "11:00");
                });
                Future<AppointmentDAO.BookingResult> two = pool.submit(() -> {
                    ready.countDown();
                    start.await();
                    return dao.bookAppointment(secondPatientId, testDoctorId, concurrentDate, "11:00");
                });
                ready.await();
                start.countDown();
                AppointmentDAO.BookingResult resultOne = one.get();
                AppointmentDAO.BookingResult resultTwo = two.get();
                int booked = count(resultOne, AppointmentDAO.BookingResult.BOOKED)
                           + count(resultTwo, AppointmentDAO.BookingResult.BOOKED);
                int taken = count(resultOne, AppointmentDAO.BookingResult.SLOT_TAKEN)
                          + count(resultTwo, AppointmentDAO.BookingResult.SLOT_TAKEN);
                if (booked != 1 || taken != 1) {
                    throw new AssertionError("Concurrent requests must yield one booking and one slot rejection; got "
                            + resultOne + " and " + resultTwo);
                }
            } finally {
                pool.shutdownNow();
            }

            System.out.println("AppointmentBooking integration checks passed: 4");
        } finally {
            deleteTestData(patientOne, patientTwo, doctorId);
        }
    }

    private static int createUser(String suffix, String role) throws Exception {
        String email = suffix + "@example.test";
        if (!new UserDAO().addUser("Integration Test", email, "Test-only-Password-42", role)) {
            throw new AssertionError("Could not create disposable " + role + " user.");
        }
        int id = new UserDAO().getUserIdAndEnsureProfile(email, role);
        if (id < 1) throw new AssertionError("Could not find newly created test user.");
        return id;
    }

    private static void cancelPending(int patientId, int doctorId, String date, String time)
            throws Exception {
        String sql = "UPDATE appointments SET status = 'Cancelled' "
                   + "WHERE patient_id = ? AND doctor_id = ? AND appointment_date = ? "
                   + "AND appointment_time = ? AND status = 'Pending'";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, patientId);
            statement.setInt(2, doctorId);
            statement.setString(3, date);
            statement.setString(4, time);
            if (statement.executeUpdate() != 1) {
                throw new AssertionError("Expected to cancel the first test booking.");
            }
        }
    }

    private static void deleteTestData(int patientOne, int patientTwo, int doctorId)
            throws Exception {
        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) throw new AssertionError("Database unavailable during test cleanup.");
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM appointments WHERE patient_id IN (?, ?)")) {
                    statement.setInt(1, patientOne);
                    statement.setInt(2, patientTwo);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM patients WHERE patient_id IN (?, ?)")) {
                    statement.setInt(1, patientOne);
                    statement.setInt(2, patientTwo);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM doctors WHERE doctor_id = ?")) {
                    statement.setInt(1, doctorId);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM users WHERE id IN (?, ?, ?)")) {
                    statement.setInt(1, patientOne);
                    statement.setInt(2, patientTwo);
                    statement.setInt(3, doctorId);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private static int count(AppointmentDAO.BookingResult actual,
                             AppointmentDAO.BookingResult expected) {
        return actual == expected ? 1 : 0;
    }

    private static void assertResult(AppointmentDAO.BookingResult actual,
                                     AppointmentDAO.BookingResult expected, String message) {
        if (actual != expected) {
            throw new AssertionError(message + ": expected " + expected + " but got " + actual);
        }
    }
}
