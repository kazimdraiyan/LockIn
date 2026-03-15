package app.lockin.lockin.server.network;

import app.lockin.lockin.server.handler.AuthHandler;
import app.lockin.lockin.server.request.*;
import app.lockin.lockin.server.response.Response;

import java.io.*;
import java.net.Socket;

// One ClientHandler instance is created per logged-in user
// Responsibilities: read client requests, route requests to corresponding handler class, send response to client
// Runnable is implemented when work need to be done in a separate thread
public class ClientHandler implements Runnable {
    private Socket socket;

    private ObjectInputStream in;
    private ObjectOutputStream out;

    private AuthHandler authHandler;

    private boolean isRunning = true;

    private String authenticatedUsername = null;

    // Dependency injection is used here by injecting AuthHandler into ClientHandler. TODO: Learn more about this
    public ClientHandler(Socket socket, AuthHandler authHandler) {
        this.socket = socket;
        this.authHandler = authHandler;
    }

    @Override
    public void run() {
        try {
            System.out.println("Client thread started");

            // The output stream should be accessed first // TODO: Why?
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            while (isRunning) {
                Request request = (Request) in.readObject(); // Deserialize // TODO: Is this a blocking method?
                handleRequest(request);
            }
        } catch (IOException | ClassNotFoundException e) {
            // TODO: Close socket and other things
            e.printStackTrace();
            System.out.println("Client thread stopped due to error");
        }
    }

    private void authenticateUsingToken(LoginUsingTokenRequest request) {
        Response response = authHandler.handleLoginUsingToken(request);
        if (response.getData() != null) {
            authenticatedUsername = (String) response.getData();
            System.out.println("Authentication successful: " + authenticatedUsername);
        }
        else {
            System.out.println("No session found corresponding to the given token");
        }
    }

    private void handleRequest(Request request) {
        request.authenticatedUsername = authenticatedUsername; // Attaches the current authenticated username to every request

        Response response = null;
        switch (request.getType()) {
            case LOGIN:
                response = authHandler.handleLogin((LoginRequest) request);
                authenticateUsingToken(new LoginUsingTokenRequest((String) response.getData())); // response.getData() contains the token
                break;
            case LOGIN_USING_TOKEN:
                authenticateUsingToken((LoginUsingTokenRequest) request);
                break;
            case LOGOUT:
                response = authHandler.handleLogout((LogoutRequest) request);
                isRunning = false;
                break;
            case SIGNUP:
                response = authHandler.handleSignUp((SignUpRequest) request);
                authenticateUsingToken(new LoginUsingTokenRequest((String) response.getData())); // response.getData() contains the token
                break;
        }
        if (response != null) {
            send(response);
        }
    }

    private void send(Response response) {
        try {
            out.writeObject(response); // Serialize
            out.flush(); // TODO: Why it's needed?
        } catch (IOException e) {
            // TODO: Close everything
            e.printStackTrace();
            System.out.println("Client thread stopped due to error while sending a response");
        }
    }
}
