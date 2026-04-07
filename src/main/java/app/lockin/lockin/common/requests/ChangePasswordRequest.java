package app.lockin.lockin.common.requests;

public class ChangePasswordRequest extends Request {
    private final String oldPassword;
    private final String newPassword;

    public ChangePasswordRequest(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword == null ? "" : oldPassword;
        this.newPassword = newPassword == null ? "" : newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    @Override
    public RequestType getType() {
        return RequestType.CHANGE_PASSWORD;
    }
}
