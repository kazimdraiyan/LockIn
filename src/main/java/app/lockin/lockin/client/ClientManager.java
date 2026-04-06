package app.lockin.lockin.client;

import app.lockin.lockin.common.models.MessageDelivery;
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
    private static final String INCOMING_MESSAGE = "Incoming message";

    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    private String serverHost;
    private UdpClient udpClient;
    // TODO: Learn more about BlockingQueue and Consumer. Seems like it's Stream type thing.
    private final BlockingQueue<Response> responseQueue = new LinkedBlockingQueue<>(); // Waits for a response to be added if empty
    private final CopyOnWriteArrayList<Consumer<MessageDelivery>> messageListeners = new CopyOnWriteArrayList<>();

    public boolean isLoggedIn = false;
    private Session authenticatedSession;

    // Returns whether successful logging in using saved token
    public void connect(String host, int port) throws IOException {
        serverHost = host;
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
    public void addMessageListener(Consumer<MessageDelivery> listener) {
        messageListeners.add(listener);
    }

    public void removeMessageListener(Consumer<MessageDelivery> listener) {
        messageListeners.remove(listener);
    }

    public String getAuthenticatedUsername() {
        return authenticatedSession == null ? null : authenticatedSession.getUsername();
    }

    public void setAuthenticatedSession(Session session) {
        authenticatedSession = session;
    }

    public void clearAuthenticatedSession() {
        stopUdpTransport();
        authenticatedSession = null;
        isLoggedIn = false;
    }

    private void startUdpTransport(Session session) {
        if (serverHost == null || serverHost.isEmpty()) {
            return;
        }
        stopUdpTransport();
        String token = session.getToken();
        if (token == null || token.isBlank()) {
            return;
        }
        udpClient = new UdpClient(serverHost, session);
        udpClient.start();
    }

    private void stopUdpTransport() {
        if (udpClient != null) {
            udpClient.stop();
            udpClient = null;
        }
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
            startUdpTransport(session);
        }

        if (response.getStatus() == ResponseStatus.SUCCESS
                && INCOMING_MESSAGE.equals(response.getMessage())
                && response.getData() instanceof MessageDelivery delivery) {
            for (Consumer<MessageDelivery> listener : messageListeners) {
                listener.accept(delivery);
            }
            return;
        }

        responseQueue.offer(response);
    }
}
