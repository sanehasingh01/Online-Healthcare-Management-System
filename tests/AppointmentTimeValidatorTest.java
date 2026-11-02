package tests;

import java.time.LocalDateTime;
import util.AppointmentTimeValidator;

/** Deterministic checks for appointment date/time validation. */
public final class AppointmentTimeValidatorTest {
    private AppointmentTimeValidatorTest() {}

    public static void main(String[] args) {
        LocalDateTime now = LocalDateTime.of(2026, 11, 2, 10, 30);
        check(AppointmentTimeValidator.isFuture("2026-11-02", "10:31", now), "later today is accepted");
        check(AppointmentTimeValidator.isFuture("2026-11-03", "09:00", now), "future date is accepted");
        check(!AppointmentTimeValidator.isFuture("2026-11-02", "10:30", now), "current minute is rejected");
        check(!AppointmentTimeValidator.isFuture("2026-11-02", "09:00", now), "past time today is rejected");
        check(!AppointmentTimeValidator.isFuture("2026-11-01", "12:00", now), "past date is rejected");
        check(!AppointmentTimeValidator.isFuture("not-a-date", "12:00", now), "invalid date is rejected");
        check(!AppointmentTimeValidator.isFuture("2026-11-03", "not-a-time", now), "invalid time is rejected");
        check(!AppointmentTimeValidator.isFuture(null, "12:00", now), "missing date is rejected");
        System.out.println("AppointmentTimeValidator checks passed: 8");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError("Failed: " + message);
    }
}
