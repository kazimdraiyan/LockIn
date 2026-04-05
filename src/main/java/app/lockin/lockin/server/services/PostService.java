package app.lockin.lockin.server.services;

import app.lockin.lockin.common.models.Comment;
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
import java.util.List;
import java.util.UUID;

public class PostService {
    private static final String DATABASE_PATH = "database";
    private static final Path POSTS_PATH = Path.of(DATABASE_PATH, "posts.json");
    private static final Path UPLOADS_PATH = Path.of(DATABASE_PATH, "uploads");
    private static final long MAX_ATTACHMENT_SIZE_BYTES = 10L * 1024 * 1024;

    private final ObjectMapper mapper = new ObjectMapper();

    private void ensureStorageExists() throws IOException {
        Files.createDirectories(UPLOADS_PATH);
        if (!Files.exists(POSTS_PATH)) {
            Files.writeString(POSTS_PATH, "[]");
        }
    }

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
        postNode.set("comments", mapper.createArrayNode());

        PostAttachment storedAttachment = null;
        if (attachment != null) {
            storedAttachment = validateAttachment(attachment);
            ObjectNode attachmentNode = storeAttachment(postId, storedAttachment);
            postNode.set("attachment", attachmentNode);
        }

        posts.add(postNode);
        savePostsNode(posts);

        return new Post(postId, username, normalizedText, storedAttachment, createdAt, List.of());
    }


    public ArrayList<Post> loadPosts() throws IOException {
        ensureStorageExists();
        ArrayList<Post> posts = new ArrayList<>();
        ArrayNode postsNode = loadPostsNode();

        for (JsonNode postNode : postsNode) {
            PostAttachment attachment = loadAttachment(postNode.get("attachment"));
            ArrayList<Comment> comments = loadComments(postNode.get("comments"), postNode.path("id").asText());

            posts.add(new Post(
                    postNode.get("id").asText(),
                    postNode.get("authorUsername").asText(),
                    postNode.path("textContent").asText(""),
                    attachment,
                    postNode.get("createdAt").asLong(),
                    comments
            ));
        }

        posts.sort(Comparator.comparingLong(Post::getCreatedAt).reversed());
        return posts;
    }

    public ArrayList<Post> loadPostsByAuthor(String username) throws IOException {
        ArrayList<Post> ownPosts = new ArrayList<>();
        for (Post post : loadPosts()) {
            if (username.equals(post.getAuthorUsername())) {
                ownPosts.add(post);
            }
        }
        return ownPosts;
    }

    public void deletePost(String username, String postId) throws IOException {
        if (username == null || username.isBlank()) {
            throw new IOException("Unauthenticated request");
        }
        if (postId == null || postId.isBlank()) {
            throw new IOException("Missing post id");
        }

        ensureStorageExists();
        ArrayNode posts = loadPostsNode();
        for (int i = 0; i < posts.size(); i++) {
            JsonNode postNode = posts.get(i);
            if (!postId.equals(postNode.path("id").asText())) {
                continue;
            }
            if (!username.equals(postNode.path("authorUsername").asText())) {
                throw new IOException("You can only delete your own posts");
            }

            deleteAttachmentIfPresent(postNode.get("attachment"));
            JsonNode commentsNode = postNode.get("comments");
            if (commentsNode != null && commentsNode.isArray()) {
                for (JsonNode commentNode : commentsNode) {
                    deleteAttachmentIfPresent(commentNode.get("attachment"));
                }
            }
            posts.remove(i);
            savePostsNode(posts);
            return;
        }
        throw new IOException("Post not found");
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

    public Comment createComment(String username, String postId, String textContent, PostAttachment attachment) throws IOException {
        String normalizedText = textContent == null ? "" : textContent.trim();
        if (normalizedText.isEmpty() && attachment == null) {
            throw new IOException("Comment must contain text or an attachment");
        }

        ensureStorageExists();
        ArrayNode posts = loadPostsNode();

        for (JsonNode postNode : posts) {
            if (!postId.equals(postNode.path("id").asText())) {
                continue;
            }

            ArrayNode commentsNode = ensureCommentsArray(postNode);
            String commentId = UUID.randomUUID().toString();
            long createdAt = System.currentTimeMillis();

            ObjectNode commentNode = mapper.createObjectNode();
            commentNode.put("id", commentId);
            commentNode.put("postId", postId);
            commentNode.put("authorUsername", username);
            commentNode.put("textContent", normalizedText);
            commentNode.put("createdAt", createdAt);

            PostAttachment storedAttachment = null;
            if (attachment != null) {
                storedAttachment = validateAttachment(attachment);
                ObjectNode attachmentNode = storeAttachment(commentId, storedAttachment);
                commentNode.set("attachment", attachmentNode);
            }

            commentsNode.add(commentNode);
            savePostsNode(posts);

            return new Comment(commentId, postId, username, normalizedText, storedAttachment, createdAt);
        }

        throw new IOException("Post not found");
    }

    private ArrayList<Comment> loadComments(JsonNode commentsNode, String postId) throws IOException {
        ArrayList<Comment> comments = new ArrayList<>();
        if (commentsNode == null || !commentsNode.isArray()) {
            return comments;
        }

        for (JsonNode commentNode : commentsNode) {
            comments.add(new Comment(
                    commentNode.path("id").asText(),
                    postId,
                    commentNode.path("authorUsername").asText(),
                    commentNode.path("textContent").asText(""),
                    loadAttachment(commentNode.get("attachment")),
                    commentNode.path("createdAt").asLong()
            ));
        }

        comments.sort(Comparator.comparingLong(Comment::getCreatedAt));
        return comments;
    }

    private ArrayNode ensureCommentsArray(JsonNode postNode) {
        JsonNode commentsNode = postNode.get("comments");
        if (commentsNode instanceof ArrayNode arrayNode) {
            return arrayNode;
        }

        ArrayNode arrayNode = mapper.createArrayNode();
        ((ObjectNode) postNode).set("comments", arrayNode);
        return arrayNode;
    }


    private PostAttachment loadAttachment(JsonNode attachmentNode) throws IOException {
        if (attachmentNode == null || attachmentNode.isNull()) {
            return null;
        }

        Path filePath = UPLOADS_PATH.resolve(attachmentNode.get("storedFileName").asText());
        byte[] data = Files.exists(filePath) ? Files.readAllBytes(filePath) : new byte[0];
        return new PostAttachment(
                attachmentNode.get("originalFileName").asText(),
                attachmentNode.get("mimeType").asText(),
                data
        );
    }

    private void deleteAttachmentIfPresent(JsonNode attachmentNode) throws IOException {
        if (attachmentNode == null || attachmentNode.isNull()) {
            return;
        }
        Path filePath = UPLOADS_PATH.resolve(attachmentNode.path("storedFileName").asText());
        Files.deleteIfExists(filePath);
    }

    private ObjectNode storeAttachment(String postId, PostAttachment attachment) throws IOException {
        String storedFileName = postId + "_" + sanitizeFileName(attachment.getOriginalFileName());
        Path storedPath = UPLOADS_PATH.resolve(storedFileName);
        Files.write(storedPath, attachment.getData());

        ObjectNode attachmentNode = mapper.createObjectNode();
        attachmentNode.put("originalFileName", attachment.getOriginalFileName());
        attachmentNode.put("mimeType", attachment.getMimeType());
        attachmentNode.put("storedFileName", storedFileName);
        return attachmentNode;
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
