package app.lockin.lockin.common.requests;

public class SignUpRequest extends Request {
    private String username;
    private String password;

    public SignUpRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public RequestType getType() {
        return RequestType.SIGNUP;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
