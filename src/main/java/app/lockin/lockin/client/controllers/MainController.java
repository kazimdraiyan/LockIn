package app.lockin.lockin.client.controllers;

import app.lockin.lockin.client.MyApplication;
import app.lockin.lockin.client.models.Page;
import app.lockin.lockin.client.utils.ThemeManager;
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

import java.io.IOException;
import java.util.Stack;

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
    public ImageView backIcon;
    @FXML
    public Button refreshButton;
    @FXML
    public ImageView refreshIcon;
    @FXML
    private BorderPane rootPane;

    @FXML
    public void initialize() throws IOException {
        if (MyApplication.clientManager.isLoggedIn) {
            navigateReplacingRoot("home-view.fxml");
        } else {
            navigateReplacingRoot("welcome-view.fxml");
        }
        searchBarController.setPromptText("Search");
        searchBar.getStyleClass().add("search-bar-navbar");
        searchBarController.getInputField().setOnAction(event -> submitSearch());
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
}
