package app.lockin.lockin.client.elements;

import app.lockin.lockin.client.utils.AvatarFactory;
import app.lockin.lockin.common.models.Attachment;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class UserRowController {
    @FXML private HBox root;
    @FXML private ProfileAvatar avatar;
    @FXML private Label primaryLabel;
    @FXML private Label secondaryLabel;

    public void configure(
            String username,
            String primaryText,
            String secondaryText,
            double avatarSize,
            Attachment profilePicture,
            Runnable onClick
    ) {
        ProfileAvatar built = AvatarFactory.create(username, avatarSize, profilePicture);
        avatar.setSize(avatarSize);
        avatar.setText(built.getText());
        avatar.setImage(built.getImage());

        primaryLabel.setText(primaryText == null ? "" : primaryText);
        if (secondaryText == null || secondaryText.isBlank()) {
            secondaryLabel.setManaged(false);
            secondaryLabel.setVisible(false);
            secondaryLabel.setText("");
        } else {
            secondaryLabel.setManaged(true);
            secondaryLabel.setVisible(true);
            secondaryLabel.setText(secondaryText);
        }

        if (onClick == null) {
            avatar.setCursor(Cursor.DEFAULT);
            primaryLabel.setCursor(Cursor.DEFAULT);
            primaryLabel.getStyleClass().remove("clickable");
            avatar.setOnMouseClicked(null);
            primaryLabel.setOnMouseClicked(null);
            root.setOnMouseClicked(null);
            return;
        }

        avatar.setCursor(Cursor.HAND);
        primaryLabel.setCursor(Cursor.HAND);
        if (!primaryLabel.getStyleClass().contains("clickable")) {
            primaryLabel.getStyleClass().add("clickable");
        }
        avatar.setOnMouseClicked(event -> onClick.run());
        primaryLabel.setOnMouseClicked(event -> onClick.run());
        root.setOnMouseClicked(event -> onClick.run());
    }

    public HBox getRoot() {
        return root;
    }
}
