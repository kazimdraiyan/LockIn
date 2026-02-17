package app.lockin.lockin.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.util.Stack;

// TODO: Add back button navigation

public class MainController {
    @FXML
    private Label welcomeText;

    @FXML
    private BorderPane rootPane;

    private Parent homePage;
    @FXML
    public void initialize() {
        homePage = (Parent) rootPane.getCenter();
    }

    public void navigateHome() {
        history.clear();
        rootPane.setCenter(homePage);
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
    @FXML
    protected void onLoginButtonClick() {
        try {
            navigateTo("login-view.fxml");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onSignUpButtonClick() {
        try {
            navigateTo("sign-up-view.fxml");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Page switching
    public void loadPage(String fxml) throws IOException {
        Parent page = FXMLLoader.load(getClass().getResource("/app/lockin/lockin/" + fxml));
        rootPane.setCenter(page);
    }
}
