module se233.project {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires org.apache.commons.compress;
    requires org.apache.commons.imaging;
    requires java.desktop;
    requires javafx.swing;


    opens se233.project.controller to javafx.fxml;
    exports se233.project.controller;
    exports se233.project to javafx.graphics;
}