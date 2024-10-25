package se233.project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.io.IOException;

public class Launcher extends Application {

    public static Stage stage;
    @Override
    public void start(Stage stage) throws IOException {
            this.stage = stage;
            FXMLLoader loader =new FXMLLoader (getClass().getResource("/se233/project/mainpage.fxml"));
            Scene scene = new Scene(loader.load());
            this.stage.setTitle(" JavaFX Project 1");
            this.stage.getIcons().add(new Image(Launcher.class.getResource("assets/AppIcon.jpg").toString()));
            this.stage.setResizable(false);
            this.stage.setScene(scene);
            this.stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    public static Stage getStage() {
        return stage;
    }

    public static void setStage(Stage stage) {
        Launcher.stage = stage;
    }

}
