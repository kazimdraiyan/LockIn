package app.lockin.lockin.client.controllers;

import app.lockin.lockin.client.MyApplication;
import app.lockin.lockin.client.elements.ProfileAvatar;
import app.lockin.lockin.common.models.Chat;
import app.lockin.lockin.common.models.ConversationData;
import app.lockin.lockin.common.models.Message;
import app.lockin.lockin.common.models.Attachment;
import app.lockin.lockin.common.models.MessageDelivery;
import app.lockin.lockin.common.models.UserPosts;
import app.lockin.lockin.common.models.UserProfile;
import app.lockin.lockin.common.requests.CreateMessageRequest;
import app.lockin.lockin.common.requests.FetchRequest;
import app.lockin.lockin.common.requests.FetchType;
import app.lockin.lockin.common.requests.FetchMessagesRequest;
import app.lockin.lockin.common.response.Response;
import app.lockin.lockin.common.response.ResponseStatus;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
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
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import app.lockin.lockin.client.utils.TextFormatter;

public class MessagesController {
    private static final long MAX_ATTACHMENT_SIZE_BYTES = 10L * 1024 * 1024; // 10 MB // TODO: Increase the limit
    private static final DateTimeFormatter MESSAGE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM, hh:mm a", Locale.ENGLISH).withZone(ZoneId.systemDefault());

    @FXML private ScrollPane messagesScrollPane;
    @FXML private VBox messagesContainer;
    @FXML private TextField messageInputField;
    @FXML private Button attachFileBtn;
    @FXML private Button sendButton;

    private final Set<String> renderedMessageIds = new HashSet<>(); // TODO: Check
    private MessengerController messengerController;
    private Chat currentChat;
    private Path selectedAttachmentPath;
    private final HashMap<String, Image> senderProfileImages = new HashMap<>();

    @FXML
    public void initialize() {
        renderPlaceholder("Select a chat to view messages.");
        setComposerEnabled(false);
        updateAttachmentIndicator();
    }

    public void setMessengerController(MessengerController messengerController) {
        this.messengerController = messengerController;
    }

    public String getCurrentChatUsername() {
        return currentChat == null ? null : currentChat.getName();
    }

    public void openConversation(Chat chat) {
        currentChat = chat;
        selectedAttachmentPath = null;
        updateAttachmentIndicator();
        messageInputField.clear();
        messageInputField.setPromptText("Write a message...");
        setComposerEnabled(chat != null);

        if (chat == null) {
            renderPlaceholder("Select a chat to view messages.");
            return;
        }

        String targetUsername = chat.getName(); // TODO: Rename "targetUsername" to something more general to include group chats
        renderPlaceholder("Loading messages...");
        new Thread(() -> {
            try {
                if (chat.isCommonChat()) {
                    loadCommonChatProfileImages();
                } else {
                    senderProfileImages.clear();
                }

                Response response = MyApplication.clientManager.sendRequest(new FetchMessagesRequest(targetUsername));
                if (response == null || response.getStatus() != ResponseStatus.SUCCESS) {
                    String errorMessage = response == null ? "Could not load messages." : response.getMessage();
                    Platform.runLater(() -> {
                        if (isCurrentConversation(targetUsername)) {
                            renderPlaceholder(errorMessage);
                        }
                    });
                    return;
                }

                ConversationData conversationData = (ConversationData) response.getData();
                // TODO: Learn more about Platform.runLater
                Platform.runLater(() -> {
                    if (!isCurrentConversation(targetUsername)) {
                        return;
                    }
                    currentChat = conversationData.getChat();
                    renderConversation(conversationData);
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    if (isCurrentConversation(targetUsername)) {
                        renderPlaceholder("Could not load messages.");
                    }
                });
            }
        }).start();
    }

    public void handleRealtimeDelivery(MessageDelivery delivery) {
        if (delivery == null || delivery.getChat() == null || delivery.getMessage() == null) {
            return;
        }
        if (!isCurrentConversation(delivery.getChat().getName())) {
            return;
        }

        currentChat = delivery.getChat(); // TODO: Maybe unnecessary
        appendMessage(delivery.getMessage());
    }

