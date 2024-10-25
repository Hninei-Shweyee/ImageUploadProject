package se233.project.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import se233.project.controller.exception.CustomExceptionHandler;

import javax.imageio.ImageIO;

public class cropPageController {

    private Stage stage;
    private Scene scene;

    @FXML
    private ImageView CropPreviewImg;
    @FXML
    private ComboBox<String> ImgFormat;
    @FXML
    private Pane pane;
    @FXML
    private Rectangle cropRect;

    private double rectX, rectY;
    private double mouseX, mouseY;

    private CustomExceptionHandler cshandle = new CustomExceptionHandler();

    private ArrayList<Image> inputImages = new ArrayList<>();
    private ArrayList<BufferedImage> bufferedImages = new ArrayList<>();  // To store cropped images
    private int currentImageIndex = -1;

    public void initialize() {
        // Choosing image file types
        ObservableList<String> fileTypes = FXCollections.observableArrayList("JPEG", "PNG");
        ImgFormat.setItems(fileTypes);
        ImgFormat.getSelectionModel().select("JPEG");

        // Initialize mouse handlers for crop rectangle adjustment
        setupCropRectangle();
    }

    // Mouse event methods for resizing the crop rectangle
    private void setupCropRectangle() {
        cropRect.setOnMousePressed(this::onRectanglePressed);
        cropRect.setOnMouseDragged(this::onRectangleDragged);
        pane.setOnMouseMoved(this::resizeOrMoveRectangle);  // Corrected to use the pane instead of the rectangle
    }

    private void onRectanglePressed(MouseEvent event) {
        rectX = cropRect.getX();
        rectY = cropRect.getY();
        mouseX = event.getSceneX();
        mouseY = event.getSceneY();
    }

    private void onRectangleDragged(MouseEvent event) {
        double deltaX = event.getSceneX() - mouseX;
        double deltaY = event.getSceneY() - mouseY;

        // Ensure the rectangle stays within bounds
        if (rectX + deltaX >= 0 && rectX + deltaX + cropRect.getWidth() <= CropPreviewImg.getFitWidth()) {
            cropRect.setX(rectX + deltaX);
        }
        if (rectY + deltaY >= 0 && rectY + deltaY + cropRect.getHeight() <= CropPreviewImg.getFitHeight()) {
            cropRect.setY(rectY + deltaY);
        }
    }

    private void resizeOrMoveRectangle(MouseEvent event) {
        double mouseX = event.getX();
        double mouseY = event.getY();
        if (isNearEdge(mouseX, mouseY)) {
            pane.setCursor(javafx.scene.Cursor.HAND);
            cropRect.setOnMouseDragged(this::resizeRectangle);
        } else {
            pane.setCursor(javafx.scene.Cursor.DEFAULT);
        }
    }

    private boolean isNearEdge(double mouseX, double mouseY) {
        double tolerance = 10.0;
        return mouseX >= cropRect.getX() - tolerance && mouseX <= cropRect.getX() + cropRect.getWidth() + tolerance &&
                mouseY >= cropRect.getY() - tolerance && mouseY <= cropRect.getY() + cropRect.getHeight() + tolerance;
    }

    private void resizeRectangle(MouseEvent event) {
        double newWidth = event.getX() - cropRect.getX();
        double newHeight = event.getY() - cropRect.getY();

        // Ensure the rectangle stays within bounds of the image
        if (newWidth > 0 && newHeight > 0 &&
                newWidth + cropRect.getX() <= CropPreviewImg.getFitWidth() &&
                newHeight + cropRect.getY() <= CropPreviewImg.getFitHeight()) {
            cropRect.setWidth(newWidth);
            cropRect.setHeight(newHeight);
        }
    }

    // Display the selected image
    @FXML
    private void updateImageView() {
        if (currentImageIndex >= 0 && currentImageIndex < inputImages.size()) {
            Image img = inputImages.get(currentImageIndex);
            CropPreviewImg.setImage(img);
        }
    }

    // Crop and save the current image

