package app.lockin.lockin.server.request;

public class SignUpRequest implements Request {
    private String name;
    private String email;
    private String password;

    public SignUpRequest(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    @Override
    public RequestType getType() {
        return RequestType.SIGNUP;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
