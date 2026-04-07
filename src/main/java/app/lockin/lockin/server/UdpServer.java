package app.lockin.lockin.server;

import app.lockin.lockin.server.handlers.CallHandler;
import app.lockin.lockin.server.services.AuthService;
import app.lockin.lockin.server.services.UdpEndpointRegistry;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static app.lockin.lockin.common.UdpConfig.SERVER_PORT;
import static app.lockin.lockin.common.UdpConfig.UDP_SESSION_BIND_PREFIX;
import static app.lockin.lockin.common.UdpConfig.UDP_VOICE_FRAME_PREFIX;

public final class UdpServer {
    private final AuthService authService = new AuthService();
    private final CallHandler callHandler;
    private final byte[] voicePrefixBytes = UDP_VOICE_FRAME_PREFIX.getBytes(StandardCharsets.UTF_8);
    private volatile DatagramSocket socket; // TODO: Change volatile

    public UdpServer(CallHandler callHandler) {
        this.callHandler = callHandler;
    }

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
                handlePacket(remote, packet.getData(), packet.getLength());
            } catch (IOException e) {
                if (socket != null && !socket.isClosed()) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private void handlePacket(InetSocketAddress remote, byte[] data, int length) {
        String text = new String(data, 0, length, StandardCharsets.UTF_8);
        if (text.startsWith(UDP_SESSION_BIND_PREFIX)) {
            String token = text.substring(UDP_SESSION_BIND_PREFIX.length()).trim();
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
                System.out.println("UDP session bind rejected: invalid token from " + remote);
                return;
            }
            UdpEndpointRegistry.bind(username, remote);
            return;
        }

        if (startsWith(data, length, voicePrefixBytes)) {
            int callIdStart = voicePrefixBytes.length;
            int callIdEnd = -1;
            for (int i = callIdStart; i < length; i++) {
                if (data[i] == ' ') {
                    callIdEnd = i;
                    break;
                }
            }
            if (callIdEnd <= callIdStart) {
                return;
            }
            String callId = new String(data, callIdStart, callIdEnd - callIdStart, StandardCharsets.UTF_8);
            String senderUsername = UdpEndpointRegistry.usernameAt(remote);
            if (senderUsername == null) {
                return;
            }
            String peerUsername = callHandler.peerInActiveCall(callId, senderUsername);
            if (peerUsername == null) {
                return;
            }
            InetSocketAddress peerEndpoint = UdpEndpointRegistry.endpointFor(peerUsername);
            if (peerEndpoint == null) {
                return;
            }
            forward(peerEndpoint, data, length);
            return;
        }
    }

    private boolean startsWith(byte[] data, int length, byte[] prefix) {
        if (length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    // TODO: Rename payload
    private void forward(InetSocketAddress remote, byte[] payload, int length) {
        try {
            socket.send(new DatagramPacket(payload, length, remote));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
