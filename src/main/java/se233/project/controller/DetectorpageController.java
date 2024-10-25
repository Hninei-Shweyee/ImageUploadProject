package se233.project.controller;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import se233.project.controller.exception.CustomExceptionHandler;

import javax.imageio.ImageIO;

public class DetectorpageController {

    private Stage stage;
    private Scene scene;

    @FXML
    private ImageView previewImg;  // Ensure this is correctly connected in FXML
    @FXML
    private Button BackBtn2;
    @FXML
    private Button applyBtn;
    @FXML
    private Button saveBtn2;
    @FXML
    private ComboBox<String> detectorBtn;
    @FXML
    private ComboBox<String> formatBtn;

    private CustomExceptionHandler cshandle = new CustomExceptionHandler();

    private ArrayList<Image> inputImages = new ArrayList<>();  // No need for @FXML here
    private ArrayList<Image> bufferedImages = new ArrayList<>();  // No need for @FXML here
    private int currentImageIndex = -1;

    public void initialize() {
        // Choosing edge detection functions
        ObservableList<String> functions = FXCollections.observableArrayList("Sobel", "RobertsCross", "Prewitt");
        detectorBtn.setItems(functions);
        detectorBtn.getSelectionModel().select("Sobel");

        // Choosing image file types
        ObservableList<String> fileTypes = FXCollections.observableArrayList("JPEG", "PNG");
        formatBtn.setItems(fileTypes);
        formatBtn.getSelectionModel().select("JPEG");
    }

    @FXML
    private void showNextImage() {
        if (!inputImages.isEmpty()) {
            currentImageIndex = (currentImageIndex + 1) % inputImages.size();  // Move to next image
            updateImageView();  // Update the preview
        }
    }

    @FXML
    private void showPreviousImage() {
        if (!inputImages.isEmpty()) {
            currentImageIndex = (currentImageIndex - 1 + inputImages.size()) % inputImages.size();  // Move to previous image
            updateImageView();  // Update the preview
        }
    }

