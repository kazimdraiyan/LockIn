package app.lockin.lockin.client.controllers;

import app.lockin.lockin.client.models.NavUiConfig;
import javafx.fxml.FXML;

import java.io.IOException;

public class WelcomeController implements MainControllerAware {
    public MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        mainController.applyNavUi(new NavUiConfig(true, "Welcome to LockIn", false, false, true));
    }

    @FXML
    protected void onLoginButtonClick() {
        try {
            mainController.navigatePush("login-view.fxml");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onSignUpButtonClick() {
        try {
            mainController.navigatePush("sign-up-view.fxml");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
