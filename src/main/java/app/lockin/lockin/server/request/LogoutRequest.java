package app.lockin.lockin.server.request;

public class LogoutRequest implements Request {
    @Override
    public RequestType getType() {
        return RequestType.LOGOUT;
    }
}
