package app.lockin.lockin.server;

import app.lockin.lockin.server.handler.AuthHandler;
import app.lockin.lockin.server.network.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerManager {
    private ServerSocket serverSocket;

    private AuthHandler authHandler = new AuthHandler();
    // Add other handlers like MessageHandler

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
                ClientHandler clientHandler = new ClientHandler(socket, authHandler);
                Thread thread = new Thread(clientHandler);
                thread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Server main is running");
        ServerSocket serverSocket = new ServerSocket(1234);
        ServerManager serverManager = new ServerManager(serverSocket);
        serverManager.startServer();
    }
}
