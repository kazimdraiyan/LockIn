package app.lockin.lockin.client.controller;

import app.lockin.lockin.LockInApplication;
import app.lockin.lockin.server.request.LoginRequest;
import app.lockin.lockin.server.response.Response;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController implements MainControllerAware {
    @FXML public TextField usernameField;
    @FXML public CheckBox keepSignedInCheckBox;
    @FXML public PasswordField passwordField;
    @FXML public Button signInButton;

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
    protected void signIn() {
        String name = usernameField.getText();
        String password = passwordField.getText();

        // TODO: Show error message on the GUI
        if (name.isEmpty() || password.isEmpty()) {
            System.out.println("Please fill all the fields");
            return;
        }

        new Thread(() -> {
            try {
                LoginRequest request = new LoginRequest(usernameField.getText(), passwordField.getText());

                LockInApplication.clientManager.send(request);

                Response response = LockInApplication.clientManager.receive();
                System.out.println(response.getMessage());
                // TODO: Store authentication token
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
