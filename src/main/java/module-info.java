module com.example.analizador_lexico {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens com.example.analizador_lexico to javafx.fxml;
    exports com.example.analizador_lexico;
}