package app.lockin.lockin.client;

import app.lockin.lockin.common.requests.LoginUsingTokenRequest;
import app.lockin.lockin.common.requests.Request;
import app.lockin.lockin.common.response.Response;
import app.lockin.lockin.common.response.ResponseStatus;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

// TODO: Proper exception handling in all networking related classes
// TODO: Should I rename ServerManager and ClientManager differently because one contains main and the other does not?

public class ClientManager {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public boolean isLoggedIn = false;
    public String username;

    // Returns whether successful logging in using saved token
    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);

        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush(); // TODO: What and why?
        in = new ObjectInputStream(socket.getInputStream());

        System.out.println("Connected to server");

        // Tries to authenticate this ClientManager's respective ClientHandler with the saved token
        String savedToken = MyApplication.loadToken();
        System.out.println("Trying to authenticate using saved token: " + savedToken);
        if (savedToken != null &&  !savedToken.isEmpty()) {
            authenticateWithToken(savedToken);
        }
    }

    public void authenticateWithToken(String token) throws IOException {
        send(new LoginUsingTokenRequest(token));
        Response response = receive();
        isLoggedIn = response.getStatus() == ResponseStatus.SUCCESS;
        username = isLoggedIn && response.getData() instanceof app.lockin.lockin.common.models.Session session
                ? session.getUsername()
                : null;
    }

    public void send(Request request) throws IOException {
        out.writeObject(request);
        out.flush(); // TODO: Learn more
    }

    public Response receive() {
        try {
            return (Response) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // TODO: Close socket and other things
            e.printStackTrace();
            System.out.println("Client manager stopped due to error");
        }
        return null;
    }

    // TODO: Add close everything method
}
