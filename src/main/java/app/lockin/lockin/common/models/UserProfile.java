package app.lockin.lockin.common.models;

import java.io.Serializable;

public class UserProfile implements Serializable {
    private final String username;
    private final String description;
    private final PostAttachment profilePicture;

    public UserProfile(String username, String description, PostAttachment profilePicture) {
        this.username = username;
        this.description = description == null ? "" : description;
        this.profilePicture = profilePicture;
    }

    public String getUsername() {
        return username;
    }

    public String getDescription() {
        return description;
    }

    public PostAttachment getProfilePicture() {
        return profilePicture;
    }
}
