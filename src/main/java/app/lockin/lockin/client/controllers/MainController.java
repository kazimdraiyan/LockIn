package app.lockin.lockin.client.controllers;

import app.lockin.lockin.client.MyApplication;
import app.lockin.lockin.client.NavCallState;
import app.lockin.lockin.client.elements.ProfileAvatar;
import app.lockin.lockin.client.models.NavUiConfig;
import app.lockin.lockin.client.models.Page;
import app.lockin.lockin.client.utils.ThemeManager;
import app.lockin.lockin.common.models.CallSignal;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.application.Platform;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.util.Stack;
import java.util.function.Consumer;

// The wrapper of every page. Every page is rendered on top of this view.
public class MainController {
    private final Stack<Page> history = new Stack<>();
    public String viewedProfileUsername;
    public String selectedChatUsername;

    @FXML
    public Label title;

    @FXML
    public HBox navBar;

    @FXML
    public Button backButton;

    @FXML
    public HBox searchBar;

    @FXML
    private SearchBarController searchBarController;

    @FXML
    public ImageView themeToggleIcon;

    @FXML
    public ImageView settingsIcon;
    @FXML
    public Button settingsButton;
    @FXML
    public ImageView backIcon;
    @FXML
    public HBox callStatusBar;
    @FXML
    public ProfileAvatar callStatusAvatar;
    @FXML
    public Label callStatusLabel;
    @FXML
    public Button callStatusPrimaryButton;
    @FXML
    public Button callStatusSecondaryButton;
    @FXML
    public Button refreshButton;
    @FXML
    public ImageView refreshIcon;
    @FXML
    private BorderPane rootPane;

    private final Consumer<CallSignal> callSignalListener = signal -> Platform.runLater(this::refreshCallStatusBar);

    @FXML
    public void initialize() throws IOException {
        MyApplication.clientManager.addCallSignalListener(callSignalListener);
        if (MyApplication.clientManager.isLoggedIn) {
            navigateReplacingRoot("home-view.fxml");
        } else {
            navigateReplacingRoot("welcome-view.fxml");
        }
        searchBarController.setPromptText("Search");
        searchBar.getStyleClass().add("search-bar-navbar");
        searchBarController.getInputField().setOnAction(event -> submitSearch());
        refreshCallStatusBar();
        loadNavBarIcons();
    }

    public void setNavBar(boolean showNavBar, String titleString, boolean showSearchBar) {
        navBar.setManaged(showNavBar);
        backButton.setDisable(history.size() <= 1);
        title.setText(titleString);
        searchBar.setVisible(showSearchBar);
        searchBar.setManaged(showSearchBar);
    }

    public void setRefreshButtonVisible(boolean visible) {
        refreshButton.setVisible(visible);
        refreshButton.setManaged(visible);
    }

    public void setSettingsButtonVisible(boolean visible) {
        settingsButton.setVisible(visible);
        settingsButton.setManaged(visible);
    }

    public void applyNavUi(NavUiConfig config) {
        if (config == null) {
            return;
        }
        setNavBar(config.isShowNavBar(), config.getTitle(), config.isShowSearchBar());
        setRefreshButtonVisible(config.isShowRefreshButton());
        setSettingsButtonVisible(config.isShowSettingsButton() && MyApplication.clientManager.isLoggedIn);
    }

    public void openProfile(String username) throws IOException {
        viewedProfileUsername = username;
        navigatePush("profile-view.fxml");
    }

    private Page loadFXML(String fxmlFileName) throws IOException {
        FXMLLoader loader = new FXMLLoader(MyApplication.getFXML(fxmlFileName));
        Parent page = loader.load();
        return new Page(page, loader);
    }

    // Keeps the history
    public void navigatePush(String fxmlFileName) throws IOException {
        Page loadedPage = loadFXML(fxmlFileName);
        rootPane.setCenter(loadedPage.root);
        if (rootPane.getScene() != null) {
            ThemeManager.applyCurrentTheme(rootPane.getScene());
        }
        history.push(loadedPage);
        // Inject this MainController object to the controller of the loaded page
        if (loadedPage.fxmlLoader.getController() instanceof MainControllerAware awareController) {
            awareController.setMainController(this);
        }
    }

    // Replaces the last added page of the history
    public void navigateReplacingCurrent(String fxmlFileName) throws IOException {
        if (!history.isEmpty()) {
            history.pop();
        }
        navigatePush(fxmlFileName);
    }

    // Deletes the history
    public void navigateReplacingRoot(String fxmlFileName) throws IOException {
        history.clear();
        navigatePush(fxmlFileName);
    }

    // Go back to the last page
    public void navigatePop() {
        if (!history.isEmpty()) {
            history.pop(); // Removes the current page
            rootPane.setCenter(history.peek().root); // Sets the last page
            // Initializes the last page controller
            if (history.peek().fxmlLoader.getController() instanceof MainControllerAware awareController) {
                awareController.setMainController(this);
            }
        }
    }

