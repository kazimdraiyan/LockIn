package app.lockin.lockin.server.handlers;

import app.lockin.lockin.common.models.Chat;
import app.lockin.lockin.common.models.Message;
import app.lockin.lockin.common.models.MessageDelivery;
import app.lockin.lockin.common.models.Session;
import app.lockin.lockin.common.requests.*;
import app.lockin.lockin.common.response.Response;
import app.lockin.lockin.common.response.ResponseStatus;
import app.lockin.lockin.server.services.ConnectedClientRegistry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    private MessageHandler messageHandler;
    private CallHandler callHandler;

    private boolean isRunning = true;

    private Session authenticatedSession = null;

    // Dependency injection is used here by injecting AuthHandler, PostHandler, and MessageHandler into ClientHandler. TODO: Learn more about this
    public ClientHandler(Socket socket, AuthHandler authHandler, PostHandler postHandler, MessageHandler messageHandler, CallHandler callHandler) {
        this.socket = socket;
        this.authHandler = authHandler;
        this.postHandler = postHandler;
        this.messageHandler = messageHandler;
        this.callHandler = callHandler;
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
            e.printStackTrace();
            System.out.println("Client thread stopped due to error");
        } finally {
            clearAuthenticatedSession();
            closeResources();
        }
    }

    private Response authenticateUsingToken(LoginUsingTokenRequest request) {
        Response response = authHandler.handleLoginUsingToken(request);
        if (response.getData() != null) {
            updateAuthenticatedSession((Session) response.getData());
            System.out.println("Authentication successful: " + authenticatedSession.getUsername());
        } else {
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
                if (newSession != null) {
                    updateAuthenticatedSession(newSession);
                }
                break;
            case LOGIN_USING_TOKEN:
                response = authenticateUsingToken((LoginUsingTokenRequest) request);
                break;
            case LOGOUT:
                response = authHandler.handleLogout((LogoutRequest) request);
                if (response.getStatus() == ResponseStatus.SUCCESS) {
                    clearAuthenticatedSession();
                }
                break;
            case SIGNUP:
                response = authHandler.handleSignUp((SignUpRequest) request);
                newSession = (Session) response.getData();
                if (newSession != null) {
                    updateAuthenticatedSession(newSession);
                }
                break;
            case FETCH:
                response = handleFetchRequest((FetchRequest) request);
                break;
            case FETCH_MESSAGES:
                response = messageHandler.handleFetchMessages((FetchMessagesRequest) request);
                break;
            case CREATE_POST:
                response = postHandler.handleCreatePost((CreatePostRequest) request);
                break;
            case CREATE_COMMENT:
                response = postHandler.handleCreateComment((CreateCommentRequest) request);
                break;
            case CREATE_MESSAGE:
                handleCreateMessage((CreateMessageRequest) request);
                return;
            case UPDATE_PROFILE:
                response = authHandler.handleUpdateProfile((UpdateProfileRequest) request);
                break;
            case DELETE_POST:
                response = postHandler.handleDeletePost((DeletePostRequest) request);
                break;
            case CHANGE_PASSWORD:
                response = authHandler.handleChangePassword((ChangePasswordRequest) request);
                break;
            case START_CALL:
                response = callHandler.handleStartCall((StartCallRequest) request);
                break;
            case ANSWER_CALL:
                response = callHandler.handleAnswerCall((AnswerCallRequest) request);
                break;
            case END_CALL:
                response = callHandler.handleEndCall((EndCallRequest) request);
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
                response = messageHandler.handleFetchChats(request);
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
            case CONNECTED_USERS:
                response = authHandler.handleFetchConnectedUsers(request);
                break;
        }
        return response;
    }

    private void handleCreateMessage(CreateMessageRequest request) {
        try {
            Message message = messageHandler.handleCreateMessage(request);
            String senderUsername = request.authenticatedSession.getUsername();
            String recipientUsername = request.getRecipientUsername();
            String chatId = request.getChatId();

            if (Chat.COMMON_CHAT_ID.equals(chatId)) {
                MessageDelivery delivery = new MessageDelivery(
                        new Chat(Chat.COMMON_CHAT_ID, Chat.COMMON_CHAT_NAME, message, 0),
                        message
                );
                send(new Response(ResponseStatus.SUCCESS, "Message sent successfully", delivery));
                broadcastToAllUsers(new Response(ResponseStatus.SUCCESS, "Incoming message", delivery), this);
                return;
            }

            MessageDelivery senderDelivery = new MessageDelivery(
                    new Chat(chatId, recipientUsername, message, 0),
                    message
            );
            MessageDelivery recipientDelivery = new MessageDelivery(
                    new Chat(chatId, senderUsername, message, 0),
                    message
            );

            send(new Response(ResponseStatus.SUCCESS, "Message sent successfully", senderDelivery));

            Response senderEvent = new Response(
                    ResponseStatus.SUCCESS,
                    "Incoming message",
                    senderDelivery
            );
            Response recipientEvent = new Response(
                    ResponseStatus.SUCCESS,
                    "Incoming message",
                    recipientDelivery
            );
            broadcastToUser(senderUsername, senderEvent, this);
            broadcastToUser(recipientUsername, recipientEvent, null);
        } catch (IOException e) {
            send(new Response(ResponseStatus.ERROR, e.getMessage(), null));
        }
    }

    private void broadcastToUser(String username, Response response, ClientHandler excludedClient) {
        for (ClientHandler client : ConnectedClientRegistry.getClients(username)) {
            if (client == excludedClient) {
                continue;
            }
            client.send(response);
        }
    }

    private void broadcastToAllUsers(Response response, ClientHandler excludedClient) {
        for (ClientHandler client : ConnectedClientRegistry.getAllClients()) {
            if (client == excludedClient) {
                continue;
            }
            client.send(response);
        }
    }

    private void updateAuthenticatedSession(Session newSession) {
        clearAuthenticatedSession();
        authenticatedSession = newSession;
        if (authenticatedSession != null) {
            ConnectedClientRegistry.register(authenticatedSession.getUsername(), this);
        }
    }

    private void clearAuthenticatedSession() {
        if (authenticatedSession != null) {
            ConnectedClientRegistry.unregister(authenticatedSession.getUsername(), this);
            authenticatedSession = null;
        }
    }

    // TODO: Reasons of using synchronized here?
    public synchronized void send(Response response) {
        try {
            out.writeObject(response); // Serialize
            out.flush(); // TODO: Why it's needed?
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Client thread stopped due to error while sending a response");
            isRunning = false;
        }
    }

    private void closeResources() {
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException ignored) {
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException ignored) {
        }
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }
}
