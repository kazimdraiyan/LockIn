package app.lockin.lockin.server.services;

import app.lockin.lockin.common.models.Chat;
import app.lockin.lockin.common.models.PostAttachment;
import app.lockin.lockin.common.models.Session;
import app.lockin.lockin.common.models.UserProfile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

// Does auth related backend tasks
public class AuthService {
    private static final long DEFAULT_SESSION_DURATION_MILLIS = 365L * 24 * 60 * 60 * 1000; // 1 year in milliseconds

    private static final String DATABASE_PATH = "database/"; // Server database should not be put inside the resources/ directory. Because it's of server side and it's not read-only.
    private static final Path PROFILE_IMAGES_PATH = Path.of("database", "profile_images");
    private static final Path USERS_PATH = Path.of("database", "users.json");
    private static final Path SESSIONS_PATH = Path.of("database", "sessions.json");

    ObjectMapper mapper = new ObjectMapper();

    private void ensureStorageExists() throws IOException {
        Files.createDirectories(PROFILE_IMAGES_PATH);
        if (!Files.exists(USERS_PATH)) {
            Files.writeString(USERS_PATH, "{}");
        }
        if (!Files.exists(SESSIONS_PATH)) {
            Files.writeString(SESSIONS_PATH, "{}");
        }
    }

    private ObjectNode loadDatabase(String filename) throws IOException {
        ensureStorageExists();
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
        user.put("description", "");
        user.putNull("profilePicture");
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

    public UserProfile loadProfile(String username) throws IOException {
        ObjectNode usersDatabase = loadDatabase("users.json");
        if (!usersDatabase.has(username)) {
            throw new IOException("User not found");
        }

        ObjectNode userNode = (ObjectNode) usersDatabase.get(username);
        return new UserProfile(
                username,
                userNode.path("description").asText(""),
                loadAttachment(userNode.get("profilePicture"))
        );
    }

    public ArrayList<UserProfile> searchUsers(String query, String authenticatedUsername) throws IOException {
        ArrayList<UserProfile> matches = new ArrayList<>();
        ObjectNode usersDatabase = loadDatabase("users.json");
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ENGLISH);

        Iterator<String> usernameIterator = usersDatabase.fieldNames();
        while (usernameIterator.hasNext()) {
            String username = usernameIterator.next();
            if (authenticatedUsername != null && authenticatedUsername.equals(username)) {
                continue;
            }
            if (!normalizedQuery.isEmpty() && !username.toLowerCase(Locale.ENGLISH).contains(normalizedQuery)) {
                continue;
            }
            matches.add(loadProfile(username));
        }

        matches.sort(Comparator.comparing(UserProfile::getUsername, String.CASE_INSENSITIVE_ORDER));
        return matches;
    }

    public UserProfile updateProfile(String username, String description, PostAttachment profilePicture) throws IOException {
        ObjectNode usersDatabase = loadDatabase("users.json");
        if (!usersDatabase.has(username)) {
            throw new IOException("User not found");
        }

        ObjectNode userNode = (ObjectNode) usersDatabase.get(username);
        userNode.put("description", description == null ? "" : description.trim());
        if (profilePicture == null) {
            System.out.println("Meowl");
            userNode.putNull("profilePicture");
            deleteAttachmentIfPresent(userNode.get("profilePicture"));
        } else {
            validateProfilePicture(profilePicture);
            String fileName = username + "." + Arrays.stream(profilePicture.getOriginalFileName().split("\\.")).toList().getLast();
            userNode.set("profilePicture", storeAttachment(fileName, profilePicture));
        }
        saveDatabase("users.json", usersDatabase);
        return new UserProfile(username, userNode.path("description").asText(""), loadAttachment(userNode.get("profilePicture")));
    }

    private void validateProfilePicture(PostAttachment profilePicture) throws IOException {
        String mimeType = profilePicture.getMimeType();
        if (!"image/jpeg".equals(mimeType) && !"image/png".equals(mimeType) && !"image/gif".equals(mimeType)) {
            throw new IOException("Profile picture must be JPG, PNG, or GIF");
        }
        if (profilePicture.getData().length == 0) {
            throw new IOException("Profile picture is empty");
        }
        if (profilePicture.getData().length > 10L * 1024 * 1024) {
            throw new IOException("Profile picture is too large");
        }
    }

    private ObjectNode storeAttachment(String attachmentId, PostAttachment attachment) throws IOException {
        Path storedPath = PROFILE_IMAGES_PATH.resolve(attachmentId);
        Files.write(storedPath, attachment.getData());

        ObjectNode attachmentNode = mapper.createObjectNode();
        attachmentNode.put("originalFileName", attachment.getOriginalFileName());
        attachmentNode.put("mimeType", attachment.getMimeType());
        attachmentNode.put("storedFileName", attachmentId);
        return attachmentNode;
    }

    private PostAttachment loadAttachment(com.fasterxml.jackson.databind.JsonNode attachmentNode) throws IOException {
        if (attachmentNode == null || attachmentNode.isNull()) {
            return null;
        }
        Path filePath = PROFILE_IMAGES_PATH.resolve(attachmentNode.get("storedFileName").asText());
        byte[] data = Files.exists(filePath) ? Files.readAllBytes(filePath) : new byte[0];
        return new PostAttachment(
                attachmentNode.path("originalFileName").asText("profile-picture"),
                attachmentNode.path("mimeType").asText("image/png"),
                data
        );
    }

    private void deleteAttachmentIfPresent(JsonNode attachmentNode) throws IOException {
        if (attachmentNode == null || attachmentNode.isNull()) {
            return;
        }
        Path filePath = PROFILE_IMAGES_PATH.resolve(attachmentNode.path("storedFileName").asText());
        Files.deleteIfExists(filePath);
    }
}
