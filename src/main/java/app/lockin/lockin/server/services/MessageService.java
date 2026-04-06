package app.lockin.lockin.server.services;

import app.lockin.lockin.common.models.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MessageService {
    private static final String DATABASE_PATH = "database";
    private static final Path USERS_PATH = Path.of(DATABASE_PATH, "users.json");
    private static final Path MESSAGES_PATH = Path.of(DATABASE_PATH, "messages.json");
    private static final Path UPLOADS_PATH = Path.of(DATABASE_PATH, "message_uploads");
    private static final long MAX_ATTACHMENT_SIZE_BYTES = 10L * 1024 * 1024;

    private final ObjectMapper mapper = new ObjectMapper();

    public ArrayList<Chat> loadChats(String username) throws IOException {
        if (username == null || username.isBlank()) {
            throw new IOException("Unauthenticated request");
        }

        ensureStorageExists();
        ObjectNode usersNode = loadUsersNode();
        ArrayNode conversationsNode = loadConversationsNode();

        ArrayList<Chat> chats = new ArrayList<>();
        Iterator<String> usernames = usersNode.fieldNames();
        while (usernames.hasNext()) {
            String otherUsername = usernames.next();
            if (username.equals(otherUsername)) {
                continue;
            }

            JsonNode conversationNode = findConversationNode(conversationsNode, username, otherUsername);
            if (conversationNode == null) {
                chats.add(new Chat(null, otherUsername, null, 0));
                continue;
            }

            Message lastMessage = loadLastMessage(conversationNode);
            chats.add(new Chat(
                    conversationNode.path("id").asText(null),
                    otherUsername,
                    lastMessage,
                    0
            ));
        }

        chats.sort(Comparator
                .comparingLong((Chat chat) -> chat.getLastMessage() == null ? Long.MIN_VALUE : chat.getLastMessage().getCreatedAt())
                .reversed()
                .thenComparing(chat -> chat.getName().toLowerCase(Locale.ENGLISH)));
        return chats;
    }

    public ConversationData loadConversation(String username, String otherUsername) throws IOException {
        if (username == null || username.isBlank()) {
            throw new IOException("Unauthenticated request");
        }
        if (otherUsername == null || otherUsername.isBlank()) {
            throw new IOException("Recipient is missing");
        }
        if (!userExists(otherUsername)) {
            throw new IOException("User not found");
        }

        ensureStorageExists();
        ArrayNode conversationsNode = loadConversationsNode();
        JsonNode conversationNode = findConversationNode(conversationsNode, username, otherUsername);

        if (conversationNode == null) {
            // TODO: Return null instead?
            return new ConversationData(new Chat(null, otherUsername, null, 0), new ArrayList<>());
        }

        ArrayList<Message> messages = loadMessages(conversationNode);
        Message lastMessage = messages.isEmpty() ? null : messages.getLast();
        Chat chat = new Chat(conversationNode.path("id").asText(null), otherUsername, lastMessage, 0);
        return new ConversationData(chat, messages);
    }

    public Message createMessage(
            String senderUsername,
            String recipientUsername,
            String text,
            MessageAttachment attachment,
            String replyOf
    ) throws IOException {
        if (senderUsername == null || senderUsername.isBlank()) {
            throw new IOException("Unauthenticated request");
        }
        if (recipientUsername == null || recipientUsername.isBlank()) {
            throw new IOException("Recipient is missing");
        }
        if (senderUsername.equals(recipientUsername)) {
            throw new IOException("You cannot message yourself");
        }
        if (!userExists(recipientUsername)) {
            throw new IOException("Recipient not found");
        }

        String normalizedText = text == null ? "" : text.trim();
        if (normalizedText.isEmpty() && attachment == null) {
            throw new IOException("Message must contain text or an attachment");
        }

        ensureStorageExists();
        ArrayNode conversationsNode = loadConversationsNode();
        ObjectNode conversationNode = ensureConversationNode(conversationsNode, senderUsername, recipientUsername);
        ArrayNode messagesNode = ensureMessagesNode(conversationNode);

        if (replyOf != null && !replyOf.isBlank() && !messageExists(messagesNode, replyOf)) {
            throw new IOException("Reply target was not found");
        }

        String chatId = conversationNode.path("id").asText();
        String messageId = UUID.randomUUID().toString();
        long createdAt = System.currentTimeMillis();

        ObjectNode messageNode = mapper.createObjectNode();
        messageNode.put("id", messageId);
        messageNode.put("senderUsername", senderUsername);
        messageNode.put("text", normalizedText);
        messageNode.put("createdAt", createdAt);
        if (replyOf != null && !replyOf.isBlank()) {
            messageNode.put("replyOf", replyOf);
        }

        MessageAttachment storedAttachment = null;
        if (attachment != null) {
            storedAttachment = validateAttachment(attachment);
            messageNode.set("attachment", storeAttachment(messageId, storedAttachment));
        }

        messagesNode.add(messageNode);
        conversationNode.put("updatedAt", createdAt);
        saveConversationsNode(conversationsNode);

        return new Message(messageId, chatId, senderUsername, normalizedText, storedAttachment, createdAt, replyOf);
    }

    private void ensureStorageExists() throws IOException {
        Files.createDirectories(UPLOADS_PATH);
        if (!Files.exists(MESSAGES_PATH)) {
            Files.writeString(MESSAGES_PATH, "[]");
        }
    }

    private ObjectNode loadUsersNode() throws IOException {
        JsonNode node = mapper.readTree(new File(USERS_PATH.toString()));
        if (node instanceof ObjectNode objectNode) {
            return objectNode;
        }
        return mapper.createObjectNode();
    }

    private ArrayNode loadConversationsNode() throws IOException {
        JsonNode node = mapper.readTree(new File(MESSAGES_PATH.toString()));
        if (node instanceof ArrayNode arrayNode) {
            return arrayNode;
        }
        return mapper.createArrayNode();
    }

    private void saveConversationsNode(ArrayNode conversations) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(MESSAGES_PATH.toString()), conversations);
    }

    private boolean userExists(String username) throws IOException {
        if (username == null || username.isBlank()) {
            return false;
        }
        return loadUsersNode().has(username);
    }

    private JsonNode findConversationNode(ArrayNode conversationsNode, String firstUsername, String secondUsername) {
        for (JsonNode conversationNode : conversationsNode) {
            JsonNode participantsNode = conversationNode.get("participants");
            if (participantsNode == null || !participantsNode.isArray() || participantsNode.size() != 2) {
                continue;
            }

            String participantOne = participantsNode.get(0).asText();
            String participantTwo = participantsNode.get(1).asText();
            if ((participantOne.equals(firstUsername) && participantTwo.equals(secondUsername))
                    || (participantOne.equals(secondUsername) && participantTwo.equals(firstUsername))) {
                return conversationNode;
            }
        }
        return null;
    }

    private ObjectNode ensureConversationNode(ArrayNode conversationsNode, String firstUsername, String secondUsername) {
        JsonNode existingNode = findConversationNode(conversationsNode, firstUsername, secondUsername);
        if (existingNode instanceof ObjectNode objectNode) {
            return objectNode;
        }

        ArrayList<String> participants = new ArrayList<>(List.of(firstUsername, secondUsername));
        participants.sort(String::compareToIgnoreCase);

        ObjectNode conversationNode = mapper.createObjectNode();
        conversationNode.put("id", UUID.randomUUID().toString());
        ArrayNode participantsNode = mapper.createArrayNode();
        participants.forEach(participantsNode::add);
        conversationNode.set("participants", participantsNode);
        conversationNode.set("messages", mapper.createArrayNode());
        conversationNode.put("updatedAt", 0L);
        conversationsNode.add(conversationNode);
        return conversationNode;
    }

    private ArrayNode ensureMessagesNode(ObjectNode conversationNode) {
        JsonNode messagesNode = conversationNode.get("messages");
        if (messagesNode instanceof ArrayNode arrayNode) {
            return arrayNode;
        }

        ArrayNode arrayNode = mapper.createArrayNode();
        conversationNode.set("messages", arrayNode);
        return arrayNode;
    }

    private boolean messageExists(ArrayNode messagesNode, String messageId) {
        for (JsonNode messageNode : messagesNode) {
            if (messageId.equals(messageNode.path("id").asText())) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<Message> loadMessages(JsonNode conversationNode) throws IOException {
        ArrayList<Message> messages = new ArrayList<>();
        JsonNode messagesNode = conversationNode.get("messages");
        if (messagesNode == null || !messagesNode.isArray()) {
            return messages;
        }

        String chatId = conversationNode.path("id").asText(null);
        for (JsonNode messageNode : messagesNode) {
            messages.add(new Message(
                    messageNode.path("id").asText(),
                    chatId,
                    messageNode.path("senderUsername").asText(),
                    messageNode.path("text").asText(""),
                    loadAttachment(messageNode.get("attachment")),
                    messageNode.path("createdAt").asLong(),
                    messageNode.path("replyOf").asText(null)
            ));
        }

        messages.sort(Comparator.comparingLong(Message::getCreatedAt));
        return messages;
    }

    private Message loadLastMessage(JsonNode conversationNode) throws IOException {
        ArrayList<Message> messages = loadMessages(conversationNode);
        return messages.isEmpty() ? null : messages.getLast();
    }

    private MessageAttachment loadAttachment(JsonNode attachmentNode) throws IOException {
        if (attachmentNode == null || attachmentNode.isNull()) {
            return null;
        }

        Path filePath = UPLOADS_PATH.resolve(attachmentNode.path("storedFileName").asText());
        byte[] data = Files.exists(filePath) ? Files.readAllBytes(filePath) : new byte[0];
        return new MessageAttachment(
                attachmentNode.path("originalFileName").asText(),
                attachmentNode.path("mimeType").asText("application/octet-stream"),
                data
        );
    }

    private ObjectNode storeAttachment(String ownerId, MessageAttachment attachment) throws IOException {
        String storedFileName = ownerId + "_" + sanitizeFileName(attachment.getOriginalFileName());
        Path storedPath = UPLOADS_PATH.resolve(storedFileName);
        Files.write(storedPath, attachment.getData());

        ObjectNode attachmentNode = mapper.createObjectNode();
        attachmentNode.put("originalFileName", attachment.getOriginalFileName());
        attachmentNode.put("mimeType", attachment.getMimeType());
        attachmentNode.put("storedFileName", storedFileName);
        return attachmentNode;
    }

    private MessageAttachment validateAttachment(MessageAttachment attachment) throws IOException {
        if (attachment.getOriginalFileName() == null || attachment.getOriginalFileName().isBlank()) {
            throw new IOException("Attachment name is missing");
        }
        if (attachment.getData() == null || attachment.getData().length == 0) {
            throw new IOException("Attachment is empty");
        }
        if (attachment.getData().length > MAX_ATTACHMENT_SIZE_BYTES) {
            throw new IOException("Attachment is too large");
        }

        String mimeType = normalizeMimeType(attachment.getMimeType(), attachment.getOriginalFileName());
        return new MessageAttachment(attachment.getOriginalFileName(), mimeType, attachment.getData());
    }

    private String normalizeMimeType(String mimeType, String fileName) {
        if (mimeType != null && !mimeType.isBlank() && !"application/octet-stream".equals(mimeType)) {
            return mimeType;
        }

        String lowerFileName = fileName.toLowerCase(Locale.ENGLISH);
        if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lowerFileName.endsWith(".png")) {
            return "image/png";
        }
        if (lowerFileName.endsWith(".gif")) {
            return "image/gif";
        }
        if (lowerFileName.endsWith(".pdf")) {
            return "application/pdf";
        }
        if (lowerFileName.endsWith(".txt")) {
            return "text/plain";
        }
        return "application/octet-stream";
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

}
