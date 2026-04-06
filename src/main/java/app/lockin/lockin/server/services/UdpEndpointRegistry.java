package app.lockin.lockin.server.services;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

// Maps UDP endpoints to usernames for relay and lookups.
public final class UdpEndpointRegistry {
    private static final ConcurrentHashMap<String, InetSocketAddress> USERNAME_TO_ENDPOINT = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<InetSocketAddress, String> ENDPOINT_TO_USERNAME = new ConcurrentHashMap<>();

    public static void bind(String username, InetSocketAddress endpoint) {
        InetSocketAddress previous = USERNAME_TO_ENDPOINT.put(username, endpoint);
        if (previous != null && !previous.equals(endpoint)) {
            ENDPOINT_TO_USERNAME.remove(previous);
        }
        ENDPOINT_TO_USERNAME.put(endpoint, username);
    }

    public static void unbind(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        InetSocketAddress endpoint = USERNAME_TO_ENDPOINT.remove(username);
        if (endpoint != null) {
            ENDPOINT_TO_USERNAME.remove(endpoint);
        }
    }

    public static String usernameAt(InetSocketAddress endpoint) {
        return ENDPOINT_TO_USERNAME.get(endpoint);
    }

    public static InetSocketAddress endpointFor(String username) {
        return USERNAME_TO_ENDPOINT.get(username);
    }

    private UdpEndpointRegistry() {
    }
}
