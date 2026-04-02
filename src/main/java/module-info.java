module app.lockin.lockin {
    requires java.prefs; // Store user settings
    requires com.fasterxml.jackson.databind; // Work with JSON

    requires javafx.controls; // UI components
    requires org.controlsfx.controls; // Extra UI controls
    requires com.dlsc.formsfx; // Form utilities
    requires javafx.fxml;
    requires javafx.graphics;
    requires org.kordamp.bootstrapfx.core;

    // Runtime reflection // TODO: Learn more
    opens app.lockin.lockin.client.controllers to javafx.fxml;
    opens app.lockin.lockin.client.elements to javafx.fxml;

    // TODO: Only export necessary packages
    exports app.lockin.lockin.client;
    exports app.lockin.lockin.client.elements;
    exports app.lockin.lockin.common.models;
    exports app.lockin.lockin.common.requests;
    exports app.lockin.lockin.common.response;
}
