module app.lockin.lockin {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens app.lockin.lockin to javafx.fxml;
    exports app.lockin.lockin;
}