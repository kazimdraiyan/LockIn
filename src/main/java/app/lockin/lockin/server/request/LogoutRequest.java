package app.lockin.lockin.server.request;

public class LogoutRequest extends Request {
    @Override
    public RequestType getType() {
        return RequestType.LOGOUT;
    }
}
