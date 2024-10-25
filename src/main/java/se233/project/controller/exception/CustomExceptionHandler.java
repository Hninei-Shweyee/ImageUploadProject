package se233.project.controller.exception;

import se233.project.Launcher;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import static se233.project.Launcher.stage;

public class CustomExceptionHandler extends Exception {
    public void showAlert(String s, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        stage.getIcons().add(new Image(Launcher.class.getResource("assets/AppIcon.jpg").toString()));
        alert.setTitle(type.name());
        alert.setContentText(s);
        alert.showAndWait();
    }


}


