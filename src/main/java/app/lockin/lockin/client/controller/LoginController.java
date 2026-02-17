package app.lockin.lockin.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController implements MainControllerAware {
    @FXML public TextField nameField;
    @FXML public CheckBox keepSignedInCheckBox;
    @FXML public PasswordField passwordField;
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
    protected void onSignUpLinkClick() {
        try { mainController.navigateTo("sign-up-view.fxml"); }
        catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    protected void onBackButtonClick() {
        mainController.navigateBack();
    }
}
