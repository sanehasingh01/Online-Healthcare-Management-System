package util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/** Validates appointment date and time values against the server's local clock. */
public final class AppointmentTimeValidator {
    private AppointmentTimeValidator() {}

    /** Returns true only when the supplied ISO date and time are strictly in the future. */
    public static boolean isFuture(String date, String time, LocalDateTime now) {
        if (date == null || time == null || now == null) return false;
        try {
            return LocalDateTime.of(LocalDate.parse(date), LocalTime.parse(time)).isAfter(now);
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
