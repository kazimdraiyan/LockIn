package app.lockin.lockin.server.request;

public class LoginRequest implements Request{
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

    // TODO: Add getters
}
