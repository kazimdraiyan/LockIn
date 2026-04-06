package app.lockin.lockin.server.services;

import app.lockin.lockin.server.handlers.ClientHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public final class ConnectedClientRegistry {
    // TODO: Is CopyOnWriteArraySet, ConcurrentHashMap be replaceable with simpler data structures? If not, learn more about them
    private static final Map<String, CopyOnWriteArraySet<ClientHandler>> CLIENTS = new ConcurrentHashMap<>();

    public static void register(String username, ClientHandler clientHandler) {
        if (username == null || username.isBlank() || clientHandler == null) {
            return;
        }
        CLIENTS.computeIfAbsent(username, ignored -> new CopyOnWriteArraySet<>()).add(clientHandler);
    }

    public static void unregister(String username, ClientHandler clientHandler) {
        if (username == null || username.isBlank() || clientHandler == null) {
            return;
        }

        CopyOnWriteArraySet<ClientHandler> handlers = CLIENTS.get(username);
        if (handlers == null) {
            return;
        }

        handlers.remove(clientHandler);
        if (handlers.isEmpty()) {
            CLIENTS.remove(username, handlers);
        }
    }

    public static ArrayList<ClientHandler> getClients(String username) {
        CopyOnWriteArraySet<ClientHandler> handlers = CLIENTS.get(username);
        return handlers == null ? new ArrayList<>() : new ArrayList<>(handlers);
    }

    public static ArrayList<ClientHandler> getAllClients() {
        ArrayList<ClientHandler> allClientHandlers = new ArrayList<>();
        for (CopyOnWriteArraySet<ClientHandler> clientHandlers : CLIENTS.values()) {
            allClientHandlers.addAll(clientHandlers);
        }
        return allClientHandlers;
    }

    public static ArrayList<String> getConnectedUsernames() {
        ArrayList<String> usernames = new ArrayList<>(CLIENTS.keySet());
        Collections.sort(usernames, String.CASE_INSENSITIVE_ORDER);
        return usernames;
    }
}
