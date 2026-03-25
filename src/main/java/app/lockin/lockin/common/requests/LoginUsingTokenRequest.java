package app.lockin.lockin.common.requests;

public class LoginUsingTokenRequest extends Request {
    private String token;

    public LoginUsingTokenRequest(String token) {
        this.token = token;
    }

    @Override
    public RequestType getType() {
        return RequestType.LOGIN_USING_TOKEN;
    }

    public String getToken() {
        return token;
    }
}
