package util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class TimeUtil {

    public static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter DISPLAY =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private TimeUtil() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZONE);
    }

    public static String format(LocalDateTime value) {
        if (value == null) {
            return "—";
        }
        return value.format(DISPLAY);
    }
}
