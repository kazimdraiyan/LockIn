package app.lockin.lockin.common.requests;

public class LogoutRequest extends Request {
    @Override
    public RequestType getType() {
        return RequestType.LOGOUT;
    }
}
