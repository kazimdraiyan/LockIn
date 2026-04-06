package app.lockin.lockin.server;

import app.lockin.lockin.server.services.AuthService;
import app.lockin.lockin.server.services.UdpEndpointRegistry;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

import static app.lockin.lockin.common.UdpConfig.SERVER_PORT;

public final class UdpServer {
    private final AuthService authService = new AuthService();
    private final AtomicLong nextClientId = new AtomicLong(); // TODO: Find easier alternatives to this
    private volatile DatagramSocket socket; // TODO: Change volatile

    public void start() {
        try {
            socket = new DatagramSocket(SERVER_PORT);
        } catch (IOException e) {
            throw new RuntimeException("UDP relay failed to bind on port " + SERVER_PORT, e);
        }
        Thread thread = new Thread(this::receiveLoop, "lockin-udp-relay");
        thread.setDaemon(true);
        thread.start();
        System.out.println("UDP relay listening on port " + SERVER_PORT);
    }

    private void receiveLoop() {
        byte[] buffer = new byte[2048];
        while (socket != null && !socket.isClosed()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                InetSocketAddress remote = (InetSocketAddress) packet.getSocketAddress();
                String text = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8).trim();
                handlePacket(remote, text);
            } catch (IOException e) {
                if (socket != null && !socket.isClosed()) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private void handlePacket(InetSocketAddress remote, String text) {
        if (text.startsWith("HELLO ")) {
            String token = text.substring(6).trim();
            if (token.isEmpty()) {
                return;
            }
            String username;
            try {
                username = authService.usernameFromToken(token);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            if (username == null) {
                System.out.println("UDP HELLO rejected: invalid token from " + remote);
                return;
            }
            UdpEndpointRegistry.bind(username, remote);
            long id = nextClientId.incrementAndGet();
            System.out.println("UDP HELLO bound " + username + " (" + remote + ") id=" + id);
            send(remote, "HELLO_ACK " + id);
        } else if ("PING".equals(text)) {
            send(remote, "PONG");
        }
    }

    private void send(InetSocketAddress remote, String payload) {
        try {
            byte[] data = payload.getBytes(StandardCharsets.UTF_8);
            socket.send(new DatagramPacket(data, data.length, remote));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
