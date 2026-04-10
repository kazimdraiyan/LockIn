package app.lockin.lockin.client.utils;

import app.lockin.lockin.client.elements.ProfileAvatar;
import app.lockin.lockin.common.models.Attachment;
import app.lockin.lockin.common.models.Chat;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class UserIdentityRows {
    private UserIdentityRows() {
    }

    public static HBox build(
            String username,
            String secondaryText,
            double avatarSize,
            Attachment profilePicture,
            Runnable onUserClick
    ) {
        return build(username, username, secondaryText, avatarSize, profilePicture, onUserClick);
    }

    public static HBox build(
            String avatarUsername,
            String primaryText,
            String secondaryText,
            double avatarSize,
            Attachment profilePicture,
            Runnable onUserClick
    ) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setFillHeight(true);

        ProfileAvatar avatar = Chat.COMMON_CHAT_NAME.equals(avatarUsername)
                ? AvatarFactory.createCommonChat(avatarSize)
                : AvatarFactory.create(avatarUsername, avatarSize, profilePicture);
        Label primaryLabel = new Label(primaryText == null ? "" : primaryText);
        primaryLabel.getStyleClass().add("text-primary-strong");

        if (onUserClick != null) {
            avatar.setCursor(Cursor.HAND);
            primaryLabel.setCursor(Cursor.HAND);
            primaryLabel.getStyleClass().add("clickable");
            avatar.setOnMouseClicked(event -> onUserClick.run());
            primaryLabel.setOnMouseClicked(event -> onUserClick.run());
        }

        VBox metaBox = new VBox(2);
        metaBox.setAlignment(Pos.CENTER_LEFT);
        metaBox.getChildren().add(primaryLabel);
        if (secondaryText != null && !secondaryText.isBlank()) {
            Label secondaryLabel = new Label(secondaryText);
            secondaryLabel.getStyleClass().add("text-secondary");
            metaBox.getChildren().add(secondaryLabel);
        }

        row.getChildren().addAll(avatar, metaBox);
        return row;
    }
}
