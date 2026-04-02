package app.lockin.lockin.client.elements;

import app.lockin.lockin.client.models.ChatListItem;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;

public class ChatCell extends ListCell<ChatListItem> {
    // Avatar
    private final ProfileAvatar avatar = new ProfileAvatar();

    // Text
    private final Label nameLabel = new Label();
    private final Label msgLabel = new Label();
    private final VBox textContainer = new VBox(3);

    // Right side (time + badge)
    private final Label timeLabel = new Label();
    private final Label badgeLabel = new Label();
    private final VBox rightContainer = new VBox(5);

    // Root
    private final HBox root = new HBox(12);

    public ChatCell() {
        avatar.setSize(44);

        // Name + message
        nameLabel.getStyleClass().add("chat-name");
        msgLabel.getStyleClass().add("chat-msg");
        msgLabel.setMaxWidth(160);
        msgLabel.setEllipsisString("...");

        textContainer.getChildren().addAll(nameLabel, msgLabel);
        HBox.setHgrow(textContainer, Priority.ALWAYS);
        textContainer.setAlignment(Pos.CENTER_LEFT);

        // Time label
        timeLabel.getStyleClass().add("chat-time");

        // Badge
        badgeLabel.setMinSize(20, 20);
        badgeLabel.setAlignment(Pos.CENTER);
        badgeLabel.getStyleClass().add("badge");

        rightContainer.setAlignment(Pos.TOP_RIGHT);
        rightContainer.getChildren().addAll(timeLabel, badgeLabel);

        root.setAlignment(Pos.CENTER_LEFT);
        root.setPadding(new Insets(8, 12, 8, 12));
        root.getChildren().addAll(avatar, textContainer, rightContainer);
    }

    @Override
    protected void updateItem(ChatListItem item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
            setText(null);
            getStyleClass().remove("transparent-list");
            return;
        }
            if (!getStyleClass().contains("transparent-list")) {
                getStyleClass().add("transparent-list");
            }
            // Avatar
            avatar.setText(item.getUserName());

            // Text
            nameLabel.setText(item.getUserName());
            msgLabel.setText(item.getLastMessage());

            // Badge — only show if unread > 0
            if (item.getUnreadCount() > 0) {
                badgeLabel.setText(String.valueOf(item.getUnreadCount()));
                badgeLabel.setVisible(true);
                badgeLabel.setManaged(true);
            } else {
                badgeLabel.setVisible(false);
                badgeLabel.setManaged(false);
            }

            setGraphic(root);

    }
}
