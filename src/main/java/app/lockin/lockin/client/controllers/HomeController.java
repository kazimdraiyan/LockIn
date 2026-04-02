package app.lockin.lockin.client.controllers;

import app.lockin.lockin.client.MyApplication;
import app.lockin.lockin.common.models.Post;
import app.lockin.lockin.common.models.PostAttachment;
import app.lockin.lockin.common.requests.CreatePostRequest;
import app.lockin.lockin.common.requests.FetchRequest;
import app.lockin.lockin.common.requests.FetchType;
import app.lockin.lockin.common.requests.LogoutRequest;
import app.lockin.lockin.common.response.Response;
import app.lockin.lockin.common.response.ResponseStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class HomeController implements MainControllerAware {
    private static final long MAX_ATTACHMENT_SIZE_BYTES = 10L * 1024 * 1024;
    private static final DateTimeFormatter ABSOLUTE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH).withZone(ZoneId.systemDefault());

    @FXML private VBox feedContainer;
    @FXML private VBox contactsContainer;
    @FXML private TextArea postTextArea;
    @FXML private Label selectedFileLabel;
    @FXML private Label composerStatusLabel;
    @FXML private Button uploadFileButton;
    @FXML private Button postButton;

    private MainController mainController;
    private Path selectedFilePath;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        mainController.setNavBar(true, "LockIn", true);
        loadPosts();
    }

    @FXML
    protected void onUploadFileClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a file to post");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Supported files", "*.jpg", "*.jpeg", "*.gif", "*.pdf", "*.txt"),
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("PDF files", "*.pdf"),
                new FileChooser.ExtensionFilter("Text files", "*.txt")
        );

        Window window = uploadFileButton.getScene() == null ? null : uploadFileButton.getScene().getWindow();
        java.io.File chosenFile = fileChooser.showOpenDialog(window);
        if (chosenFile == null) {
            return;
        }

        selectedFilePath = chosenFile.toPath();
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
                PostAttachment attachment = createAttachmentFromSelection();
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

    public void onChatsButtonClick(MouseEvent mouseEvent) throws IOException {
        mainController.navigatePush("messenger-view.fxml");
    }

    public void logout(MouseEvent mouseEvent) {
        new Thread(() -> {
            try {
                LogoutRequest request = new LogoutRequest();
                Response response = sendRequest(request);
                if (response != null && response.getStatus() == ResponseStatus.SUCCESS) {
                    MyApplication.clientManager.isLoggedIn = false;
                    MyApplication.deleteToken();
                    Platform.runLater(() -> {
                        try {
                            mainController.navigateReplacingRoot("welcome-view.fxml");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadPosts() {
        new Thread(() -> {
            try {
                Response response = sendRequest(new FetchRequest(FetchType.POSTS));

                if (response != null && response.getStatus() == ResponseStatus.SUCCESS) {
                    @SuppressWarnings("unchecked")
                    ArrayList<Post> posts = (ArrayList<Post>) response.getData();
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

    private PostAttachment createAttachmentFromSelection() throws IOException {
        if (selectedFilePath == null) {
            return null;
        }

        if (!Files.exists(selectedFilePath)) {
            throw new IOException("Selected file no longer exists.");
        }

        long size = Files.size(selectedFilePath);
        if (size > MAX_ATTACHMENT_SIZE_BYTES) {
            throw new IOException("File is too large. Limit is 10 MB.");
        }

        String mimeType = Files.probeContentType(selectedFilePath);
        String fileName = selectedFilePath.getFileName().toString();
        byte[] data = Files.readAllBytes(selectedFilePath);
        return new PostAttachment(fileName, mimeType, data);
    }

    private Response sendRequest(app.lockin.lockin.common.requests.Request request) throws IOException {
        synchronized (MyApplication.clientManager) {
            MyApplication.clientManager.send(request);
            return MyApplication.clientManager.receive();
        }
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

    private VBox buildPostCard(Post post) {
        VBox card = new VBox(10);
        card.getStyleClass().add("feed-card");
        card.setPadding(new Insets(14, 16, 14, 16));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(createAvatar(post.getAuthorUsername(), 42));

        VBox metaBox = new VBox(2);
        Label usernameLabel = new Label(post.getAuthorUsername());
        usernameLabel.getStyleClass().add("text-strong");
        Label timeLabel = new Label(formatTimestamp(post.getCreatedAt()));
        timeLabel.getStyleClass().add("muted-text");
        metaBox.getChildren().addAll(usernameLabel, timeLabel);

        header.getChildren().add(metaBox);
        card.getChildren().add(header);

        if (post.getTextContent() != null && !post.getTextContent().isBlank()) {
            Label contentLabel = new Label(post.getTextContent());
            contentLabel.setWrapText(true);
            contentLabel.getStyleClass().add("body-text");
            card.getChildren().add(contentLabel);
        }

        if (post.getAttachment() != null) {
            card.getChildren().add(buildAttachmentNode(post.getAttachment()));
        }

        card.getChildren().add(new Separator());

        HBox actions = new HBox();
        actions.setSpacing(0);
        actions.getChildren().addAll(
                createDisabledActionButton("Like"),
                createDisabledActionButton("Comment"),
                createDisabledActionButton("Share")
        );
        card.getChildren().add(actions);

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

        if ("text/plain".equals(attachment.getMimeType())) {
            Label previewLabel = new Label(extractTextPreview(attachment));
            previewLabel.setWrapText(true);
            previewLabel.getStyleClass().add("body-text");
            attachmentBox.getChildren().add(previewLabel);
        }

        attachmentBox.getChildren().add(buildFilePreview(attachment));
        return attachmentBox;
    }

    private HBox buildFilePreview(PostAttachment attachment) {
        HBox preview = new HBox(14);
        preview.setAlignment(Pos.CENTER_LEFT);
        preview.setPadding(new Insets(14));
        preview.getStyleClass().add("file-preview");

        Label iconLabel = new Label(fileBadgeText(attachment));
        iconLabel.getStyleClass().add("file-icon");

        VBox textBox = new VBox(3);
        Label fileNameLabel = new Label(attachment.getOriginalFileName());
        fileNameLabel.getStyleClass().add("text-strong");
        Label metaLabel = new Label(readableFileSize(attachment.getData().length) + "  |  " + readableFileType(attachment));
        metaLabel.getStyleClass().add("file-meta");
        textBox.getChildren().addAll(fileNameLabel, metaLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button downloadButton = new Button("Download");
        downloadButton.getStyleClass().add("primary-button");
        downloadButton.setOnAction(event -> downloadAttachment(attachment));

        preview.getChildren().addAll(iconLabel, textBox, spacer, downloadButton);
        return preview;
    }

    private Button createDisabledActionButton(String text) {
        Button button = new Button(text);
        button.setDisable(true);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(34);
        button.getStyleClass().add("feed-action-button");
        HBox.setHgrow(button, Priority.ALWAYS);
        return button;
    }

    private VBox createAvatar(String username, double size) {
        VBox avatar = new VBox();
        avatar.setAlignment(Pos.CENTER);
        avatar.getStyleClass().add("profile-avatar");
        avatar.setMinSize(size, size);
        avatar.setPrefSize(size, size);
        avatar.setMaxSize(size, size);

        Label initials = new Label(extractInitials(username));
        initials.getStyleClass().add("profile-avatar-text");
        avatar.getChildren().add(initials);
        return avatar;
    }

    private void downloadAttachment(PostAttachment attachment) {
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

    private void setComposerBusy(boolean busy, String message) {
        postButton.setDisable(busy);
        uploadFileButton.setDisable(busy);
        composerStatusLabel.setText(message == null ? "" : message);
    }

    private String extractInitials(String username) {
        if (username == null || username.isBlank()) {
            return "?";
        }
        String trimmed = username.trim();
        return trimmed.substring(0, Math.min(2, trimmed.length())).toUpperCase(Locale.ENGLISH);
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

    private String readableFileType(PostAttachment attachment) {
        return switch (attachment.getMimeType()) {
            case "image/jpeg" -> "JPEG Image";
            case "image/gif" -> "GIF Image";
            case "application/pdf" -> "PDF Document";
            case "text/plain" -> "Text File";
            default -> "File";
        };
    }

    private String fileBadgeText(PostAttachment attachment) {
        return switch (attachment.getMimeType()) {
            case "image/jpeg" -> "JPG";
            case "image/gif" -> "GIF";
            case "application/pdf" -> "PDF";
            case "text/plain" -> "TXT";
            default -> "FILE";
        };
    }

    private String readableFileSize(long sizeBytes) {
        if (sizeBytes < 1024) {
            return sizeBytes + " B";
        }
        if (sizeBytes < 1024 * 1024) {
            return String.format(Locale.ENGLISH, "%.1f KB", sizeBytes / 1024.0);
        }
        return String.format(Locale.ENGLISH, "%.1f MB", sizeBytes / (1024.0 * 1024.0));
    }

    private String extractTextPreview(PostAttachment attachment) {
        String text = new String(attachment.getData(), StandardCharsets.UTF_8).trim();
        if (text.length() > 240) {
            return text.substring(0, 240) + "...";
        }
        return text.isBlank() ? "(Empty text file)" : text;
    }
}
