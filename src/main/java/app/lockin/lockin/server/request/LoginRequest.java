package app.lockin.lockin.server.request;

public class LoginRequest implements Request{
    private String email;
    private String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    @Override
    public RequestType getType() {
        return RequestType.LOGIN;
    }

    // TODO: Add getters
}
