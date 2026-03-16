package app.lockin.lockin.client.controller;

import app.lockin.lockin.LockInApplication;
import app.lockin.lockin.server.request.SignUpRequest;
import app.lockin.lockin.server.response.Response;
import app.lockin.lockin.server.response.ResponseStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SignUpController implements MainControllerAware {
    @FXML
    public TextField usernameField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public PasswordField confirmPasswordField;
    @FXML
    public Button signUpButton;

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
        try {
            mainController.navigateTo("login-view.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void signUp() {
        String name = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // TODO: Show error message on the GUI
        if (name.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            System.out.println("Please fill all the fields");
            return;
        }
        if (!password.equals(confirmPassword)) {
            System.out.println("Passwords do not match");
            return;
        }

        // Networking calls should be done in a separate thread so that JavaFX UI is not interrupted
        // TODO: Should I do the threading here?
        new Thread(() -> {
            try {
                SignUpRequest request = new SignUpRequest(usernameField.getText(), passwordField.getText());

                // TODO: Should I rename LockInApplication?
                LockInApplication.clientManager.send(request);

                Response response = LockInApplication.clientManager.receive();
                System.out.println(response.getMessage());
                System.out.println(response.getData());

                if (response.getStatus() == ResponseStatus.SUCCESS) {
                    LockInApplication.saveToken((String) response.getData());
                    Platform.runLater(() -> {
                        try { mainController.navigateTo("home-view.fxml"); }
                        catch (Exception e) { e.printStackTrace(); }
                    });
                } else {
                    // TODO: Show error on GUI using Platform.runLater as well
                    System.out.println("Login failed: " + response.getMessage());
                }

                // TODO: Learn more about updating UI safely
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}