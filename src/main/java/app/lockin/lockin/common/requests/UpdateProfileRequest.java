package app.lockin.lockin.common.requests;

import app.lockin.lockin.common.models.PostAttachment;

public class UpdateProfileRequest extends Request {
    private final String description;
    private final PostAttachment profilePicture;

    public UpdateProfileRequest(String description, PostAttachment profilePicture) {
        this.description = description == null ? "" : description.trim();
        this.profilePicture = profilePicture;
    }

    public String getDescription() {
        return description;
    }

    public PostAttachment getProfilePicture() {
        return profilePicture;
    }

    @Override
    public RequestType getType() {
        return RequestType.UPDATE_PROFILE;
    }
}
