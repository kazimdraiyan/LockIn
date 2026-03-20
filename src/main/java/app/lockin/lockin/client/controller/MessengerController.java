package app.lockin.lockin.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.io.IOException;

// TODO: rename to a better name\
// TODO: fix dark mode in messages view
public class MessengerController implements MainControllerAware {
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void onAttachFile(ActionEvent actionEvent) {
        // TODO: Learn more about ActionEvent
    }

    public void onSendMessage(ActionEvent actionEvent) {

    }

    @FXML
    protected void onHomeButtonClick() throws IOException {
        mainController.navigateReplacement("home-view.fxml");
    }
}
