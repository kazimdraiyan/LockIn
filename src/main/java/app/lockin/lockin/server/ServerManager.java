package app.lockin.lockin.server;

import app.lockin.lockin.server.handlers.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerManager {
    private ServerSocket serverSocket;

    private final AuthHandler authHandler = new AuthHandler();
    private final PostHandler postHandler = new PostHandler();
    private final MessageHandler messageHandler = new MessageHandler();
    private final CallHandler callHandler;

    public ServerManager(ServerSocket serverSocket, CallHandler callHandler) {
        this.serverSocket = serverSocket;
        this.callHandler = callHandler;
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
                ClientHandler clientHandler = new ClientHandler(socket, authHandler, postHandler, messageHandler, callHandler);
                Thread thread = new Thread(clientHandler);
                thread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Server main is running");
        CallHandler callHandler = new CallHandler();
        new UdpServer(callHandler).start();
        ServerSocket serverSocket = new ServerSocket(5000);
        ServerManager serverManager = new ServerManager(serverSocket, callHandler);
        serverManager.startServer();
    }
}
