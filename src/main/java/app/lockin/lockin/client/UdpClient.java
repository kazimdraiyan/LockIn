package app.lockin.lockin.client;

import app.lockin.lockin.common.models.Session;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import static app.lockin.lockin.common.UdpConfig.SERVER_PORT;
import static app.lockin.lockin.common.UdpConfig.UDP_SESSION_BIND_PREFIX;
import static app.lockin.lockin.common.UdpConfig.UDP_VOICE_FRAME_PREFIX;

// Minimal UDP session to the server for future voice.
// First datagram carries the TCP session token so the server can bind this endpoint to the user.
public final class UdpClient {
    private final String serverHost;
    private final Session session;

    private volatile DatagramSocket socket;
    private volatile boolean running;

    public UdpClient(String serverHost, Session session) {
        this.serverHost = serverHost;
        this.session = session;
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

    public synchronized void sendVoiceFrame(String callId, byte[] frame) throws IOException {
        if (!running || socket == null || socket.isClosed()) {
            throw new IOException("UDP client is not running");
        }
        if (callId == null || callId.isBlank()) {
            throw new IOException("Call id required");
        }
        if (frame == null || frame.length == 0) {
            return;
        }
        byte[] header = (UDP_VOICE_FRAME_PREFIX + callId + " ").getBytes(StandardCharsets.UTF_8);
        byte[] packetData = new byte[header.length + frame.length];
        System.arraycopy(header, 0, packetData, 0, header.length);
        System.arraycopy(frame, 0, packetData, header.length, frame.length);
        socket.send(new DatagramPacket(packetData, packetData.length));
    }

    private void runLoop() {
        if (session.getToken() == null || session.getToken().isBlank()) {
            return;
        }
        try {
            socket = new DatagramSocket();
            socket.connect(InetAddress.getByName(serverHost), SERVER_PORT);

            // Authenticate to UDP server using session token
            sendUtf8(UDP_SESSION_BIND_PREFIX + session.getToken());

            byte[] buffer = new byte[2048];
            while (running && socket != null && !socket.isClosed()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
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