    private void updateImageView() {
        if (currentImageIndex >= 0 && currentImageIndex < inputImages.size()) {
            Image img = inputImages.get(currentImageIndex);
            previewImg.setImage(img);  // Update the ImageView with the current image
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


    @FXML
    private void OnApplyEdgeDetector() {
        if (currentImageIndex < 0 || currentImageIndex >= inputImages.size()) {
            cshandle.showAlert("No image selected for edge detection!", Alert.AlertType.WARNING);
            return;
        }

        Image originalImage = inputImages.get(currentImageIndex);
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(originalImage, null);

        BufferedImage resultImage = null;

        // Apply the selected edge detection technique
        String selectedDetector = detectorBtn.getSelectionModel().getSelectedItem();
        switch (selectedDetector) {
            case "Sobel":
                resultImage = applySobelEdgeDetection(bufferedImage);
                break;
            case "RobertsCross":
                resultImage = applyRobertsCrossEdgeDetection(bufferedImage);
                break;
            case "Prewitt":
                resultImage = applyPrewittEdgeDetection(bufferedImage);
                break;
            default:
                cshandle.showAlert("Invalid edge detector selected!", Alert.AlertType.ERROR);
                return;
        }

        // Convert result back to JavaFX Image and display in ImageView
        Image fxImage = SwingFXUtils.toFXImage(resultImage, null);
        bufferedImages.add(fxImage);  // Add processed image to bufferedImages for saving
        previewImg.setImage(fxImage);  // Update the ImageView with the processed image
    }

    private BufferedImage applySobelEdgeDetection(BufferedImage inputImage) {
        // Sobel edge detection algorithm implementation
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();

        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Sobel kernels for X and Y gradients
        int[][] sobelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
        int[][] sobelY = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int pixelX = 0;
                int pixelY = 0;

                // Apply convolution with Sobel kernels
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int rgb = new Color(inputImage.getRGB(x + j, y + i)).getRed();
                        pixelX += rgb * sobelX[i + 1][j + 1];
                        pixelY += rgb * sobelY[i + 1][j + 1];
                    }
                }

                int magnitude = (int) Math.sqrt(pixelX * pixelX + pixelY * pixelY);
                magnitude = Math.min(255, Math.max(0, magnitude));

                outputImage.setRGB(x, y, new Color(magnitude, magnitude, magnitude).getRGB());
            }
        }
        return outputImage;
    }

    private BufferedImage applyRobertsCrossEdgeDetection(BufferedImage inputImage) {
        // Roberts Cross edge detection algorithm implementation
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();

        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Roberts Cross kernels for X and Y gradients
        int[][] robertsX = {{1, 0}, {0, -1}};
        int[][] robertsY = {{0, 1}, {-1, 0}};

        for (int y = 0; y < height - 1; y++) {
            for (int x = 0; x < width - 1; x++) {
                int pixelX = 0;
                int pixelY = 0;

                // Apply convolution with Roberts Cross kernels (2x2 matrix)
                int rgbTopLeft = new Color(inputImage.getRGB(x, y)).getRed();
                int rgbTopRight = new Color(inputImage.getRGB(x + 1, y)).getRed();
                int rgbBottomLeft = new Color(inputImage.getRGB(x, y + 1)).getRed();
                int rgbBottomRight = new Color(inputImage.getRGB(x + 1, y + 1)).getRed();

                // Compute gradients using Roberts Cross kernels
                pixelX = (rgbTopLeft * robertsX[0][0]) + (rgbBottomRight * robertsX[1][1]);
                pixelY = (rgbBottomLeft * robertsY[1][0]) + (rgbTopRight * robertsY[0][1]);

                // Calculate gradient magnitude
                int magnitude = (int) Math.sqrt((pixelX * pixelX) + (pixelY * pixelY));
                magnitude = Math.min(255, Math.max(0, magnitude));

                // Set the pixel to the calculated magnitude
                outputImage.setRGB(x, y, new Color(magnitude, magnitude, magnitude).getRGB());
            }
        }
        return outputImage;
    }


    private BufferedImage applyPrewittEdgeDetection(BufferedImage inputImage) {
        // Prewitt edge detection algorithm implementation
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();

        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Prewitt kernels for X and Y gradients
        int[][] prewittX = {{-1, 0, 1}, {-1, 0, 1}, {-1, 0, 1}};
        int[][] prewittY = {{-1, -1, -1}, {0, 0, 0}, {1, 1, 1}};

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int pixelX = 0;
                int pixelY = 0;

                // Apply convolution with Prewitt kernels
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int rgb = new Color(inputImage.getRGB(x + j, y + i)).getRed();
                        pixelX += rgb * prewittX[i + 1][j + 1];
                        pixelY += rgb * prewittY[i + 1][j + 1];
                    }
                }

                int magnitude = (int) Math.sqrt(pixelX * pixelX + pixelY * pixelY);
                magnitude = Math.min(255, Math.max(0, magnitude));

                outputImage.setRGB(x, y, new Color(magnitude, magnitude, magnitude).getRGB());
            }
        }
        return outputImage;
    }

    private String removeDoubleExtension(String filename, String selectedFiletype) {
        int lastIndex = filename.lastIndexOf("." + selectedFiletype);
        if (lastIndex != -1) {
            return filename.substring(0, lastIndex);
        }
        return filename;
    }
    @FXML
    private void SaveDetector() {
         if( formatBtn.getValue()!= null){
            FileChooser fileChooser = new FileChooser();
            String selectedFiletype = formatBtn.getValue().toLowerCase();

            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(selectedFiletype.toUpperCase() + " files", "*." + selectedFiletype)
            );

            File selectedFile = fileChooser.showSaveDialog(new Stage());
            // Remove the double extension if it exists
            String fileName = selectedFile.getName();
            fileName = removeDoubleExtension(fileName, selectedFiletype);
            // Now, fileName should have only one extension
            File newFile = new File(selectedFile.getParent(), fileName);
            Thread applyThread = new Thread(() -> {
                savefiles(bufferedImages, newFile, selectedFiletype);
            });

            applyThread.setDaemon(true); // Set the thread as a daemon to exit when the application exits
            applyThread.start();
        }

    }

    @FXML
    private void savefiles(ArrayList<Image> bufferedImages, File selectedFile,
                           String selectedFiletype) {

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        try{

            if( bufferedImages.size() == 1) {
                save_as_individual(bufferedImages,selectedFile,selectedFiletype);

            } else if (bufferedImages.size()> 1) {
                save_as_zip(bufferedImages,selectedFile,selectedFiletype);
            }
            Platform.runLater(() -> {
                cshandle.showAlert("You have saved your files successfully!", Alert.AlertType.INFORMATION);
            });


        } catch (Exception  e) {
            e.printStackTrace();
        }finally {
            executorService.shutdown();

        }

    }
    @FXML
    public void save_as_individual(ArrayList<Image> bufferedImages, File selectedFile,  String selectedFiletype){
        Image imgToSave = bufferedImages.get(0);
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imgToSave, null);
        BufferedImage bufferedImage1 = bufferAgain(bufferedImage);
        String fileName = selectedFile.getName()+"."+selectedFiletype;
        File outputFile = new File(selectedFile.getParent(), fileName);

        try {
            ImageIO.write(bufferedImage1, selectedFiletype, outputFile);
            System.out.println("Image saved successfully!" + outputFile);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error saving image: " + e.getMessage());
        }
    }
    @FXML
    public void save_as_zip(ArrayList<Image> bufferedImages, File selectedFile,  String selectedFiletype){
        String zipFileName = selectedFile.getAbsolutePath()+".zip";
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(new File(zipFileName)))) {
            for (int i = 0; i < bufferedImages.size(); i++) {
                Image imgToSave = bufferedImages.get(i);
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imgToSave, null);
                BufferedImage bufferedImage1 = bufferAgain(bufferedImage);
                String entryName = selectedFile.getName() + i+"."+selectedFiletype ;

                ZipEntry entry = new ZipEntry(entryName);
                zipOutputStream.putNextEntry(entry);

                ImageIO.write(bufferedImage1, selectedFiletype, zipOutputStream);
                zipOutputStream.closeEntry();

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BufferedImage bufferAgain(BufferedImage bufferedImage){
        BufferedImage bufferedImage1 = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = bufferedImage1.createGraphics();

        graphics2D.drawImage(bufferedImage, 0, 0, null);

        return bufferedImage1;
    }



    @FXML
    private void switchToMain(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/se233/project/mainpage.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.show();
    }
}
