package app.lockin.lockin.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SignUpController implements MainControllerAware {
    @FXML public TextField nameField;
    @FXML public PasswordField passwordField;
    @FXML public PasswordField confirmpasswordField;
    @FXML public Button signInButton;
    @FXML private Label welcomeText;

    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    protected void onHomeButtonClick() {
        mainController.navigateHome();
    }

    @FXML
    protected void onSignInLinkClick() {
        try { mainController.navigateTo("login-view.fxml"); }
        catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    protected void onBackButtonClick() {
        mainController.navigateBack();
    }
}