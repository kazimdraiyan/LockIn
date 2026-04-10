package app.lockin.lockin.client.controllers;

import app.lockin.lockin.client.MyApplication;
import app.lockin.lockin.client.elements.ProfileAvatar;
import app.lockin.lockin.client.models.NavUiConfig;
import app.lockin.lockin.client.utils.AttachmentViews;
import app.lockin.lockin.client.utils.PostCardRenderer;
import app.lockin.lockin.client.utils.UiIcons;
import app.lockin.lockin.common.models.Comment;
import app.lockin.lockin.common.models.Post;
import app.lockin.lockin.common.models.Attachment;
import app.lockin.lockin.common.models.UserPosts;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

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
    private Attachment currentProfilePicture;
    private String viewedUsername;
    private boolean ownProfile;
    private final Map<String, Attachment> profilePicturesByUsername = new HashMap<>();
    private final PostCardRenderer.Actions postCardActions = new PostCardRenderer.Actions() {
        @Override
        public void openUserProfile(String username) {
            ProfileController.this.openUserProfile(username);
        }

        @Override
        public void deletePost(String postId, Button deleteButton) {
            ProfileController.this.deletePost(postId, deleteButton);
        }

        @Override
        public VBox buildAttachmentNode(Attachment attachment) {
            return ProfileController.this.buildAttachmentNode(attachment);
        }

        @Override
        public String formatTimestamp(long createdAt) {
            return ProfileController.this.formatTimestamp(createdAt);
        }
    };

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        viewedUsername = mainController.viewedProfileUsername;
        ownProfile = viewedUsername == null || viewedUsername.isBlank();
        UiIcons.setButtonIcon(chooseProfilePictureButton, "attach", 14);
        mainController.applyNavUi(new NavUiConfig(true, ownProfile ? "Profile" : viewedUsername, true, false, true));
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
                Attachment profilePicture = currentProfilePicture;
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
                    UserPosts data = (UserPosts) response.getData();
                    loadProfilePictures(data == null ? null : data.getPosts());
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

    private void renderProfilePage(UserPosts data) {
        UserProfile profile = data.getProfile();
        ownProfile = mainController != null
                && MyApplication.clientManager.getAuthenticatedUsername() != null
                && MyApplication.clientManager.getAuthenticatedUsername().equals(profile.getUsername());
        usernameLabel.setText(profile.getUsername());
        profileAvatar.setText(profile.getUsername());
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
            postsContainer.getChildren().add(buildPostCard(post));
        }
        postsScrollPane.setVvalue(0);
    }

    private void renderError(String message) {
        postsContainer.getChildren().clear();
        Label errorLabel = new Label(message);
        errorLabel.getStyleClass().add("error-label");
        postsContainer.getChildren().add(errorLabel);
    }

    private VBox buildPostCard(Post post) {
        return PostCardRenderer.buildPostCard(
                post,
                ownProfile,
                MyApplication.clientManager.getAuthenticatedUsername(),
                profilePicturesByUsername,
                null,
                postCardActions
        );
    }

    private VBox buildAttachmentNode(Attachment attachment) {
        return AttachmentViews.buildFeedAttachment(attachment, false, false, null);
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

    private void openUserProfile(String username) {
        if (mainController == null || username == null || username.isBlank()) {
            return;
        }
        try {
            mainController.openProfile(username);
        } catch (IOException ignored) {
        }
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
                java.util.ArrayList<UserProfile> users = (java.util.ArrayList<UserProfile>) response.getData();
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

    private void renderProfileImage(Attachment profilePicture) {
        if (profilePicture == null || profilePicture.getData().length == 0) {
            profileAvatar.setImage(null);
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

    private Attachment createProfilePictureAttachment(Path path) throws IOException {
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
        return new Attachment(path.getFileName().toString(), mimeType, Files.readAllBytes(path));
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

}
