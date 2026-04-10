package app.lockin.lockin.client.elements;

import app.lockin.lockin.client.models.ChatListItem;
import app.lockin.lockin.client.utils.AvatarFactory;
import app.lockin.lockin.common.models.Chat;
import app.lockin.lockin.common.models.Attachment;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.ByteArrayInputStream;

public class ChatCell extends ListCell<ChatListItem> {
    private final ProfileAvatar avatar = new ProfileAvatar();
    private final Label nameLabel = new Label();
    private final Label msgLabel = new Label();
    private final VBox textContainer = new VBox(3);
    private final Label timeLabel = new Label();
    private final Label badgeLabel = new Label();
    private final VBox rightContainer = new VBox(5);
    private final HBox root = new HBox(12);

    public ChatCell() {
        avatar.setSize(44);

        nameLabel.getStyleClass().add("chat-name");
        msgLabel.getStyleClass().add("chat-msg");
        msgLabel.setMaxWidth(160);
        msgLabel.setEllipsisString("...");

        textContainer.getChildren().addAll(nameLabel, msgLabel);
        HBox.setHgrow(textContainer, Priority.ALWAYS);
        textContainer.setAlignment(Pos.CENTER_LEFT);

        timeLabel.getStyleClass().add("chat-time");

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
            setMinHeight(USE_COMPUTED_SIZE);
            setPrefHeight(USE_COMPUTED_SIZE);
            setMaxHeight(USE_COMPUTED_SIZE);
            getStyleClass().remove("transparent-list");
            return;
        }
        setMinHeight(USE_COMPUTED_SIZE);
        setPrefHeight(USE_COMPUTED_SIZE);
        setMaxHeight(USE_COMPUTED_SIZE);

        if (!getStyleClass().contains("transparent-list")) {
            getStyleClass().add("transparent-list");
        }

        boolean commonChat = item.getChat() != null && item.getChat().isCommonChat();
        if (commonChat) {
            avatar.setText(Chat.COMMON_CHAT_NAME);
            avatar.setImage(AvatarFactory.createCommonChat(44).getImage());
        } else {
            avatar.setText(item.getUserName());
            Attachment pic = item.getChat() == null ? null : item.getChat().getProfilePicture();
            if (pic != null && pic.getData().length > 0) {
                avatar.setImage(new Image(new ByteArrayInputStream(pic.getData())));
            } else {
                avatar.setImage(null);
            }
        }
        nameLabel.setText(item.getUserName());
        msgLabel.setText(item.getLastMessage());
        timeLabel.setText(item.getTimeAgo());

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
