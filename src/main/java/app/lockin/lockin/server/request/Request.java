package app.lockin.lockin.server.request;

import java.io.Serializable;

// TODO: Should I move Request and Response classes to neutral package from server package, because client also uses the classes?

// Serializable means convertible to a stream of bytes. Request need to be serializable, because we send stream of bytes via the sockets.
// Sent by client
public abstract class Request implements Serializable {
    public String authenticatedUsername = null;

    public abstract RequestType getType();
}
