package app.lockin.lockin.server.response;

import java.io.Serializable;

// Response must also be serializable, because it also travels via sockets
// Sent by Service
public class Response implements Serializable {
    private ResponseStatus status; // enums are automatically serializable
    private String message; // Human-readable summary of the response
    private Object data; // The object must be serializable // TODO: Is Object the right type?

    public Response(ResponseStatus status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // TODO: Add getters
    public String getMessage() {
        return message;
    }
}
