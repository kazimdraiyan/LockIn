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
    @FXML
    private BorderPane rootPane;

    private Parent homePage;

    @FXML
    public void initialize() throws IOException {
        if (MyApplication.clientManager.isLoggedIn) {
            navigateTo("home-view.fxml");
        }
        else {
            navigateTo("welcome-view.fxml");
        }
    }

    public void navigateHome() throws IOException {
        history.clear();
        rootPane.setCenter(homePage);
        navigateTo("home-view.fxml");
    }

    private final Stack<Parent> history =  new Stack<>();

    public void navigateTo(String fxml) throws IOException {
        Parent current = (Parent) rootPane.getCenter();
        if (current!=null) history.push(current);
        loadCenter(fxml);
    }

    public void navigateBack(){
        if(!history.isEmpty()){
            rootPane.setCenter(history.pop());
        }
    }

    private void loadCenter(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/app/lockin/lockin/" + fxml)
        );
        Parent page = loader.load();

        Object controller = loader.getController();
        if (controller instanceof MainControllerAware aware){
            aware.setMainController(this);
        }
        rootPane.setCenter(page);
    }
}
