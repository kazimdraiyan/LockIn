package app.lockin.lockin.server.services;

import app.lockin.lockin.common.models.Chat;
import app.lockin.lockin.common.models.Session;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

// Does auth related backend tasks
public class AuthService {
    private static final long DEFAULT_SESSION_DURATION_MILLIS = 365L * 24 * 60 * 60 * 1000; // 1 year in milliseconds

    private static final String DATABASE_PATH = "database/"; // Server database should not be put inside the resources/ directory. Because it's of server side and it's not read-only.

    ObjectMapper mapper = new ObjectMapper();

    private ObjectNode loadDatabase(String filename) throws IOException {
        return (ObjectNode) mapper.readTree(new File(DATABASE_PATH + filename));
    }

    private void saveDatabase(String filename, ObjectNode database) throws IOException {
        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(
                        new File(DATABASE_PATH + filename),
                        database
                );
    }

    public Session createUser(String username, String password) throws IOException {
        // TODO: Create file when database folder, or users.json file does not exist
        // TODO: Fix when file is empty instead of {}
        ObjectNode usersDatabase = loadDatabase("users.json");

        // Check if username already exists
        if (usersDatabase.has(username)) {
            throw new IOException("Username is already in use"); // TODO: Use custom exceptions
        }

        ObjectNode user = mapper.createObjectNode();
        // TODO: Use hashes instead of storing the password in plaintext
        user.put("password", password);
        usersDatabase.set(username, user);

        System.out.println("User successfully created");

        saveDatabase("users.json", usersDatabase);

        return addSession(username);
    }

    // Token is returned
    private Session addSession(String username) throws IOException {
        ObjectNode sessionsDatabase = loadDatabase("sessions.json");

        UUID uuid = UUID.randomUUID();
        String token = uuid.toString();
        long expiresAt = System.currentTimeMillis() + DEFAULT_SESSION_DURATION_MILLIS;

        ObjectNode sessionInfo = mapper.createObjectNode();
        sessionInfo.put("username", username);
        sessionInfo.put("expiresAt", expiresAt); // Milliseconds since epoch
        sessionsDatabase.set(token, sessionInfo);

        saveDatabase("sessions.json", sessionsDatabase);

        return new Session(token, username);
    }

    // Logout
    public void removeSession(Session session) throws IOException {
        if (session == null) throw new IOException("Session is null");
        ObjectNode sessionsDatabase = loadDatabase("sessions.json");
        sessionsDatabase.remove(session.getToken());
        saveDatabase("sessions.json", sessionsDatabase);
    }

    public Session login(String username, String password) throws IOException {
        ObjectNode usersDatabase = (ObjectNode) mapper.readTree(new File(DATABASE_PATH + "users.json"));
        // In the client side, IOException should be interpreted as an unexpected server error

        if (!usersDatabase.has(username)) {
            return null; // User not found
        }

        String savedPassword = usersDatabase.get(username).get("password").asText();
        if (!savedPassword.equals(password)) {
            return null; // Password do not match
        }
        return addSession(username);
    }

    public String usernameFromToken(String token) throws IOException {
        ObjectNode sessionsDatabase = loadDatabase("sessions.json");

        if (!sessionsDatabase.has(token)) {
            return null;
        }
        return sessionsDatabase.get(token).get("username").asText();
    }

    public ArrayList<Chat> loadChats(String username) throws IOException {
        // For now loads all users
        // TODO: Implement adding friend and load only friend chats
        // TODO: Implement group chats

        ArrayList<Chat> chats = new ArrayList<>();
        ObjectNode usersDatabase = loadDatabase("users.json");
        Iterator<String> usernameIterator = usersDatabase.fieldNames();
        while (usernameIterator.hasNext()) {
            chats.add(new Chat(usernameIterator.next()));
        }
        return chats;
    }
}
