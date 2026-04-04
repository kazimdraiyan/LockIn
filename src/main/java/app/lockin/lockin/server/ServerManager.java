package app.lockin.lockin.server;

import app.lockin.lockin.server.handlers.AuthHandler;
import app.lockin.lockin.server.handlers.ClientHandler;
import app.lockin.lockin.server.handlers.MessageHandler;
import app.lockin.lockin.server.handlers.PostHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerManager {
    private ServerSocket serverSocket;

    private AuthHandler authHandler = new AuthHandler();
    private PostHandler postHandler = new PostHandler();
    private MessageHandler messageHandler = new MessageHandler();

    public ServerManager(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        while (!serverSocket.isClosed()) {
            // Server is running
            try {
                Socket socket = serverSocket.accept(); // Blocking method
                // New client found and connected
                System.out.println("New client found and connected");

                // TODO: Create thread per client
                // Each thread should manage one logged-in / trying to login client
                ClientHandler clientHandler = new ClientHandler(socket, authHandler, postHandler, messageHandler);
                Thread thread = new Thread(clientHandler);
                thread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Server main is running");
        ServerSocket serverSocket = new ServerSocket(5000);
        ServerManager serverManager = new ServerManager(serverSocket);
        serverManager.startServer();
    }
}
