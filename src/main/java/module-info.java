module org.example {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires com.almasb.fxgl.all;
    requires org.json;

    opens org.example to javafx.fxml;
    exports org.example;
}