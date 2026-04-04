package app.lockin.lockin.server.handlers;

import app.lockin.lockin.common.models.Session;
import app.lockin.lockin.common.requests.*;
import app.lockin.lockin.common.response.Response;
import app.lockin.lockin.common.response.ResponseStatus;

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
    private PostHandler postHandler;

    private boolean isRunning = true;

    private Session authenticatedSession = null;

    // Dependency injection is used here by injecting AuthHandler into ClientHandler. TODO: Learn more about this
    public ClientHandler(Socket socket, AuthHandler authHandler, PostHandler postHandler) {
        this.socket = socket;
        this.authHandler = authHandler;
        this.postHandler = postHandler;
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

    private Response authenticateUsingToken(LoginUsingTokenRequest request) {
        Response response = authHandler.handleLoginUsingToken(request);
        if (response.getData() != null) {
            authenticatedSession = (Session) response.getData();
            System.out.println("Authentication successful: " + authenticatedSession.getUsername());
        }
        else {
            System.out.println("No session found corresponding to the given token");
        }
        return response;
    }

    private void handleRequest(Request request) {
        request.authenticatedSession = authenticatedSession; // Attaches the current authenticated username to every request

        Response response = null;
        Session newSession;
        switch (request.getType()) {
            case LOGIN:
                response = authHandler.handleLogin((LoginRequest) request);
                newSession = (Session) response.getData();
                authenticateUsingToken(new LoginUsingTokenRequest(newSession == null ? null : newSession.getToken()));
                break;
            case LOGIN_USING_TOKEN:
                response = authenticateUsingToken((LoginUsingTokenRequest) request);
                break;
            case LOGOUT:
                response = authHandler.handleLogout((LogoutRequest) request);
                if (response.getStatus() == ResponseStatus.SUCCESS) {
                    authenticatedSession = null;
                }
                break;
            case SIGNUP:
                response = authHandler.handleSignUp((SignUpRequest) request);
                newSession = (Session) response.getData();
                authenticateUsingToken(new LoginUsingTokenRequest(newSession == null ? null : newSession.getToken()));
                break;
            case FETCH:
                response = handleFetchRequest((FetchRequest) request);
                break;
            case CREATE_POST:
                response = postHandler.handleCreatePost((CreatePostRequest) request);
                break;
            case CREATE_COMMENT:
                response = postHandler.handleCreateComment((CreateCommentRequest) request);
                break;
            case UPDATE_PROFILE:
                response = authHandler.handleUpdateProfile((UpdateProfileRequest) request);
                break;
            case DELETE_POST:
                response = postHandler.handleDeletePost((DeletePostRequest) request);
                break;
        }
        if (response != null) {
            send(response);
        }
    }

    private Response handleFetchRequest(FetchRequest request) {
        Response response = null;
        switch (request.getFetchType()) {
            case CHATS:
                response = authHandler.handleFetchChats(request);
                break;
            case MESSAGES:
                break;
            case POSTS:
                response = postHandler.handleFetchPosts(request);
                break;
            case PROFILE:
                response = authHandler.handleFetchProfile(request, postHandler);
                break;
            case USER_SEARCH:
                response = authHandler.handleSearchUsers(request);
                break;
        }
        return response;
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
