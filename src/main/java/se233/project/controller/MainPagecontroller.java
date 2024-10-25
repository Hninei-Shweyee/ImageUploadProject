package se233.project.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import se233.project.Launcher;
import se233.project.controller.exception.CustomExceptionHandler;

public class MainPagecontroller {

    private Stage stage;
    private Scene scene;
    @FXML
    private ListView<File> inputListView;
    @FXML
    Button DetectorBtn;
    @FXML
    Button CropBtn;
    private CustomExceptionHandler Exception = new CustomExceptionHandler();


    public void initialize() {

        //For the list View Cell
        inputListView.setCellFactory(param -> new ListCell<>() { //Custom Cell in ListView
            @Override
            protected void updateItem(File file, boolean empty) {
                super.updateItem(file, empty);
                if (empty || file == null) {
                    setText(null);
                } else {
                    setText(file.getName());
                }
            }
        });
        //Dragging the item
        inputListView.setOnDragOver(dragEvent -> {
            Dragboard db = dragEvent.getDragboard();

            boolean isAccepted = false;
            List<File> files = db.getFiles();

            for (File file : files) {
                String fileName = file.getName().toLowerCase();

                if (db.hasFiles() && fileName.endsWith(".png")
                        || fileName.toLowerCase().endsWith(".jpeg")
                        || fileName.toLowerCase().endsWith(".jpg")
                        || fileName.toLowerCase().endsWith(".zip")) {
                    isAccepted = true;
                    if (isAccepted) {
                        dragEvent.acceptTransferModes(TransferMode.COPY);
                    }


                } else {
                    dragEvent.consume();

                }
            }
        });
        DetectorBtn.setOnAction(e -> {
            if (!inputListView.getItems().isEmpty()) {
                try {
//load the resize screen
                    FXMLLoader DetectorLoader = new FXMLLoader(Launcher.class.getResource("/se233/project/EdgeDetectorpage.fxml"));
                    Scene scene1 = new Scene(DetectorLoader.load(), 600, 400);
                    DetectorpageController detectorController = DetectorLoader.getController();

                    List<File> inputListViewItems = inputListView.getItems();
                    detectorController.OnImgPreview(inputListViewItems);
                    Stage currentStage = (Stage) DetectorBtn.getScene().getWindow();
                    currentStage.setScene(scene1);
                    currentStage.show();

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                Exception.showAlert("Upload files before clicking this button.", Alert.AlertType.ERROR);
            }
        });
        CropBtn.setOnAction(e -> {
            if (!inputListView.getItems().isEmpty()) {
                try {
//load the resize screen
                    FXMLLoader CropLoader = new FXMLLoader(Launcher.class.getResource("/se233/project/Croppage.fxml"));
                    Scene scene1 = new Scene(CropLoader.load(), 600, 400);
                    cropPageController cropController = CropLoader.getController();

                    List<File> inputListViewItems = inputListView.getItems();
                    cropController.OnImgPreview(inputListViewItems);
                    Stage currentStage = (Stage) CropBtn.getScene().getWindow();
                    currentStage.setScene(scene1);
                    currentStage.show();

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                Exception.showAlert("Upload files before clicking this button.", Alert.AlertType.ERROR);
            }
        });
        //checking the target items are acceptable or not
        inputListView.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();

            List<File> files = dragboard.getFiles();
            boolean success = false;

            for (int i = 0 ;i<files.size();i++)  {
                if(dragboard.hasFiles()&&
                        ( dragboard.getFiles().get(i).getName().toLowerCase().endsWith(".png")
                                ||dragboard.getFiles().get(i).getName().toLowerCase().endsWith(".jpg")
                                ||dragboard.getFiles().get(i).getName().toLowerCase().endsWith(".jpeg"))){
                    success = true;
                    File file = dragboard.getFiles().get(i);
                    inputListView.getItems().add(file);

                } else if (dragboard.getFiles().get(i).getName().toLowerCase().endsWith(".zip")) {
                    success = true;
                    File file = dragboard.getFiles().get(i);

                    zipArchive(file,inputListView);
                }
            }



            event.setDropCompleted(success);
            event.consume();
        });
    }
    public void zipArchive(File zipFile, ListView<File> inputListView) {
        try {
            try (ZipArchiveInputStream zipInput = new ZipArchiveInputStream(new FileInputStream(zipFile))) {
                ZipArchiveEntry entry;
                while ((entry = zipInput.getNextZipEntry()) != null) {
                    if (!entry.isDirectory()) {
                        String entryName = entry.getName();
                        if(entryName.endsWith(".jpg") || entryName.endsWith(".png")){
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            File extractedFile = new File("path_to_extracted_files", entryName);
                            extractedFile.getParentFile().mkdirs();
                            try (FileOutputStream outputStream = new FileOutputStream(extractedFile)) {
                                while ((bytesRead = zipInput.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, bytesRead);
                                }
                            }
                            inputListView.getItems().add(new File(extractedFile.getAbsolutePath()));
                        }

                    }
                }

            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void OnDeleteBtn()
    {     System.out.println("Delete was clicked!!");

        if (inputListView != null) {
            if(inputListView.getSelectionModel().getSelectedItem()== null){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText(null);
                alert.setTitle("Error");
                alert.setContentText("You didn't select anything!\n Select any file you want to delete.");
                alert.showAndWait();
            }
            inputListView.getItems().remove(inputListView.getSelectionModel().getSelectedItem());
        }
    }


    public ListView<File> getInputListView() {
        return inputListView;
    }


}





