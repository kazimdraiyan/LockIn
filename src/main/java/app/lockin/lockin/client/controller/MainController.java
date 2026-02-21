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

    @FXML
    protected void onLoginButtonClick() {
        try {
            loadPage("login-view.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onSignUpButtonClick() {
        try {
            loadPage("sign-up-view.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Page switching
    public void loadPage(String fxml) throws IOException {
        Parent page = FXMLLoader.load(getClass().getResource("/app/lockin/lockin/" + fxml));
        rootPane.setCenter(page);
    }
}