    @FXML
    public void onAttachFile(ActionEvent actionEvent) {
        if (currentChat == null) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select an attachment");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All files", "*.*"),
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif")
        );

        Window window = attachFileBtn.getScene() == null ? null : attachFileBtn.getScene().getWindow();
        java.io.File chosenFile = fileChooser.showOpenDialog(window);
        selectedAttachmentPath = chosenFile == null ? null : chosenFile.toPath();
        updateAttachmentIndicator();
        if (selectedAttachmentPath != null) {
            messageInputField.setPromptText("Attachment ready: " + selectedAttachmentPath.getFileName());
        }
    }

    @FXML
    public void onSendMessage(ActionEvent actionEvent) {
        if (currentChat == null) {
            return;
        }

        String text = messageInputField.getText() == null ? "" : messageInputField.getText().trim();
        if (text.isEmpty() && selectedAttachmentPath == null) {
            messageInputField.setPromptText("Write a message or attach a file.");
            return;
        }

        setComposerEnabled(false);
        new Thread(() -> {
            try {
                Attachment attachment = createAttachmentFromPath(selectedAttachmentPath);
                Response response = MyApplication.clientManager.sendRequest(
                        new CreateMessageRequest(currentChat.getId(), currentChat.isCommonChat() ? null : currentChat.getName(), text, attachment, null) // TODO: Add reply feature
                );

                if (response != null && response.getStatus() == ResponseStatus.SUCCESS) {
                    MessageDelivery delivery = (MessageDelivery) response.getData();
                    Platform.runLater(() -> {
                        currentChat = delivery.getChat();
                        appendMessage(delivery.getMessage());
                        if (messengerController != null) {
                            messengerController.onLocalMessage(delivery);
                        }
                        // TODO: Extract the following clearing code to a function
                        messageInputField.clear();
                        messageInputField.setPromptText("Write a message...");
                        selectedAttachmentPath = null;
                        updateAttachmentIndicator();
                        setComposerEnabled(true);
                    });
                } else {
                    String message = response == null ? "Could not send message." : response.getMessage();
                    Platform.runLater(() -> {
                        messageInputField.setPromptText(message);
                        setComposerEnabled(true);
                    });
                }
            } catch (IOException e) {
                Platform.runLater(() -> {
                    messageInputField.setPromptText("An unknown error occurred.");
                    setComposerEnabled(true);
                });
            }
        }).start();
    }

    private void renderConversation(ConversationData conversationData) {
        messagesContainer.getChildren().clear();
        renderedMessageIds.clear();

        if (conversationData.getMessages() == null || conversationData.getMessages().isEmpty()) {
            renderPlaceholder("No messages yet. Start the conversation.");
            return;
        }

        for (Message message : conversationData.getMessages()) {
            appendMessage(message);
        }
    }

    private void appendMessage(Message message) {
        if (message == null || message.getId() == null || renderedMessageIds.contains(message.getId())) {
            return;
        }

        clearPlaceholderIfNeeded();
        renderedMessageIds.add(message.getId());
        messagesContainer.getChildren().add(buildMessageNode(message));
        scrollToBottom();
    }


    private VBox buildMessageNode(Message message) {
        boolean commonChat = isCommonConversation();
        boolean outgoing = !commonChat && isOutgoing(message);

        VBox wrapper = new VBox(4);
        wrapper.setFillWidth(true);

        HBox row = new HBox();
        row.setAlignment(commonChat ? Pos.TOP_LEFT : (outgoing ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT));

        VBox bubbleBox = new VBox(6);
        bubbleBox.setAlignment(commonChat ? Pos.CENTER_LEFT : (outgoing ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT));
        bubbleBox.setMaxWidth(420);

        if (message.getText() != null && !message.getText().isBlank()) {
            Label textLabel = new Label(message.getText());
            textLabel.setWrapText(true);
            textLabel.getStyleClass().add(outgoing ? "message-bubble-text-out" : "message-bubble-text-in");
            bubbleBox.getChildren().add(textLabel);
        }

        if (message.getAttachment() != null) {
            bubbleBox.getChildren().add(buildAttachmentNode(message.getAttachment(), outgoing));
        }

        Label timeLabel = new Label(MESSAGE_TIME_FORMAT.format(Instant.ofEpochMilli(message.getCreatedAt())));
        timeLabel.getStyleClass().add("message-meta");
        bubbleBox.getChildren().add(timeLabel);

        if (commonChat) {
            ProfileAvatar senderAvatar = createSenderAvatar(message.getSenderUsername());
            Label senderLabel = new Label(message.getSenderUsername());
            senderLabel.getStyleClass().add("text-strong");

            VBox contentBox = new VBox(4);
            contentBox.setAlignment(Pos.CENTER_LEFT);
            contentBox.getChildren().addAll(senderLabel, bubbleBox);

            row.setSpacing(8);
            row.getChildren().addAll(senderAvatar, contentBox);
        } else {
            row.getChildren().add(bubbleBox);
        }

        wrapper.getChildren().add(row);
        return wrapper;
    }

    private VBox buildAttachmentNode(Attachment attachment, boolean outgoing) {
        VBox attachmentBox = new VBox(8);
        String bubbleStyle = outgoing ? "message-bubble-out" : "message-bubble-in";

        if (attachment.getMimeType() != null && attachment.getMimeType().startsWith("image/")) {
            ImageView imageView = new ImageView(new Image(new ByteArrayInputStream(attachment.getData())));
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(220);
            attachmentBox.getChildren().add(imageView);
        }

        HBox fileRow = new HBox(10);
        fileRow.setAlignment(Pos.CENTER_LEFT);
        fileRow.getStyleClass().add(bubbleStyle);

        Label iconLabel = new Label(TextFormatter.fileBadgeText(attachment.getMimeType()));
        iconLabel.getStyleClass().add("file-icon");

        VBox metaBox = new VBox(2);
        Label fileNameLabel = new Label(attachment.getOriginalFileName());
        fileNameLabel.getStyleClass().add("text-strong");
        Label fileMetaLabel = new Label(TextFormatter.readableFileSize(attachment.getData().length) + "  |  " + TextFormatter.readableFileType(attachment.getMimeType()));
        fileMetaLabel.getStyleClass().add("file-meta");
        metaBox.getChildren().addAll(fileNameLabel, fileMetaLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button saveButton = new Button("DL");
        saveButton.setPrefSize(30, 30);
        saveButton.getStyleClass().add("action-icon-button");
        saveButton.setOnAction(event -> downloadAttachment(attachment));

        fileRow.getChildren().addAll(iconLabel, metaBox, spacer, saveButton);
        attachmentBox.getChildren().add(fileRow);
        return attachmentBox;
    }

    private void downloadAttachment(Attachment attachment) {
        try {
            Path downloadsDir = Path.of(System.getProperty("user.home"), "Downloads", "LockIn");
            Files.createDirectories(downloadsDir); // Creates if it doesn't exist

            Path targetPath = downloadsDir.resolve(attachment.getOriginalFileName());
            if (Files.exists(targetPath)) {
                targetPath = downloadsDir.resolve(System.currentTimeMillis() + "_" + attachment.getOriginalFileName());
            }

            Files.copy(new ByteArrayInputStream(attachment.getData()), targetPath, StandardCopyOption.REPLACE_EXISTING);
            messageInputField.setPromptText("Saved to " + targetPath.getFileName());
            // TODO: Show the success message in little popup/toast instead of showing in the message field
        } catch (IOException e) {
            messageInputField.setPromptText("Could not save attachment.");
        }
    }

    private Attachment createAttachmentFromPath(Path filePath) throws IOException {
        if (filePath == null) {
            return null;
        }
        if (!Files.exists(filePath)) {
            throw new IOException("Selected file no longer exists.");
        }

        long size = Files.size(filePath); // In bytes
        if (size > MAX_ATTACHMENT_SIZE_BYTES) {
            throw new IOException("File is too large. Limit is 10 MB.");
        }

        return new Attachment(
                filePath.getFileName().toString(),
                Files.probeContentType(filePath),
                Files.readAllBytes(filePath)
        );
    }

    private void renderPlaceholder(String text) {
        messagesContainer.getChildren().clear();
        renderedMessageIds.clear();

        HBox placeholderRow = new HBox();
        placeholderRow.setAlignment(Pos.CENTER);
        placeholderRow.setPadding(new Insets(40, 0, 40, 0));

        Label placeholderLabel = new Label(text);
        placeholderLabel.getStyleClass().add("muted-text");
        placeholderRow.getChildren().add(placeholderLabel);
        messagesContainer.getChildren().add(placeholderRow);
        scrollToBottom();
    }

    // TODO: Learn more
    private void clearPlaceholderIfNeeded() {
        if (messagesContainer.getChildren().size() == 1
                && messagesContainer.getChildren().getFirst() instanceof HBox placeholderRow
                && placeholderRow.getChildren().size() == 1
                && placeholderRow.getChildren().getFirst() instanceof Label) {
            messagesContainer.getChildren().clear();
        }
    }

    private void scrollToBottom() {
        Platform.runLater(() -> messagesScrollPane.setVvalue(1.0));
    }

    private void setComposerEnabled(boolean enabled) {
        messageInputField.setDisable(!enabled);
        attachFileBtn.setDisable(!enabled);
        sendButton.setDisable(!enabled);
    }

    private void updateAttachmentIndicator() {
        attachFileBtn.setText(selectedAttachmentPath == null ? "+" : "1");
    }

    private boolean isCurrentConversation(String username) {
        return currentChat != null && currentChat.getName().equals(username);
    }

    private boolean isCommonConversation() {
        return currentChat != null && currentChat.isCommonChat();
    }

    private ProfileAvatar createSenderAvatar(String username) {
        ProfileAvatar avatar = new ProfileAvatar();
        avatar.setSize(30);
        avatar.setText(extractInitial(username));

        Image image = senderProfileImages.get(username);
        if (image != null) {
            avatar.setImage(image);
        }
        return avatar;
    }

    private String extractInitial(String username) {
        if (username == null || username.isBlank()) {
            return "?";
        }
        return String.valueOf(username.trim().charAt(0)).toUpperCase(Locale.ENGLISH);
    }

    private void loadCommonChatProfileImages() {
        senderProfileImages.clear();
        loadOwnProfileImage();
        loadOtherUsersProfileImages();
    }

    private void loadOwnProfileImage() {
        try {
            Response response = MyApplication.clientManager.sendRequest(new FetchRequest(FetchType.PROFILE));
            if (response != null && response.getStatus() == ResponseStatus.SUCCESS && response.getData() instanceof UserPosts pageData) {
                UserProfile profile = pageData.getProfile();
                if (profile != null) {
                    Image image = toImage(profile.getProfilePicture());
                    if (image != null) {
                        senderProfileImages.put(profile.getUsername(), image);
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }

    private void loadOtherUsersProfileImages() {
        try {
            Response response = MyApplication.clientManager.sendRequest(new FetchRequest(FetchType.USER_SEARCH, ""));
            if (response == null || response.getStatus() != ResponseStatus.SUCCESS) {
                return;
            }

            @SuppressWarnings("unchecked")
            java.util.ArrayList<UserProfile> users = (java.util.ArrayList<UserProfile>) response.getData();
            for (UserProfile user : users) {
                if (user == null) {
                    continue;
                }
                Image image = toImage(user.getProfilePicture());
                if (image != null) {
                    senderProfileImages.put(user.getUsername(), image);
                }
            }
        } catch (IOException ignored) {
        }
    }

    private Image toImage(Attachment profilePicture) {
        if (profilePicture == null || profilePicture.getData().length == 0) {
            return null;
        }
        return new Image(new ByteArrayInputStream(profilePicture.getData()));
    }

    private boolean isOutgoing(Message message) {
        String username = MyApplication.clientManager.getAuthenticatedUsername();
        return username != null && username.equals(message.getSenderUsername());
    }
}
