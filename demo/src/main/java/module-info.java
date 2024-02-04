module com.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires com.fasterxml.jackson.databind;
    opens com.example to javafx.fxml;
    exports com.example;
}
