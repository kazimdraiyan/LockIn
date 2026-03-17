package app.lockin.lockin.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;

import java.io.IOException;

import app.lockin.lockin.util.ThemeManager;


public class WelcomeController implements MainControllerAware {
    @FXML
    private ToggleButton darkModeToggle;

    MainController mainController;

    @FXML
    public void initialize() {
        darkModeToggle.setSelected(ThemeManager.isDarkMode());
    }

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    protected void onLoginButtonClick() {
        try {
            mainController.navigateTo("login-view.fxml");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onSignUpButtonClick() {
        try {
            mainController.navigateTo("sign-up-view.fxml");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleDarkModeToggle() {
        ThemeManager.toggle();
        darkModeToggle.setText(ThemeManager.isDarkMode() ? "Disable Darkmode" : "Enable Darkmode");
    }
}
