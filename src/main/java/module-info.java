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
    opens app.lockin.lockin to javafx.fxml;
    opens app.lockin.lockin.client.controller to javafx.fxml;

    // TODO: Only export necessary packages
    exports app.lockin.lockin;
    exports app.lockin.lockin.client;
}