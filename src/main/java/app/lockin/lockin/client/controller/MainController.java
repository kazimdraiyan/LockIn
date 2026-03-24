package app.lockin.lockin.client.controller;

import app.lockin.lockin.MyApplication;
import app.lockin.lockin.client.model.Page;
import app.lockin.lockin.util.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.Stack;

// The wrapper of every page. Every page is rendered on top of this view.
public class MainController {
    private final Stack<Page> history = new Stack<>();

    @FXML
    public Label title;

    @FXML
    public HBox navBar;

    @FXML
    public Button backButton;

    @FXML
    public HBox searchBar;

    @FXML
    public ImageView themeToggleIcon;

    @FXML
    private BorderPane rootPane;

    @FXML
    public void initialize() throws IOException {
        if (MyApplication.clientManager.isLoggedIn) {
            navigateReplacingRoot("home-view.fxml");
        } else {
            navigateReplacingRoot("welcome-view.fxml");
        }
        themeToggleIcon.setImage(new Image(
                MyApplication.getIcon(ThemeManager.isDarkMode() ? "light_mode.png" : "dark_mode.png").toExternalForm()
        ));
    }

    public void setNavBar(boolean showNavBar, String titleString, boolean showSearchBar) {
        navBar.setManaged(showNavBar);
        backButton.setDisable(history.size() <= 1);
        title.setText(titleString);
        searchBar.setVisible(showSearchBar);
        searchBar.setManaged(showSearchBar);
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
        themeToggleIcon.setImage(new Image(
                MyApplication.getIcon(ThemeManager.isDarkMode() ? "light_mode.png" : "dark_mode.png").toExternalForm()
        ));
    }
}
