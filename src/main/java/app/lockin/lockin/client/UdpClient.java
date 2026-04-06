package app.lockin.lockin.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import static app.lockin.lockin.common.UdpConfig.SERVER_PORT;

/**
 * Minimal UDP session to the server for future voice. Handshake + ping/pong only.
 */
public final class UdpClient {
    private final String serverHost;
    private final String username;

    private volatile DatagramSocket socket;
    private volatile boolean running;

    public UdpClient(String serverHost, String username) {
        this.serverHost = serverHost;
        this.username = username;
    }

    public void start() {
        if (running) {
            return;
        }
        running = true;
        Thread thread = new Thread(this::runLoop, "lockin-udp-client");
        thread.setDaemon(true); // TODO: is unnecessary?
        thread.start();
    }

    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    private void runLoop() {
        try {
            socket = new DatagramSocket();
            socket.connect(InetAddress.getByName(serverHost), SERVER_PORT);
            String helloUser = username == null || username.isBlank() ? "anon" : username;
            sendUtf8("HELLO " + helloUser);

            byte[] buffer = new byte[2048];
            while (running && socket != null && !socket.isClosed()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String text = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8).trim();
                if (text.startsWith("HELLO_ACK")) {
                    System.out.println("UDP " + text);
                    sendUtf8("PING");
                } else if ("PONG".equals(text)) {
                    System.out.println("UDP PONG");
                }
            }
        } catch (IOException e) {
            if (running) {
                e.printStackTrace();
            }
        }
    }

    private void sendUtf8(String text) throws IOException {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        socket.send(new DatagramPacket(data, data.length));
    }
}
