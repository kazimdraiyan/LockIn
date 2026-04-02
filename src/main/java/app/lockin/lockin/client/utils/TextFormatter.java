package app.lockin.lockin.client.utils;

import java.time.Duration;

public class TextFormatter {
    public static String readableDuration(Duration duration) {
        long days = duration.toDays();
        if (days > 0) return days + (days == 1 ? " day" : " days");

        long hours = duration.toHours();
        if (hours > 0) return hours + (hours == 1 ? " hour" : " hours");

        long minutes = duration.toMinutes();
        if (minutes > 0) return minutes + (minutes == 1 ? " minute" : " minutes");

        return "Just now";
    }
}
