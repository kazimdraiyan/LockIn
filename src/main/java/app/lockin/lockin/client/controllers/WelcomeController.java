package app.lockin.lockin.client.controllers;

import javafx.fxml.FXML;

import java.io.IOException;

public class WelcomeController implements MainControllerAware {
    public MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        mainController.setNavBar(true, "Welcome to LockIn", false);
        mainController.setRefreshButtonVisible(false);
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
