package app.lockin.lockin.server.services;

import app.lockin.lockin.common.models.Post;
import app.lockin.lockin.common.models.PostAttachment;
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
import java.util.UUID;

public class PostService {
    private static final String DATABASE_PATH = "database";
    private static final Path POSTS_PATH = Path.of(DATABASE_PATH, "posts.json");
    private static final Path UPLOADS_PATH = Path.of(DATABASE_PATH, "uploads");
    private static final long MAX_ATTACHMENT_SIZE_BYTES = 10L * 1024 * 1024;

    private final ObjectMapper mapper = new ObjectMapper();

    public Post createPost(String username, String textContent, PostAttachment attachment) throws IOException {
        if (username == null || username.isBlank()) {
            throw new IOException("Unauthenticated request");
        }

        String normalizedText = textContent == null ? "" : textContent.trim();
        if (normalizedText.isEmpty() && attachment == null) {
            throw new IOException("Post must contain text or an attachment");
        }

        ensureStorageExists();

        ArrayNode posts = loadPostsNode();
        String postId = UUID.randomUUID().toString();
        long createdAt = System.currentTimeMillis();

        ObjectNode postNode = mapper.createObjectNode();
        postNode.put("id", postId);
        postNode.put("authorUsername", username);
        postNode.put("textContent", normalizedText);
        postNode.put("createdAt", createdAt);

        PostAttachment storedAttachment = null;
        if (attachment != null) {
            storedAttachment = validateAttachment(attachment);
            String storedFileName = postId + "_" + sanitizeFileName(storedAttachment.getOriginalFileName());
            Path storedPath = UPLOADS_PATH.resolve(storedFileName);
            Files.write(storedPath, storedAttachment.getData());

            ObjectNode attachmentNode = mapper.createObjectNode();
            attachmentNode.put("originalFileName", storedAttachment.getOriginalFileName());
            attachmentNode.put("mimeType", storedAttachment.getMimeType());
            attachmentNode.put("storedFileName", storedFileName);
            postNode.set("attachment", attachmentNode);
        }

        posts.add(postNode);
        savePostsNode(posts);

        return new Post(postId, username, normalizedText, storedAttachment, createdAt);
    }

    public ArrayList<Post> loadPosts() throws IOException {
        ensureStorageExists();
        ArrayList<Post> posts = new ArrayList<>();
        ArrayNode postsNode = loadPostsNode();

        for (JsonNode postNode : postsNode) {
            PostAttachment attachment = null;
            JsonNode attachmentNode = postNode.get("attachment");
            if (attachmentNode != null && !attachmentNode.isNull()) {
                Path filePath = UPLOADS_PATH.resolve(attachmentNode.get("storedFileName").asText());
                byte[] data = Files.exists(filePath) ? Files.readAllBytes(filePath) : new byte[0];
                attachment = new PostAttachment(
                        attachmentNode.get("originalFileName").asText(),
                        attachmentNode.get("mimeType").asText(),
                        data
                );
            }

            posts.add(new Post(
                    postNode.get("id").asText(),
                    postNode.get("authorUsername").asText(),
                    postNode.path("textContent").asText(""),
                    attachment,
                    postNode.get("createdAt").asLong()
            ));
        }

        posts.sort(Comparator.comparingLong(Post::getCreatedAt).reversed());
        return posts;
    }

    private void ensureStorageExists() throws IOException {
        Files.createDirectories(UPLOADS_PATH);
        if (!Files.exists(POSTS_PATH)) {
            Files.writeString(POSTS_PATH, "[]");
        }
    }

    private ArrayNode loadPostsNode() throws IOException {
        JsonNode node = mapper.readTree(new File(POSTS_PATH.toString()));
        if (node == null || !node.isArray()) {
            return mapper.createArrayNode();
        }
        return (ArrayNode) node;
    }

    private void savePostsNode(ArrayNode posts) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(POSTS_PATH.toString()), posts);
    }

    private PostAttachment validateAttachment(PostAttachment attachment) throws IOException {
        if (attachment.getOriginalFileName() == null || attachment.getOriginalFileName().isBlank()) {
            throw new IOException("Attachment name is missing");
        }
        if (attachment.getData().length == 0) {
            throw new IOException("Attachment is empty");
        }
        if (attachment.getData().length > MAX_ATTACHMENT_SIZE_BYTES) {
            throw new IOException("Attachment is too large");
        }

        String mimeType = normalizeMimeType(attachment.getMimeType(), attachment.getOriginalFileName());
        if (!isAllowedMimeType(mimeType)) {
            throw new IOException("Unsupported file type");
        }

        return new PostAttachment(attachment.getOriginalFileName(), mimeType, attachment.getData());
    }

    private String normalizeMimeType(String mimeType, String fileName) {
        if (mimeType != null && !mimeType.isBlank() && !"application/octet-stream".equals(mimeType)) {
            return mimeType;
        }

        String lowerFileName = fileName.toLowerCase();
        if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) {
            return "image/jpeg";
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

    private boolean isAllowedMimeType(String mimeType) {
        return "image/jpeg".equals(mimeType)
                || "image/gif".equals(mimeType)
                || "application/pdf".equals(mimeType)
                || "text/plain".equals(mimeType);
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
