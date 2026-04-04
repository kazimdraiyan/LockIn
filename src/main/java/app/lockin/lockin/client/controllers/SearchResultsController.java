package app.lockin.lockin.client.controllers;

import app.lockin.lockin.client.MyApplication;
import app.lockin.lockin.client.elements.ProfileAvatar;
import app.lockin.lockin.common.models.UserProfile;
import app.lockin.lockin.common.requests.FetchRequest;
import app.lockin.lockin.common.requests.FetchType;
import app.lockin.lockin.common.response.Response;
import app.lockin.lockin.common.response.ResponseStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import javafx.scene.image.Image;

public class SearchResultsController implements MainControllerAware {
    @FXML private Label queryLabel;
    @FXML private Label statusLabel;
    @FXML private VBox resultsContainer;

    private MainController mainController;
    private String query;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        this.query = mainController.consumeSearchQuery();
        if (query == null || query.isBlank()) {
            query = mainController.getSearchField().getText();
        }
        mainController.setNavBar(true, "Search", true);
        queryLabel.setText(query == null || query.isBlank() ? "All users" : "Results for \"" + query + "\"");
        loadResults();
    }

    private void loadResults() {
        resultsContainer.getChildren().setAll(new Label("Searching users..."));
        statusLabel.setText("");
        new Thread(() -> {
            try {
                Response response = sendRequest(new FetchRequest(FetchType.USER_SEARCH, query));
                if (response != null && response.getStatus() == ResponseStatus.SUCCESS) {
                    @SuppressWarnings("unchecked")
                    ArrayList<UserProfile> users = (ArrayList<UserProfile>) response.getData();
                    Platform.runLater(() -> renderResults(users));
                } else {
                    String message = response == null ? "Could not search users." : response.getMessage();
                    Platform.runLater(() -> renderError(message));
                }
            } catch (IOException e) {
                Platform.runLater(() -> renderError("Could not search users."));
            }
        }).start();
    }

    private void renderResults(ArrayList<UserProfile> users) {
        resultsContainer.getChildren().clear();
        if (users == null || users.isEmpty()) {
            Label emptyLabel = new Label("No signed up user matched your search.");
            emptyLabel.getStyleClass().add("muted-text");
            resultsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (UserProfile user : users) {
            resultsContainer.getChildren().add(buildUserCard(user));
        }
    }

    private void renderError(String message) {
        resultsContainer.getChildren().clear();
        Label errorLabel = new Label(message);
        errorLabel.getStyleClass().add("error-label");
        resultsContainer.getChildren().add(errorLabel);
    }

    private HBox buildUserCard(UserProfile user) {
        HBox card = new HBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.getStyleClass().addAll("feed-card", "contact-item");

        ProfileAvatar avatar = new ProfileAvatar();
        avatar.setSize(48);
        avatar.setText(extractInitials(user.getUsername()));
        if (user.getProfilePicture() != null && user.getProfilePicture().getData().length > 0) {
            avatar.setImage(new Image(new ByteArrayInputStream(user.getProfilePicture().getData())));
        }

        VBox textBox = new VBox(4);
        Label usernameLabel = new Label(user.getUsername());
        usernameLabel.getStyleClass().add("text-strong");
        Label descriptionLabel = new Label(user.getDescription().isBlank() ? "No description yet." : user.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.getStyleClass().add("muted-text");
        textBox.getChildren().addAll(usernameLabel, descriptionLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button openButton = new Button("View profile");
        openButton.getStyleClass().add("primary-button");
        openButton.setOnAction(event -> openProfile(user.getUsername()));

        card.getChildren().addAll(avatar, textBox, spacer, openButton);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        return card;
    }

    private void openProfile(String username) {
        try {
            mainController.openProfile(username);
        } catch (IOException e) {
            statusLabel.setText("Could not open profile.");
        }
    }

    private Response sendRequest(app.lockin.lockin.common.requests.Request request) throws IOException {
        synchronized (MyApplication.clientManager) {
            return MyApplication.clientManager.sendRequest(request);
        }
    }

    private String extractInitials(String username) {
        if (username == null || username.isBlank()) {
            return "?";
        }
        return username.trim().substring(0, Math.min(2, username.trim().length())).toUpperCase(Locale.ENGLISH);
    }
}
