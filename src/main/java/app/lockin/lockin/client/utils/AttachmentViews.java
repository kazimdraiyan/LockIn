package app.lockin.lockin.client.utils;

import app.lockin.lockin.common.models.Attachment;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public final class AttachmentViews {
    private AttachmentViews() {
    }

    public static VBox buildFeedAttachment(
            Attachment attachment,
            boolean showTextPreview,
            boolean showDownload,
            EventHandler<ActionEvent> onDownload
    ) {
        VBox attachmentBox = new VBox(10);

        if (attachment.getMimeType() != null && attachment.getMimeType().startsWith("image/")) {
            ImageView imageView = new ImageView(new Image(new ByteArrayInputStream(attachment.getData())));
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(420);
            attachmentBox.getChildren().add(imageView);
        }

        if (showTextPreview && "text/plain".equals(attachment.getMimeType())) {
            Label previewLabel = new Label(extractTextPreview(attachment));
            previewLabel.setWrapText(true);
            previewLabel.getStyleClass().add("body-text");
            attachmentBox.getChildren().add(previewLabel);
        }

        attachmentBox.getChildren().add(buildFeedFilePreview(attachment, showDownload, onDownload));
        return attachmentBox;
    }

    private static HBox buildFeedFilePreview(
            Attachment attachment,
            boolean showDownload,
            EventHandler<ActionEvent> onDownload
    ) {
        HBox preview = new HBox(14);
        preview.setAlignment(Pos.CENTER_LEFT);
        preview.setPadding(new Insets(14));
        preview.getStyleClass().add("file-preview");

        Label iconLabel = new Label(TextFormatter.fileBadgeText(attachment.getMimeType()));
        iconLabel.getStyleClass().add("file-icon");

        VBox textBox = new VBox(3);
        Label fileNameLabel = new Label(attachment.getOriginalFileName());
        fileNameLabel.getStyleClass().add("text-strong");
        Label metaLabel = new Label(
                TextFormatter.readableFileSize(attachment.getData().length)
                        + "  |  "
                        + TextFormatter.readableFileType(attachment.getMimeType())
        );
        metaLabel.getStyleClass().add("file-meta");
        textBox.getChildren().addAll(fileNameLabel, metaLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        preview.getChildren().addAll(iconLabel, textBox, spacer);

        if (showDownload) {
            Button downloadButton = new Button("Download");
            downloadButton.getStyleClass().add("primary-button");
            downloadButton.setGraphic(UiIcons.icon("download", 14));
            if (onDownload != null) {
                downloadButton.setOnAction(onDownload);
            }
            preview.getChildren().add(downloadButton);
        }

        return preview;
    }

    private static String extractTextPreview(Attachment attachment) {
        String text = new String(attachment.getData(), StandardCharsets.UTF_8).trim();
        if (text.length() > 240) {
            return text.substring(0, 240) + "...";
        }
        return text.isBlank() ? "(Empty text file)" : text;
    }
}
