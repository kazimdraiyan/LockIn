package app.lockin.lockin.client.controllers;

import app.lockin.lockin.client.MyApplication;
import app.lockin.lockin.client.elements.UserRowController;
import app.lockin.lockin.client.utils.AttachmentViews;
import app.lockin.lockin.client.utils.TextFormatter;
import app.lockin.lockin.client.utils.PostCardRenderer;
import app.lockin.lockin.client.utils.UiIcons;
import app.lockin.lockin.client.utils.UserIdentityRows;
import app.lockin.lockin.client.elements.ProfileAvatar;
import app.lockin.lockin.client.models.ChatListItem;
import app.lockin.lockin.common.models.Comment;
import app.lockin.lockin.common.models.Chat;
import app.lockin.lockin.common.models.Post;
import app.lockin.lockin.common.models.Attachment;
import app.lockin.lockin.common.models.UserPosts;
import app.lockin.lockin.common.models.UserProfile;
import app.lockin.lockin.common.requests.*;
import app.lockin.lockin.common.response.Response;
import app.lockin.lockin.common.response.ResponseStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HomeController implements MainControllerAware {
    private static final long MAX_ATTACHMENT_SIZE_BYTES = 10L * 1024 * 1024;

    @FXML private VBox feedContainer;
    @FXML private VBox contactsContainer;
    @FXML private TextArea postTextArea;
    @FXML private Label selectedFileLabel;
    @FXML private Label composerStatusLabel;
    @FXML private Button uploadFileButton;
    @FXML private Button postButton;
    @FXML private ProfileAvatar profileNavAvatar;
    @FXML private ProfileAvatar composerAvatar;
    @FXML private Label profileNavLabel;

    private MainController mainController;
    private Path selectedFilePath;
    private final Map<String, Attachment> profilePicturesByUsername = new HashMap<>();
    private final PostCardRenderer.Actions postCardActions = new PostCardRenderer.Actions() {
        @Override
        public void openUserProfile(String username) {
            HomeController.this.openUserProfile(username);
        }

        @Override
        public void deletePost(String postId, Button deleteButton) {
        }

        @Override
        public VBox buildAttachmentNode(Attachment attachment) {
            return HomeController.this.buildAttachmentNode(attachment);
        }

        @Override
        public String formatTimestamp(long createdAt) {
            return TextFormatter.formatTimestamp(createdAt);
        }
    };

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        mainController.setNavBar(true, "LockIn", true);
        mainController.setRefreshButtonVisible(true);
        uploadFileButton.setGraphic(UiIcons.icon("attach", 14));
        profileNavLabel.setText(MyApplication.clientManager.getAuthenticatedUsername());
        loadSidebarProfileImage();
        loadPosts();
        loadConnectedUsers();
    }

    @FXML
    protected void onUploadFileClick() {
        Path path = chooseAttachmentFile(uploadFileButton);
        if (path == null) {
            return;
        }

        selectedFilePath = path;
        selectedFileLabel.setText("Selected: " + selectedFilePath.getFileName());
        composerStatusLabel.setText("");
    }

    @FXML
    protected void onCreatePostClick() {
        String textContent = postTextArea.getText() == null ? "" : postTextArea.getText().trim();
        if (textContent.isEmpty() && selectedFilePath == null) {
            composerStatusLabel.setText("Write something or choose a file first.");
            return;
        }

        setComposerBusy(true, "Posting...");
        new Thread(() -> {
            try {
                Attachment attachment = createAttachmentFromSelection();
                CreatePostRequest request = new CreatePostRequest(textContent, attachment);
                Response response = sendRequest(request);

                if (response != null && response.getStatus() == ResponseStatus.SUCCESS) {
                    Platform.runLater(() -> {
                        postTextArea.clear();
                        selectedFilePath = null;
                        selectedFileLabel.setText("No file selected");
                        setComposerBusy(false, "Post shared.");
                    });
                    loadPosts();
                } else {
                    String message = response == null ? "No response from server." : response.getMessage();
                    Platform.runLater(() -> setComposerBusy(false, message));
                }
            } catch (IOException e) {
                Platform.runLater(() -> setComposerBusy(false, e.getMessage()));
            }
        }).start();
    }

    public void onChatsButtonClick(MouseEvent mouseEvent) {
        try {
            mainController.navigatePush("messenger-view.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            composerStatusLabel.setText("Could not open chats.");
        }
    }

    public void onProfileButtonClick(MouseEvent mouseEvent) {
        try {
            mainController.viewedProfileUsername = null;
            mainController.navigatePush("profile-view.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            composerStatusLabel.setText("Could not open profile page.");
        }
    }

    private void loadPosts() {
        new Thread(() -> {
            try {
                Response response = sendRequest(new FetchRequest(FetchType.POSTS));

                if (response != null && response.getStatus() == ResponseStatus.SUCCESS) {
                    @SuppressWarnings("unchecked")
                    ArrayList<Post> posts = (ArrayList<Post>) response.getData();
                    loadProfilePictures(posts);
                    Platform.runLater(() -> renderPosts(posts));
                } else {
                    String message = response == null ? "Could not load posts." : response.getMessage();
                    Platform.runLater(() -> renderErrorState(message));
                }
            } catch (IOException e) {
                Platform.runLater(() -> renderErrorState("Could not load posts."));
            }
        }).start();
    }

    private void loadProfilePictures(List<Post> posts) {
        Set<String> usernames = collectAuthorUsernames(posts);
        Map<String, Attachment> loadedPictures = new HashMap<>();

        try {
            Response response = sendRequest(new FetchRequest(FetchType.PROFILE));
            if (response != null && response.getStatus() == ResponseStatus.SUCCESS && response.getData() instanceof UserPosts pageData) {
                UserProfile profile = pageData.getProfile();
                if (profile != null && usernames.contains(profile.getUsername())) {
                    loadedPictures.put(profile.getUsername(), profile.getProfilePicture());
                }
            }
        } catch (IOException ignored) {
        }

        try {
            Response response = sendRequest(new FetchRequest(FetchType.USER_SEARCH, ""));
            if (response != null && response.getStatus() == ResponseStatus.SUCCESS) {
                @SuppressWarnings("unchecked")
                ArrayList<UserProfile> users = (ArrayList<UserProfile>) response.getData();
                for (UserProfile user : users) {
                    if (user != null && usernames.contains(user.getUsername())) {
                        loadedPictures.put(user.getUsername(), user.getProfilePicture());
                    }
                }
            }
        } catch (IOException ignored) {
        }

        profilePicturesByUsername.clear();
        profilePicturesByUsername.putAll(loadedPictures);
    }

    private void loadConnectedUsers() {
        new Thread(() -> {
            try {
                Response response = sendRequest(new FetchRequest(FetchType.CHATS));
                if (response != null && response.getStatus() == ResponseStatus.SUCCESS) {
                    @SuppressWarnings("unchecked")
                    ArrayList<Chat> chats = (ArrayList<Chat>) response.getData();
                    Platform.runLater(() -> renderConnectedUsers(chats));
                } else {
                    String message = response == null ? "Could not load users." : response.getMessage();
                    Platform.runLater(() -> renderContactsMessage(message));
                }
            } catch (IOException e) {
                Platform.runLater(() -> renderContactsMessage("Could not load users."));
            }
        }).start();
    }

    private void loadSidebarProfileImage() {
        new Thread(() -> {
            try {
                Response response = sendRequest(new FetchRequest(FetchType.PROFILE));
                if (response != null && response.getStatus() == ResponseStatus.SUCCESS && response.getData() instanceof UserPosts pageData) {
                    UserProfile profile = pageData.getProfile();
                    Platform.runLater(() -> renderSidebarProfileImage(profile == null ? null : profile.getProfilePicture()));
                } else {
                    Platform.runLater(() -> renderSidebarProfileImage(null));
                }
            } catch (IOException e) {
                Platform.runLater(() -> renderSidebarProfileImage(null));
            }
        }).start();
    }

    private Attachment createAttachmentFromSelection() throws IOException {
        return createAttachmentFromPath(selectedFilePath);
    }

    private Response sendRequest(Request request) throws IOException {
        return MyApplication.clientManager.sendRequest(request);
    }

    private void renderPosts(ArrayList<Post> posts) {
        feedContainer.getChildren().clear();
        if (posts == null || posts.isEmpty()) {
            Label emptyLabel = new Label("No posts yet. Share the first update.");
            emptyLabel.getStyleClass().add("muted-text");
            feedContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Post post : posts) {
            feedContainer.getChildren().add(buildPostCard(post));
        }
    }

    private void renderErrorState(String message) {
        feedContainer.getChildren().clear();
        Label errorLabel = new Label(message);
        errorLabel.getStyleClass().add("error-label");
        feedContainer.getChildren().add(errorLabel);
    }

    private void renderConnectedUsers(List<Chat> chats) {
        contactsContainer.getChildren().clear();
        if (chats == null || chats.isEmpty()) {
            renderContactsMessage("No users found.");
            return;
        }

        for (Chat chat : chats) {
            ChatListItem item = buildChatListItem(chat);
            contactsContainer.getChildren().add(buildConnectedUserItem(item));
        }
    }

    private void renderContactsMessage(String message) {
        contactsContainer.getChildren().clear();
        Label label = new Label(message);
        label.getStyleClass().add("muted-text");
        contactsContainer.getChildren().add(label);
    }

    private HBox buildConnectedUserItem(ChatListItem item) {
        String username = item.getUserName();
        HBox row = createReusableUserRow(
                username,
                username,
                null,
                36,
                profilePicturesByUsername.get(username),
                () -> openUserProfile(username)
        );
        row.setPrefHeight(48);
        row.getStyleClass().add("contact-item");
        row.setOnMouseClicked(event -> openChat(username));
        return row;
    }

    private ChatListItem buildChatListItem(Chat chat) {
        return new ChatListItem(
                chat,
                chat.getName(),
                "",
                chat.getUnreadCount(),
                "",
                chat.getLastMessage() == null ? 0L : chat.getLastMessage().getCreatedAt()
        );
    }

    private VBox buildPostCard(Post post) {
        return PostCardRenderer.buildPostCard(
                post,
                false,
                MyApplication.clientManager.getAuthenticatedUsername(),
                profilePicturesByUsername,
                buildCommentComposer(post),
                postCardActions
        );
    }

    private VBox buildCommentComposer(Post post) {
        VBox composer = new VBox(8);
        composer.setPadding(new Insets(10, 0, 0, 0));
        composer.getStyleClass().add("comment-composer");

        TextField commentField = new TextField();
        commentField.setPromptText("Write a comment...");
        commentField.getStyleClass().add("input-field");

        Label selectedCommentFileLabel = new Label("No file selected");
        selectedCommentFileLabel.getStyleClass().add("muted-text");

        Label commentStatusLabel = new Label();
        commentStatusLabel.getStyleClass().add("muted-text");

        Button chooseFileButton = new Button("Attach File");
        chooseFileButton.getStyleClass().add("feed-action-button");
        chooseFileButton.setGraphic(UiIcons.icon("attach", 14));

        Button commentButton = new Button("Comment");
        commentButton.getStyleClass().add("primary-button");

        final Path[] selectedCommentFilePath = new Path[1];

        chooseFileButton.setOnAction(event -> {
            Path path = chooseAttachmentFile(chooseFileButton);
            if (path == null) {
                return;
            }

            selectedCommentFilePath[0] = path;
            selectedCommentFileLabel.setText("Selected: " + path.getFileName());
            commentStatusLabel.setText("");
        });

        commentButton.setOnAction(event -> submitComment(
                post.getId(),
                commentField,
                selectedCommentFilePath,
                selectedCommentFileLabel,
                commentStatusLabel,
                chooseFileButton,
                commentButton
        ));

        HBox actions = new HBox(8, chooseFileButton, commentButton);
        HBox.setHgrow(chooseFileButton, Priority.ALWAYS);
        HBox.setHgrow(commentButton, Priority.ALWAYS);
        chooseFileButton.setMaxWidth(Double.MAX_VALUE);
        commentButton.setMaxWidth(Double.MAX_VALUE);

        HBox metaRow = new HBox(10, selectedCommentFileLabel, new Region(), commentStatusLabel);
        HBox.setHgrow(metaRow.getChildren().get(1), Priority.ALWAYS);

        composer.getChildren().addAll(commentField, metaRow, actions);
        return composer;
    }

    private VBox buildAttachmentNode(Attachment attachment) {
        return AttachmentViews.buildFeedAttachment(
                attachment,
                true,
                true,
                event -> downloadAttachment(attachment)
        );
    }

    private void renderSidebarProfileImage(Attachment profilePicture) {
        applyOwnProfileImage(profileNavAvatar, profilePicture);
        applyOwnProfileImage(composerAvatar, profilePicture);
    }

    private void applyOwnProfileImage(ProfileAvatar avatar, Attachment profilePicture) {
        String username = MyApplication.clientManager.getAuthenticatedUsername();
        avatar.setText(username == null ? "ME" : username);
        if (profilePicture == null || profilePicture.getData().length == 0) {
            avatar.setImage(null);
            return;
        }
        avatar.setImage(new Image(new ByteArrayInputStream(profilePicture.getData())));
    }

    private void downloadAttachment(Attachment attachment) {
        try {
            Path downloadsDir = Path.of(System.getProperty("user.home"), "Downloads", "LockIn");
            Files.createDirectories(downloadsDir);

            Path targetPath = downloadsDir.resolve(attachment.getOriginalFileName());
            if (Files.exists(targetPath)) {
                targetPath = downloadsDir.resolve(System.currentTimeMillis() + "_" + attachment.getOriginalFileName());
            }

            Files.copy(new ByteArrayInputStream(attachment.getData()), targetPath, StandardCopyOption.REPLACE_EXISTING);
            composerStatusLabel.setText("Saved to " + targetPath);
        } catch (IOException e) {
            composerStatusLabel.setText("Could not save file.");
        }
    }

    private void openUserProfile(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        try {
            if (MyApplication.clientManager.getAuthenticatedUsername() == null) {
                System.out.println("Can't open profile. User is not logged in.");
                return;
            }
            mainController.openProfile(username);
        } catch (IOException e) {
            composerStatusLabel.setText("Could not open profile.");
        }
    }

    private void openChat(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        try {
            mainController.openChat(username);
        } catch (IOException e) {
            composerStatusLabel.setText("Could not open chat.");
        }
    }

    private Set<String> collectAuthorUsernames(List<Post> posts) {
        Set<String> usernames = new HashSet<>();
        if (posts == null) {
            return usernames;
        }

        for (Post post : posts) {
            if (post == null) {
                continue;
            }
            usernames.add(post.getAuthorUsername());
            for (Comment comment : post.getComments()) {
                usernames.add(comment.getAuthorUsername());
            }
        }
        return usernames;
    }

    private void setComposerBusy(boolean busy, String message) {
        postButton.setDisable(busy);
        uploadFileButton.setDisable(busy);
        composerStatusLabel.setText(message == null ? "" : message);
    }

    private Path chooseAttachmentFile(Button triggerButton) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a file");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Supported files", "*.jpg", "*.jpeg", "*.gif", "*.pdf", "*.txt"),
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("PDF files", "*.pdf"),
                new FileChooser.ExtensionFilter("Text files", "*.txt")
        );

        Window window = triggerButton.getScene() == null ? null : triggerButton.getScene().getWindow();
        java.io.File chosenFile = fileChooser.showOpenDialog(window);
        return chosenFile == null ? null : chosenFile.toPath();
    }

    private void submitComment(
            String postId,
            TextField commentField,
            Path[] selectedCommentFilePath,
            Label selectedCommentFileLabel,
            Label commentStatusLabel,
            Button chooseFileButton,
            Button commentButton
    ) {
        String textContent = commentField.getText() == null ? "" : commentField.getText().trim();
        if (textContent.isEmpty() && selectedCommentFilePath[0] == null) {
            commentStatusLabel.setText("Write something or choose a file first.");
            return;
        }

        setCommentComposerBusy(chooseFileButton, commentButton, commentStatusLabel, true, "Commenting...");
        new Thread(() -> {
            try {
                Attachment attachment = createAttachmentFromPath(selectedCommentFilePath[0]);
                Response response = sendRequest(new CreateCommentRequest(postId, textContent, attachment));

                if (response != null && response.getStatus() == ResponseStatus.SUCCESS) {
                    Platform.runLater(() -> {
                        commentField.clear();
                        selectedCommentFilePath[0] = null;
                        selectedCommentFileLabel.setText("No file selected");
                        setCommentComposerBusy(chooseFileButton, commentButton, commentStatusLabel, false, "Comment added.");
                    });
                    loadPosts();
                } else {
                    String message = response == null ? "No response from server." : response.getMessage();
                    Platform.runLater(() -> setCommentComposerBusy(chooseFileButton, commentButton, commentStatusLabel, false, message));
                }
            } catch (IOException e) {
                Platform.runLater(() -> setCommentComposerBusy(chooseFileButton, commentButton, commentStatusLabel, false, e.getMessage()));
            }
        }).start();
    }

    private void setCommentComposerBusy(
            Button chooseFileButton,
            Button commentButton,
            Label commentStatusLabel,
            boolean busy,
            String message
    ) {
        chooseFileButton.setDisable(busy);
        commentButton.setDisable(busy);
        commentStatusLabel.setText(message == null ? "" : message);
    }

    private Attachment createAttachmentFromPath(Path filePath) throws IOException {
        if (filePath == null) {
            return null;
        }

        if (!Files.exists(filePath)) {
            throw new IOException("Selected file no longer exists.");
        }

        long size = Files.size(filePath);
        if (size > MAX_ATTACHMENT_SIZE_BYTES) {
            throw new IOException("File is too large. Limit is 10 MB.");
        }

        String mimeType = Files.probeContentType(filePath);
        String fileName = filePath.getFileName().toString();
        byte[] data = Files.readAllBytes(filePath);
        return new Attachment(fileName, mimeType, data);
    }

    private HBox createReusableUserRow(
            String username,
            String primaryText,
            String secondaryText,
            double avatarSize,
            Attachment picture,
            Runnable onClick
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(MyApplication.getFXML("user-row.fxml"));
            HBox root = loader.load();
            UserRowController controller = loader.getController();
            controller.configure(username, primaryText, secondaryText, avatarSize, picture, onClick);
            return root;
        } catch (IOException ignored) {
            return UserIdentityRows.build(username, primaryText, secondaryText, avatarSize, picture, onClick);
        }
    }

}
