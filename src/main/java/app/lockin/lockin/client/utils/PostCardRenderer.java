package app.lockin.lockin.client.utils;

import app.lockin.lockin.common.models.Attachment;
import app.lockin.lockin.common.models.Comment;
import app.lockin.lockin.common.models.Post;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Map;

public final class PostCardRenderer {
    private PostCardRenderer() {
    }

    public interface Actions {
        void openUserProfile(String username);
        void deletePost(String postId, Button deleteButton);
        VBox buildAttachmentNode(Attachment attachment);
        String formatTimestamp(long createdAt);
    }

    public static VBox buildPostCard(
            Post post,
            boolean ownProfile,
            String authenticatedUsername,
            Map<String, Attachment> profilePicturesByUsername,
            VBox trailingCommentsNode,
            Actions actions
    ) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("feed-card", "post-thread-card");
        card.setPadding(new Insets(14, 16, 14, 16));

        card.getChildren().add(buildPostHeader(post, ownProfile, authenticatedUsername, profilePicturesByUsername, actions));

        if (post.getTextContent() != null && !post.getTextContent().isBlank()) {
            Label contentLabel = new Label(post.getTextContent());
            contentLabel.setWrapText(true);
            contentLabel.getStyleClass().add("body-text");
            card.getChildren().add(contentLabel);
        }

        if (post.getAttachment() != null) {
            card.getChildren().add(actions.buildAttachmentNode(post.getAttachment()));
        }

        card.getChildren().add(new Separator());
        card.getChildren().add(buildCommentsSection(post, profilePicturesByUsername, trailingCommentsNode, actions));
        return card;
    }

    private static HBox buildPostHeader(
            Post post,
            boolean ownProfile,
            String authenticatedUsername,
            Map<String, Attachment> profilePicturesByUsername,
            Actions actions
    ) {
        String authorUsername = ownProfile ? authenticatedUsername : post.getAuthorUsername();
        String title = ownProfile && authenticatedUsername == null ? "Posted by you" : authorUsername;
        HBox header = UserIdentityRows.build(
                authorUsername == null ? "ME" : authorUsername,
                title == null ? "Posted by you" : title,
                actions.formatTimestamp(post.getCreatedAt()),
                42,
                profilePicturesByUsername.get(authorUsername),
                () -> actions.openUserProfile(authorUsername)
        );

        if (ownProfile) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Button deleteButton = new Button("Delete post");
            deleteButton.getStyleClass().add("danger-button");
            deleteButton.setOnAction(event -> actions.deletePost(post.getId(), deleteButton));
            header.getChildren().addAll(spacer, deleteButton);
        }
        return header;
    }

    private static VBox buildCommentsSection(
            Post post,
            Map<String, Attachment> profilePicturesByUsername,
            VBox trailingCommentsNode,
            Actions actions
    ) {
        VBox section = new VBox(10);
        section.getStyleClass().add("comments-section");

        if (post.getComments().isEmpty()) {
            Label emptyLabel = new Label("No comments yet.");
            emptyLabel.getStyleClass().add("muted-text");
            section.getChildren().add(emptyLabel);
        } else {
            for (Comment comment : post.getComments()) {
                section.getChildren().add(buildCommentCard(comment, profilePicturesByUsername, actions));
            }
        }

        if (trailingCommentsNode != null) {
            section.getChildren().add(trailingCommentsNode);
        }
        return section;
    }

    private static VBox buildCommentCard(
            Comment comment,
            Map<String, Attachment> profilePicturesByUsername,
            Actions actions
    ) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.getStyleClass().add("comment-card");

        HBox header = UserIdentityRows.build(
                comment.getAuthorUsername(),
                actions.formatTimestamp(comment.getCreatedAt()),
                32,
                profilePicturesByUsername.get(comment.getAuthorUsername()),
                () -> actions.openUserProfile(comment.getAuthorUsername())
        );
        card.getChildren().add(header);

        if (comment.getTextContent() != null && !comment.getTextContent().isBlank()) {
            Label contentLabel = new Label(comment.getTextContent());
            contentLabel.setWrapText(true);
            contentLabel.getStyleClass().add("body-text");
            card.getChildren().add(contentLabel);
        }

        if (comment.getAttachment() != null) {
            card.getChildren().add(actions.buildAttachmentNode(comment.getAttachment()));
        }
        return card;
    }
}
