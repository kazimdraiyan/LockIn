package app.lockin.lockin.common;

// Shared UDP settings for client and server
public final class UdpConfig {
    public static final int SERVER_PORT = 5001;

    // First UDP datagram from client: BIND + session token. Binds socket to user associated with the token
    public static final String UDP_SESSION_BIND_PREFIX = "BIND ";

    private UdpConfig() {
    }
}
