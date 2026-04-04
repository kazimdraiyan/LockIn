package app.lockin.lockin.client.utils;

import app.lockin.lockin.common.models.MessageAttachment;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TextFormatter {
    private static final DateTimeFormatter ABSOLUTE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH).withZone(ZoneId.systemDefault());

    public static String formatTimestamp(long createdAt) {
        Duration age = Duration.between(Instant.ofEpochMilli(createdAt), Instant.now());
        if (age.toMinutes() < 1) {
            return "Just now";
        }
        if (age.toHours() < 1) {
            return age.toMinutes() + " minutes ago";
        }
        if (age.toDays() < 1) {
            return age.toHours() + " hours ago";
        }
        if (age.toDays() < 7) {
            return age.toDays() + " days ago";
        }
        return ABSOLUTE_TIME_FORMAT.format(Instant.ofEpochMilli(createdAt));
    }

    public static String readableFileType(String mimeType) {
        if (mimeType == null) {
            return "File";
        }
        if (mimeType.startsWith("image/")) {
            return "Image";
        }
        return switch (mimeType) {
            case "application/pdf" -> "PDF Document";
            case "text/plain" -> "Text File";
            default -> "File";
        };
    }

    public static String fileBadgeText(String mimeType) {
        if (mimeType == null) {
            return "FILE";
        }
        if ("image/jpeg".equals(mimeType)) {
            return "JPG";
        }
        if ("image/png".equals(mimeType)) {
            return "PNG";
        }
        if ("image/gif".equals(mimeType)) {
            return "GIF";
        }
        if ("application/pdf".equals(mimeType)) {
            return "PDF";
        }
        if ("text/plain".equals(mimeType)) {
            return "TXT";
        }
        return "FILE";
    }

    public static String readableFileSize(long sizeBytes) {
        if (sizeBytes < 1024) {
            return sizeBytes + " B";
        }
        if (sizeBytes < 1024 * 1024) {
            return String.format(Locale.ENGLISH, "%.1f KB", sizeBytes / 1024.0);
        }
        return String.format(Locale.ENGLISH, "%.1f MB", sizeBytes / (1024.0 * 1024.0));
    }
}
