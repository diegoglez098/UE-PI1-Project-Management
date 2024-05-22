module org.ue.javafxgestiondeproyectos {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens org.ue.javafxgestiondeproyectos to javafx.fxml;
    exports org.ue.javafxgestiondeproyectos;
}