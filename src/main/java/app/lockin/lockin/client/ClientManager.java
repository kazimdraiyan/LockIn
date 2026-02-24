package app.lockin.lockin.client;

import app.lockin.lockin.server.request.Request;
import app.lockin.lockin.server.response.Response;

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

    public void connect(String host, int port) throws IOException {
        Socket socket = new Socket(host, port);

        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush(); // TODO: What and why?
        in = new ObjectInputStream(socket.getInputStream());

        System.out.println("Connected to server");
    }

    public void send(Request request) throws IOException {
        out.writeObject(request);
        out.flush(); // TODO: Learn more
    }

    public Response receive() {
        // TODO: Implement
        return null;
    }

    // TODO: Add close everything method
}