    @FXML
    private void cropCurrentImage() {
        if (currentImageIndex >= 0 && currentImageIndex < inputImages.size()) {
            Image img = inputImages.get(currentImageIndex);
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(img, null);

            // Get the original size of the image
            double originalWidth = img.getWidth();
            double originalHeight = img.getHeight();

            // Get the displayed size in the ImageView (note: the image might be scaled to fit)
            double displayedWidth = CropPreviewImg.getBoundsInParent().getWidth();
            double displayedHeight = CropPreviewImg.getBoundsInParent().getHeight();

            // Calculate the scale factor between the original and displayed image
            double scaleX = originalWidth / displayedWidth;
            double scaleY = originalHeight / displayedHeight;

            // Get the crop rectangle's displayed bounds and adjust for the scale
            int cropX = (int) (cropRect.getX() * scaleX);
            int cropY = (int) (cropRect.getY() * scaleY);
            int cropWidth = (int) (cropRect.getWidth() * scaleX);
            int cropHeight = (int) (cropRect.getHeight() * scaleY);

            // Ensure the crop rectangle is within the bounds of the original image
            if (cropX + cropWidth <= bufferedImage.getWidth() && cropY + cropHeight <= bufferedImage.getHeight()) {
                BufferedImage croppedImage = bufferedImage.getSubimage(cropX, cropY, cropWidth, cropHeight);
                bufferedImages.add(croppedImage);  // Save the cropped image in the list

                cshandle.showAlert("Cropped image added to list!", Alert.AlertType.INFORMATION);
            } else {
                cshandle.showAlert("Crop rectangle exceeds image bounds!", Alert.AlertType.ERROR);
            }
        }
    }

    // Save all cropped images (either as individual files or as a zip file)
    @FXML
    private void saveCroppedImages() {
        if (ImgFormat.getValue() != null) {
            FileChooser fileChooser = new FileChooser();
            String selectedFiletype = ImgFormat.getValue().toLowerCase();

            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(selectedFiletype.toUpperCase() + " files", "*." + selectedFiletype)
            );

            File selectedFile = fileChooser.showSaveDialog(new Stage());

            if (selectedFile != null) {
                if (bufferedImages.size() == 1) {
                    saveAsIndividual(bufferedImages.get(0), selectedFile, selectedFiletype);
                } else {
                    saveAsZip(bufferedImages, selectedFile, selectedFiletype);
                }
            }
        }
    }

    // Save a single cropped image as a file
    private void saveAsIndividual(BufferedImage croppedImage, File selectedFile, String selectedFiletype) {
        String fileName = selectedFile.getName() + "." + selectedFiletype;
        File outputFile = new File(selectedFile.getParent(), fileName);

        try {
            ImageIO.write(croppedImage, selectedFiletype, outputFile);
            cshandle.showAlert("Cropped image saved successfully!", Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            e.printStackTrace();
            cshandle.showAlert("Error saving image: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Save multiple cropped images as a zip file
    private void saveAsZip(List<BufferedImage> croppedImages, File selectedFile, String selectedFiletype) {
        String zipFileName = selectedFile.getAbsolutePath() + ".zip";
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(new File(zipFileName)))) {
            for (int i = 0; i < croppedImages.size(); i++) {
                BufferedImage croppedImage = croppedImages.get(i);
                String entryName = selectedFile.getName() + i + "." + selectedFiletype;

                ZipEntry entry = new ZipEntry(entryName);
                zipOutputStream.putNextEntry(entry);

                ImageIO.write(croppedImage, selectedFiletype, zipOutputStream);
                zipOutputStream.closeEntry();
            }
            cshandle.showAlert("Cropped images saved as zip file!", Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            e.printStackTrace();
            cshandle.showAlert("Error saving zip file: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Navigation between images
    @FXML
    private void showNextImage() {
        if (!inputImages.isEmpty()) {
            currentImageIndex = (currentImageIndex + 1) % inputImages.size();
            updateImageView();
        }
    }

    @FXML
    private void showPreviousImage() {
        if (!inputImages.isEmpty()) {
            currentImageIndex = (currentImageIndex - 1 + inputImages.size()) % inputImages.size();
            updateImageView();
        }
    }

    @FXML
    public void OnImgPreview(List<File> inputListViewItems) {
        // Load the images from the file list
        for (File inputListViewItem : inputListViewItems) {
            String filepath = inputListViewItem.getAbsolutePath();
            Image image = new Image("file:" + filepath);  // Load image from file path
            inputImages.add(image);  // Add image to the list
        }

        if (!inputImages.isEmpty()) {
            currentImageIndex = 0;  // Start with the first image
            updateImageView();  // Show the first image in the ImageView
        }

        System.out.println("Images from input listView are saved in image arraylist!");
    }

    // Switching to main page
    public void switchToMain(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/se233/project/mainpage.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.show();
    }
}
