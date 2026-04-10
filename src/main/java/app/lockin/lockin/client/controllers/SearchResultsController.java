package app.lockin.lockin.client.controllers;

import app.lockin.lockin.client.MyApplication;
import app.lockin.lockin.client.elements.UserRowController;
import app.lockin.lockin.client.models.NavUiConfig;
import app.lockin.lockin.client.utils.UserIdentityRows;
import app.lockin.lockin.common.models.UserProfile;
import app.lockin.lockin.common.requests.FetchRequest;
import app.lockin.lockin.common.requests.FetchType;
import app.lockin.lockin.common.response.Response;
import app.lockin.lockin.common.response.ResponseStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;

public class SearchResultsController implements MainControllerAware {
    @FXML
    private Label queryLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private VBox resultsContainer;

    private MainController mainController;
    private String query;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        query = mainController.getSearchQuery();
        mainController.applyNavUi(new NavUiConfig(true, "Search", true, false, true));
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
        card.setPadding(new Insets(14, 16, 14, 16));
        card.getStyleClass().addAll("feed-card", "contact-item");
        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(event -> openProfile(user.getUsername()));

        HBox identityRow = createReusableUserRow(
                user.getUsername(),
                user.getUsername(),
                user.getDescription().isBlank() ? "No description yet." : user.getDescription(),
                48,
                user.getProfilePicture(),
                () -> openProfile(user.getUsername())
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        card.getChildren().addAll(identityRow, spacer);
        HBox.setHgrow(identityRow, Priority.ALWAYS);
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

    private HBox createReusableUserRow(
            String username,
            String primaryText,
            String secondaryText,
            double avatarSize,
            app.lockin.lockin.common.models.Attachment picture,
            Runnable onClick
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(MyApplication.getFXML("user-row.fxml"));
            HBox root = loader.load();
            UserRowController controller = loader.getController();
            controller.configure(username, primaryText, secondaryText, avatarSize, picture, onClick);
            return root;
        } catch (IOException ignored) {
            return UserIdentityRows.build(username, primaryText, secondaryText, avatarSize, picture, onClick);
        }
    }

}
