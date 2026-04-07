package app.lockin.lockin.common.requests;

import app.lockin.lockin.common.models.Attachment;

public class UpdateProfileRequest extends Request {
    private final String description;
    private final Attachment profilePicture;

    public UpdateProfileRequest(String description, Attachment profilePicture) {
        this.description = description == null ? "" : description.trim();
        this.profilePicture = profilePicture;
    }

    public String getDescription() {
        return description;
    }

    public Attachment getProfilePicture() {
        return profilePicture;
    }

    @Override
    public RequestType getType() {
        return RequestType.UPDATE_PROFILE;
    }
}