    public void toggleTheme() {
        ThemeManager.toggle();
        loadNavBarIcons();
    }

    public void openSettings() {
        try {
            navigatePush("settings-view.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openChat(String username) throws IOException {
        selectedChatUsername = username;
        navigatePush("messenger-view.fxml");
    }

    @FXML
    public void onCallStatusPrimary(ActionEvent actionEvent) {
        NavCallState state = MyApplication.clientManager.getNavCallState();
        String callId = MyApplication.clientManager.getNavCallId();
        if (callId == null || callId.isBlank()) {
            return;
        }
        new Thread(() -> {
            try {
                switch (state) {
                    case INCOMING_RING -> MyApplication.clientManager.answerCall(callId, true);
                    case OUTGOING_RING, ACTIVE -> MyApplication.clientManager.endCall(callId);
                    default -> {
                    }
                }
            } catch (IOException ignored) {
            }
        }, "lockin-nav-call-primary").start();
    }

    @FXML
    public void onCallStatusSecondary(ActionEvent actionEvent) {
        if (MyApplication.clientManager.getNavCallState() != NavCallState.INCOMING_RING) {
            return;
        }
        String callId = MyApplication.clientManager.getNavCallId();
        if (callId == null || callId.isBlank()) {
            return;
        }
        new Thread(() -> {
            try {
                MyApplication.clientManager.answerCall(callId, false);
            } catch (IOException ignored) {
            }
        }, "lockin-nav-call-secondary").start();
    }

    public void refreshPage() throws IOException {
        if (!history.isEmpty()) {
            history.pop();
            navigatePush("home-view.fxml");
        }
    }

    private void loadNavBarIcons() {
        themeToggleIcon.setImage(new Image(
                MyApplication.getIcon(ThemeManager.isDarkMode() ? "light_mode.png" : "dark_mode.png").toExternalForm()
        ));
        settingsIcon.setImage(new Image(
                MyApplication.getIcon(ThemeManager.isDarkMode() ? "settings-white.png" : "settings.png").toExternalForm()
        ));
        backIcon.setImage(new Image(
                MyApplication.getIcon(ThemeManager.isDarkMode() ? "back-white.png" : "back.png").toExternalForm()
        ));
        refreshIcon.setImage(new Image(
                MyApplication.getIcon(ThemeManager.isDarkMode() ? "refresh-white.png" : "refresh.png").toExternalForm()
        ));
    }

    public String getSearchQuery() {
        return searchBarController.getInputField().getText().trim();
    }

    private void submitSearch() {
        String query = searchBarController.getInputField().getText();
        if (query == null || query.isBlank() || !MyApplication.clientManager.isLoggedIn) {
            return;
        }
        try {
            navigatePush("search-results-view.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshCallStatusBar() {
        NavCallState state = MyApplication.clientManager.getNavCallState();
        String peer = MyApplication.clientManager.getNavPeerUsername();
        String callId = MyApplication.clientManager.getNavCallId();

        if (state == NavCallState.IDLE || peer == null || peer.isBlank() || callId == null || callId.isBlank()) {
            hideCallStatusBar();
            return;
        }

        callStatusBar.setVisible(true);
        callStatusBar.setManaged(true);
        callStatusAvatar.setText(peer);

        switch (state) {
            case INCOMING_RING -> {
                callStatusLabel.setText("Incoming from " + peer);
                callStatusPrimaryButton.setText("Accept");
                callStatusPrimaryButton.setVisible(true);
                callStatusPrimaryButton.setManaged(true);
                callStatusSecondaryButton.setText("Reject");
                callStatusSecondaryButton.setVisible(true);
                callStatusSecondaryButton.setManaged(true);
            }
            case OUTGOING_RING -> {
                callStatusLabel.setText("Calling " + peer + "…");
                callStatusPrimaryButton.setText("Cancel");
                callStatusPrimaryButton.setVisible(true);
                callStatusPrimaryButton.setManaged(true);
                callStatusSecondaryButton.setVisible(false);
                callStatusSecondaryButton.setManaged(false);
            }
            case ACTIVE -> {
                callStatusLabel.setText("In call with " + peer);
                callStatusPrimaryButton.setText("End call");
                callStatusPrimaryButton.setVisible(true);
                callStatusPrimaryButton.setManaged(true);
                callStatusSecondaryButton.setVisible(false);
                callStatusSecondaryButton.setManaged(false);
            }
            default -> hideCallStatusBar();
        }
    }

    private void hideCallStatusBar() {
        callStatusBar.setVisible(false);
        callStatusBar.setManaged(false);
        callStatusAvatar.setText("");
        callStatusLabel.setText("");
    }
}
