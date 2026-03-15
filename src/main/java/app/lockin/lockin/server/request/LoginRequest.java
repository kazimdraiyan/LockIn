package app.lockin.lockin.server.request;

public class LoginRequest extends Request {
    private String username;
    private String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public RequestType getType() {
        return RequestType.LOGIN;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
