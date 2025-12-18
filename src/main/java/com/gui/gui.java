package com.gui;

import com.domain.Instance;
import com.utils.CsvReader;
import com.utils.LabelEncoder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class gui extends Application {
    private TableView<Instance<Double, Double>> tableView;

    @Override
    public void start(Stage stage) {
        try {
            tableView = new TableView<>();
            tableView.setStyle("-fx-font-size: 18; -fx-base: #3B4252; -fx-control-inner-background: #3B4252; -fx-text-fill: white;");

            HBox.setHgrow(tableView, Priority.ALWAYS);
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();

            stage.setWidth(bounds.getWidth() * 0.80);
            stage.setHeight(bounds.getHeight() * 0.85);

            VBox leftPane = createLeftPane(stage);

            HBox mainLayout = new HBox(20);
            mainLayout.setPadding(new Insets(20));
            mainLayout.setStyle("-fx-background-color: #2E3440");
            mainLayout.getChildren().addAll(leftPane, tableView);

            Scene scene = new Scene(mainLayout);

            stage.setScene(scene);
            stage.show();
            stage.setTitle("Binary Classification Library");
            stage.centerOnScreen();

        } catch (Exception e) {
            showAlert(e.getMessage());
            Platform.exit();
        }
    }

    private VBox createLeftPane(Stage stage) {
        VBox vBox = new VBox(15);
        vBox.setPrefWidth(300);
        vBox.setMinWidth(300);
        vBox.setAlignment(Pos.TOP_LEFT);

        Label labelIndex = createLabel("Label Index");

        TextField labelIndexTextField = createTextField("1");
        labelIndexTextField.setPromptText("Set label index in dataset");

        Label selectedFileLabel = createLabel("No file selected");
        selectedFileLabel.setStyle("-fx-text-fill: #D8DEE9; -fx-font-style: italic");
        selectedFileLabel.setWrapText(true);

        Button chooseDatasetButton = createButton("Choose CSV Dataset");

        chooseDatasetButton.setOnAction((event) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open CSV Dataset");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

            String currentDir = System.getProperty("user.dir");
            File resourceDir = new File(currentDir, "src/main/resources");
            fileChooser.setInitialDirectory(resourceDir);

            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                try {
                    int index = Integer.parseInt(labelIndexTextField.getText().trim());
                    List<Instance<Double, String>> rawData = CsvReader.loadFromCsv(file.getAbsolutePath(), index);
                    List<Instance<Double, Double>> processedData = LabelEncoder.encode(rawData);
                    List<String> headers = CsvReader.readHeaders(file.getAbsolutePath(), index);
                    
                    selectedFileLabel.setText("Selected: " + file.getName());

                    updateTable(processedData, headers);
                    tableView.setItems(FXCollections.observableArrayList(processedData));

                } catch (NumberFormatException e) {
                    showAlert("Please enter a valid integer for label index");
                } catch (Exception e) {
                    showAlert(e.getMessage());
                }
            }
        });

        vBox.getChildren().addAll(labelIndex, labelIndexTextField, selectedFileLabel, chooseDatasetButton);

        return vBox;
    }

    private void updateTable(List<Instance<Double, Double>> dataset, List<String> headers) {
        tableView.getColumns().clear();

        int featuresCount = dataset.getFirst().getInput().size();

        for (int i = 0; i < featuresCount; i++) {
            int featureIndex = i;
            TableColumn<Instance<Double, Double>, Double> column = new TableColumn<>(headers.get(featureIndex));
            column.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getInput().get(featureIndex)));
            tableView.getColumns().add(column);
        }

        TableColumn<Instance<Double, Double>, Double> labelColumn = new TableColumn<>("Output");
        labelColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getOutput()));
        labelColumn.setStyle("-fx-font-weight: bold");
        tableView.getColumns().add(labelColumn);
    }

    private Button createButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefWidth(0);
        HBox.setHgrow(button, Priority.ALWAYS);
        button.setStyle("-fx-text-fill: white; -fx-background-color: #483D8B; -fx-font-weight: bold; -fx-font-size: 16; -fx-cursor: hand");
        return button;
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16");
        return label;
    }

    private TextField createTextField(String text) {
        TextField textField = new TextField(text);
        textField.setStyle("-fx-text-fill: white; -fx-background-color: #4C566A; -fx-font-size: 16");
        return textField;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
