package app.lockin.lockin.client;

import app.lockin.lockin.common.models.MessageRealtimeEvent;
import app.lockin.lockin.common.models.Session;
import app.lockin.lockin.common.requests.LoginUsingTokenRequest;
import app.lockin.lockin.common.requests.Request;
import app.lockin.lockin.common.response.Response;
import app.lockin.lockin.common.response.ResponseStatus;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

// TODO: Proper exception handling in all networking related classes
// TODO: Should I rename ServerManager and ClientManager differently because one contains main and the other does not?

public class ClientManager {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    // TODO: Learn more about BlockingQueue and Consumer. Seems like it's Stream type thing.
    private final BlockingQueue<Response> responseQueue = new LinkedBlockingQueue<>(); // Waits for a response to be added if empty
    private final CopyOnWriteArrayList<Consumer<MessageRealtimeEvent>> messageListeners = new CopyOnWriteArrayList<>();

    public boolean isLoggedIn = false;
    private Session authenticatedSession;

    // Returns whether successful logging in using saved token
    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);

        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush(); // TODO: What and why?
        in = new ObjectInputStream(socket.getInputStream());
        startResponseListener();

        System.out.println("Connected to server");

        // Tries to authenticate this ClientManager's respective ClientHandler with the saved token
        String savedToken = MyApplication.loadToken();
        System.out.println("Trying to authenticate using saved token: " + savedToken);
        if (savedToken != null &&  !savedToken.isEmpty()) {
            authenticateWithToken(savedToken);
        }
    }

    public void authenticateWithToken(String token) throws IOException {
        Response response = sendRequest(new LoginUsingTokenRequest(token));
        isLoggedIn = response.getStatus() == ResponseStatus.SUCCESS;
    }

    public synchronized Response sendRequest(Request request) throws IOException {
        out.writeObject(request);
        out.flush(); // TODO: Learn more
        return receive();
    }

    public Response receive() {
        try {
            return responseQueue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new Response(ResponseStatus.ERROR, "Interrupted while waiting for server response", null);
        }
    }

    // TODO: Learn more about Consumer
    public void addMessageListener(Consumer<MessageRealtimeEvent> listener) {
        messageListeners.add(listener);
    }

    public void removeMessageListener(Consumer<MessageRealtimeEvent> listener) {
        messageListeners.remove(listener);
    }

    public String getAuthenticatedUsername() {
        return authenticatedSession == null ? null : authenticatedSession.getUsername();
    }

    public void setAuthenticatedSession(Session session) {
        authenticatedSession = session;
    }

    public void clearAuthenticatedSession() {
        authenticatedSession = null;
        isLoggedIn = false;
    }

    private void startResponseListener() {
        Thread listenerThread = new Thread(() -> {
            try {
                while (!socket.isClosed()) {
                    Response response = (Response) in.readObject();
                    dispatchResponse(response);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                responseQueue.offer(new Response(ResponseStatus.ERROR, "Connection to the server was lost", null));
            }
        });
        listenerThread.setName("lockin-client-response-listener");
        listenerThread.setDaemon(true); // Non-priority thread // TODO: Is this unnecessary?
        listenerThread.start();
    }

    private void dispatchResponse(Response response) {
        if (response.getStatus() == ResponseStatus.SUCCESS && response.getData() instanceof Session session) {
            authenticatedSession = session;
            isLoggedIn = true;
        }

        if (response.getData() instanceof MessageRealtimeEvent event) {
            for (Consumer<MessageRealtimeEvent> listener : messageListeners) {
                listener.accept(event);
            }
            return;
        }

        responseQueue.offer(response);
    }
}
