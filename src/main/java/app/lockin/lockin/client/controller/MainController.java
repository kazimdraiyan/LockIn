package app.lockin.lockin.client.controller;

import app.lockin.lockin.MyApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.util.Stack;

// The wrapper of every page. Every page is rendered on top of this view.
public class MainController {
    private final Stack<Parent> history = new Stack<>();

    @FXML
    private BorderPane rootPane;

    @FXML
    public void initialize() throws IOException {
        if (MyApplication.clientManager.isLoggedIn) {
            navigateReplacement("home-view.fxml");
        } else {
            navigateReplacement("welcome-view.fxml");
        }
    }

    private Parent loadFXML(String fxmlFileName) throws IOException {
        FXMLLoader loader = new FXMLLoader(MyApplication.getFXML(fxmlFileName));
        Parent page = loader.load();
        Object controller = loader.getController();

        // Inject this MainController object to the controller of the loaded page
        if (controller instanceof MainControllerAware awareController) {
            awareController.setMainController(this);
        }
        return page;
    }

    // Keeps the history
    public void navigatePush(String fxmlFileName) throws IOException {
        // Add the current page to history
        Parent current = (Parent) rootPane.getCenter();
        if (current != null) history.push(current);

        rootPane.setCenter(loadFXML(fxmlFileName));
    }

    // Deletes the history
    public void navigateReplacement(String fxmlFileName) throws IOException {
        history.clear();
        rootPane.setCenter(loadFXML(fxmlFileName));
    }

    // TODO: Implement back button in every page
    public void navigatePop() {
        if (!history.isEmpty()) {
            rootPane.setCenter(history.pop());
        }
    }
}
