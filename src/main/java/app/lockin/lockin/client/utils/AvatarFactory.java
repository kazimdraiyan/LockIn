package app.lockin.lockin.client.utils;

import app.lockin.lockin.client.elements.ProfileAvatar;
import app.lockin.lockin.common.models.Attachment;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.util.Locale;

public final class AvatarFactory {
    private AvatarFactory() {
    }

    public static ProfileAvatar create(String username, double size, Attachment profilePicture) {
        ProfileAvatar avatar = new ProfileAvatar();
        avatar.setSize(size);
        avatar.setText(extractInitials(username));
        if (profilePicture != null && profilePicture.getData() != null && profilePicture.getData().length > 0) {
            avatar.setImage(new Image(new ByteArrayInputStream(profilePicture.getData())));
        } else {
            avatar.setImage(null);
        }
        return avatar;
    }

    private static String extractInitials(String username) {
        if (username == null || username.isBlank()) {
            return "?";
        }
        String trimmed = username.trim();
        return trimmed.substring(0, Math.min(2, trimmed.length())).toUpperCase(Locale.ENGLISH);
    }
}
