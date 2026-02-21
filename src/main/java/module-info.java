module app.lockin.lockin {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires com.fasterxml.jackson.databind;

    opens app.lockin.lockin to javafx.fxml;
    exports app.lockin.lockin;
    exports app.lockin.lockin.client.controller;
    opens app.lockin.lockin.client.controller to javafx.fxml;
}