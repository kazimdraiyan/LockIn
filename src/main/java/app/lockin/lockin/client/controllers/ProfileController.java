package app.lockin.lockin.client.controllers;

import app.lockin.lockin.client.MyApplication;
import app.lockin.lockin.client.elements.ProfileAvatar;
import app.lockin.lockin.common.models.Comment;
import app.lockin.lockin.common.models.Post;
import app.lockin.lockin.common.models.PostAttachment;
import app.lockin.lockin.common.models.ProfilePageData;
import app.lockin.lockin.common.models.UserProfile;
import app.lockin.lockin.common.requests.DeletePostRequest;
import app.lockin.lockin.common.requests.FetchRequest;
import app.lockin.lockin.common.requests.FetchType;
import app.lockin.lockin.common.requests.UpdateProfileRequest;
import app.lockin.lockin.common.response.Response;
import app.lockin.lockin.common.response.ResponseStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ProfileController implements MainControllerAware {
    private static final DateTimeFormatter ABSOLUTE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH).withZone(ZoneId.systemDefault());
    private static final long MAX_PROFILE_PICTURE_SIZE_BYTES = 10L * 1024 * 1024;

    @FXML private ProfileAvatar profileAvatar;
    @FXML private Label usernameLabel;
    @FXML private Label profilePictureLabel;
    @FXML private Label profileStatusLabel;
    @FXML private TextArea descriptionTextArea;
    @FXML private VBox postsContainer;
    @FXML private ScrollPane postsScrollPane;
    @FXML private Button chooseProfilePictureButton;
    @FXML private Button saveProfileButton;
    @FXML private Label postsTitleLabel;

    private MainController mainController;
    private Path selectedProfileImagePath;
    private PostAttachment currentProfilePicture;
    private String viewedUsername;
    private boolean ownProfile;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        viewedUsername = mainController.viewedProfileUsername;
        ownProfile = viewedUsername == null || viewedUsername.isBlank();
        mainController.setNavBar(true, ownProfile ? "Profile" : viewedUsername, true);
        loadProfile();
    }

    @FXML
    protected void onChooseProfilePictureClick() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select profile picture");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        Window window = chooseProfilePictureButton.getScene() == null ? null : chooseProfilePictureButton.getScene().getWindow();
        java.io.File file = chooser.showOpenDialog(window);
        if (file == null) {
            return;
        }

        selectedProfileImagePath = file.toPath();
        profilePictureLabel.setText("Selected: " + selectedProfileImagePath.getFileName());
        profileStatusLabel.setText("");
        previewSelectedImage();
    }

    @FXML
    protected void onSaveProfileClick() {
        String description = descriptionTextArea.getText();
        setProfileBusy(true, "Saving profile...");
        new Thread(() -> {
            try {
                PostAttachment profilePicture = currentProfilePicture;
                if (selectedProfileImagePath != null) {
                    profilePicture = createProfilePictureAttachment(selectedProfileImagePath);
                }

                Response response = sendRequest(new UpdateProfileRequest(description, profilePicture));
                if (response != null && response.getStatus() == ResponseStatus.SUCCESS) {
                    UserProfile updatedProfile = response.getData() instanceof UserProfile userProfile ? userProfile : null;
                    currentProfilePicture = updatedProfile == null ? profilePicture : updatedProfile.getProfilePicture();
                    selectedProfileImagePath = null;
                    Platform.runLater(() -> {
                        profilePictureLabel.setText(currentProfilePicture == null ? "Using default account image" : "Current picture saved");
                        renderProfileImage(currentProfilePicture);
                        setProfileBusy(false, "Profile saved.");
                    });
                } else {
                    String message = response == null ? "No response from server." : response.getMessage();
                    Platform.runLater(() -> setProfileBusy(false, message));
                }
            } catch (IOException e) {
                Platform.runLater(() -> setProfileBusy(false, e.getMessage()));
            }
        }).start();
    }

    private void loadProfile() {
        postsContainer.getChildren().setAll(new Label("Loading profile..."));
        new Thread(() -> {
            try {
                Response response = sendRequest(new FetchRequest(FetchType.PROFILE, viewedUsername));
                if (response != null && response.getStatus() == ResponseStatus.SUCCESS) {
                    ProfilePageData data = (ProfilePageData) response.getData();
                    Platform.runLater(() -> renderProfilePage(data));
                } else {
                    String message = response == null ? "Could not load profile." : response.getMessage();
                    Platform.runLater(() -> renderError(message));
                }
            } catch (IOException e) {
                Platform.runLater(() -> renderError("Could not load profile."));
            }
        }).start();
    }

    private void renderProfilePage(ProfilePageData data) {
        UserProfile profile = data.getProfile();
        ownProfile = mainController != null
                && MyApplication.clientManager.getAuthenticatedUsername() != null
                && MyApplication.clientManager.getAuthenticatedUsername().equals(profile.getUsername());
        usernameLabel.setText(profile.getUsername());
        profileAvatar.setText(extractInitials(profile.getUsername()));
        descriptionTextArea.setText(profile.getDescription());
        descriptionTextArea.setEditable(ownProfile);
        descriptionTextArea.setDisable(!ownProfile);
        currentProfilePicture = profile.getProfilePicture();
        selectedProfileImagePath = null;
        renderProfileImage(currentProfilePicture);
        profilePictureLabel.setText(currentProfilePicture == null ? "Using default account image" : "Current picture saved");
        profileStatusLabel.setText("");
        chooseProfilePictureButton.setManaged(ownProfile);
        chooseProfilePictureButton.setVisible(ownProfile);
        saveProfileButton.setManaged(ownProfile);
        saveProfileButton.setVisible(ownProfile);
        postsTitleLabel.setText(ownProfile ? "Your posts" : profile.getUsername() + "'s posts");

        postsContainer.getChildren().clear();
        List<Post> posts = data.getPosts();
        if (posts.isEmpty()) {
            Label emptyLabel = new Label(ownProfile ? "You have not posted anything yet." : "This user has not posted anything yet.");
            emptyLabel.getStyleClass().add("muted-text");
            postsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Post post : posts) {
            postsContainer.getChildren().add(ownProfile ? buildOwnPostCard(post) : buildReadOnlyPostCard(post));
        }
        postsScrollPane.setVvalue(0);
    }

    private void renderError(String message) {
        postsContainer.getChildren().clear();
        Label errorLabel = new Label(message);
        errorLabel.getStyleClass().add("error-label");
        postsContainer.getChildren().add(errorLabel);
    }

    private VBox buildOwnPostCard(Post post) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("feed-card", "post-thread-card");
        card.setPadding(new Insets(16));

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox metaBox = new VBox(2);
        Label titleLabel = new Label("Posted by you");
        titleLabel.getStyleClass().add("text-strong");
        Label timeLabel = new Label(formatTimestamp(post.getCreatedAt()));
        timeLabel.getStyleClass().add("muted-text");
        metaBox.getChildren().addAll(titleLabel, timeLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteButton = new Button("Delete post");
        deleteButton.getStyleClass().add("danger-button");
        deleteButton.setOnAction(event -> deletePost(post.getId(), deleteButton));

        topRow.getChildren().addAll(metaBox, spacer, deleteButton);
        card.getChildren().add(topRow);

        if (post.getTextContent() != null && !post.getTextContent().isBlank()) {
            Label contentLabel = new Label(post.getTextContent());
            contentLabel.setWrapText(true);
            contentLabel.getStyleClass().add("body-text");
            card.getChildren().add(contentLabel);
        }

        if (post.getAttachment() != null) {
            card.getChildren().add(buildAttachmentNode(post.getAttachment()));
        }

        VBox commentsSection = new VBox(8);
        commentsSection.getStyleClass().add("comments-section");
        Label commentsTitle = new Label("Comments");
        commentsTitle.getStyleClass().add("text-strong");
        commentsSection.getChildren().add(commentsTitle);

        if (post.getComments().isEmpty()) {
            Label emptyLabel = new Label("No comments yet.");
            emptyLabel.getStyleClass().add("muted-text");
            commentsSection.getChildren().add(emptyLabel);
        } else {
            for (Comment comment : post.getComments()) {
                commentsSection.getChildren().add(buildCommentCard(comment));
            }
        }

        card.getChildren().addAll(new Separator(), commentsSection);
        return card;
    }

    private VBox buildReadOnlyPostCard(Post post) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("feed-card", "post-thread-card");
        card.setPadding(new Insets(16));

        VBox metaBox = new VBox(2);
        Label titleLabel = new Label(post.getAuthorUsername());
        titleLabel.getStyleClass().add("text-strong");
        Label timeLabel = new Label(formatTimestamp(post.getCreatedAt()));
        timeLabel.getStyleClass().add("muted-text");
        metaBox.getChildren().addAll(titleLabel, timeLabel);
        card.getChildren().add(metaBox);

        if (post.getTextContent() != null && !post.getTextContent().isBlank()) {
            Label contentLabel = new Label(post.getTextContent());
            contentLabel.setWrapText(true);
            contentLabel.getStyleClass().add("body-text");
            card.getChildren().add(contentLabel);
        }

        if (post.getAttachment() != null) {
            card.getChildren().add(buildAttachmentNode(post.getAttachment()));
        }

        VBox commentsSection = new VBox(8);
        commentsSection.getStyleClass().add("comments-section");
        Label commentsTitle = new Label("Comments");
        commentsTitle.getStyleClass().add("text-strong");
        commentsSection.getChildren().add(commentsTitle);

        if (post.getComments().isEmpty()) {
            Label emptyLabel = new Label("No comments yet.");
            emptyLabel.getStyleClass().add("muted-text");
            commentsSection.getChildren().add(emptyLabel);
        } else {
            for (Comment comment : post.getComments()) {
                commentsSection.getChildren().add(buildCommentCard(comment));
            }
        }

        card.getChildren().addAll(new Separator(), commentsSection);
        return card;
    }

    private VBox buildCommentCard(Comment comment) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(12));
        card.getStyleClass().add("comment-card");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label username = new Label(comment.getAuthorUsername());
        username.getStyleClass().add("text-strong");
        Label time = new Label(formatTimestamp(comment.getCreatedAt()));
        time.getStyleClass().add("muted-text");
        header.getChildren().addAll(username, time);
        card.getChildren().add(header);

        if (comment.getTextContent() != null && !comment.getTextContent().isBlank()) {
            Label contentLabel = new Label(comment.getTextContent());
            contentLabel.setWrapText(true);
            contentLabel.getStyleClass().add("body-text");
            card.getChildren().add(contentLabel);
        }

        if (comment.getAttachment() != null) {
            card.getChildren().add(buildAttachmentNode(comment.getAttachment()));
        }
        return card;
    }

    private VBox buildAttachmentNode(PostAttachment attachment) {
        VBox attachmentBox = new VBox(10);

        if (attachment.getMimeType() != null && attachment.getMimeType().startsWith("image/")) {
            ImageView imageView = new ImageView(new Image(new ByteArrayInputStream(attachment.getData())));
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(420);
            attachmentBox.getChildren().add(imageView);
        }

        HBox preview = new HBox(12);
        preview.setAlignment(Pos.CENTER_LEFT);
        preview.setPadding(new Insets(12));
        preview.getStyleClass().add("file-preview");

        Label fileNameLabel = new Label(attachment.getOriginalFileName());
        fileNameLabel.getStyleClass().add("text-strong");
        Label fileTypeLabel = new Label(attachment.getMimeType());
        fileTypeLabel.getStyleClass().add("file-meta");

        VBox fileMeta = new VBox(2, fileNameLabel, fileTypeLabel);
        preview.getChildren().add(fileMeta);
        attachmentBox.getChildren().add(preview);
        return attachmentBox;
    }

    private void deletePost(String postId, Button deleteButton) {
        deleteButton.setDisable(true);
        profileStatusLabel.setText("Deleting post...");
        new Thread(() -> {
            try {
                Response response = sendRequest(new DeletePostRequest(postId));
                if (response != null && response.getStatus() == ResponseStatus.SUCCESS) {
                    Platform.runLater(() -> {
                        profileStatusLabel.setText("Post deleted.");
                        loadProfile();
                    });
                } else {
                    String message = response == null ? "No response from server." : response.getMessage();
                    Platform.runLater(() -> {
                        deleteButton.setDisable(false);
                        profileStatusLabel.setText(message);
                    });
                }
            } catch (IOException e) {
                Platform.runLater(() -> {
                    deleteButton.setDisable(false);
                    profileStatusLabel.setText(e.getMessage());
                });
            }
        }).start();
    }

    private Response sendRequest(app.lockin.lockin.common.requests.Request request) throws IOException {
        synchronized (MyApplication.clientManager) {
            return MyApplication.clientManager.sendRequest(request);
        }
    }

    private void renderProfileImage(PostAttachment profilePicture) {
        if (profilePicture == null || profilePicture.getData().length == 0) {
            profileAvatar.setImage(new Image(MyApplication.getIcon("account.png").toExternalForm()));
            return;
        }
        profileAvatar.setImage(new Image(new ByteArrayInputStream(profilePicture.getData())));
    }

    private void previewSelectedImage() {
        try {
            byte[] data = Files.readAllBytes(selectedProfileImagePath);
            profileAvatar.setImage(new Image(new ByteArrayInputStream(data)));
        } catch (IOException e) {
            profileStatusLabel.setText("Could not preview selected image.");
        }
    }

    private PostAttachment createProfilePictureAttachment(Path path) throws IOException {
        if (path == null || !Files.exists(path)) {
            throw new IOException("Selected image no longer exists.");
        }

        long size = Files.size(path);
        if (size > MAX_PROFILE_PICTURE_SIZE_BYTES) {
            throw new IOException("Profile picture is too large. Limit is 10 MB.");
        }

        String mimeType = Files.probeContentType(path);
        if (mimeType == null || "application/octet-stream".equals(mimeType)) {
            String lower = path.getFileName().toString().toLowerCase(Locale.ENGLISH);
            if (lower.endsWith(".png")) {
                mimeType = "image/png";
            } else if (lower.endsWith(".gif")) {
                mimeType = "image/gif";
            } else {
                mimeType = "image/jpeg";
            }
        }
        return new PostAttachment(path.getFileName().toString(), mimeType, Files.readAllBytes(path));
    }

    private void setProfileBusy(boolean busy, String message) {
        chooseProfilePictureButton.setDisable(busy);
        saveProfileButton.setDisable(busy);
        profileStatusLabel.setText(message == null ? "" : message);
    }

    private String formatTimestamp(long createdAt) {
        Duration age = Duration.between(Instant.ofEpochMilli(createdAt), Instant.now());
        if (age.toMinutes() < 1) {
            return "Just now";
        }
        if (age.toHours() < 1) {
            return age.toMinutes() + " minutes ago";
        }
        if (age.toDays() < 1) {
            return age.toHours() + " hours ago";
        }
        if (age.toDays() < 7) {
            return age.toDays() + " days ago";
        }
        return ABSOLUTE_TIME_FORMAT.format(Instant.ofEpochMilli(createdAt));
    }

    private String extractInitials(String username) {
        if (username == null || username.isBlank()) {
            return "?";
        }
        String trimmed = username.trim();
        return trimmed.substring(0, Math.min(2, trimmed.length())).toUpperCase(Locale.ENGLISH);
    }
}
