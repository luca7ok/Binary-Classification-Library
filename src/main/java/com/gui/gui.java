package com.gui;

import com.domain.Instance;
import com.evaluation.*;
import com.models.GaussianNaiveBayes;
import com.models.Model;
import com.utils.CsvReader;
import com.utils.LabelEncoder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class gui extends Application {
    private TableView<Instance<Double, Double>> tableView;
    private Slider splitSlider;
    private List<Instance<Double, Double>> processedData;
    private ComboBox<String> classifierSelection;
    private VBox visualResultsContainer;
    private TextArea results;
    private Label placeholder;

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
            VBox middlePane = createMiddlePane(stage);

            HBox mainLayout = new HBox(20);
            mainLayout.setPadding(new Insets(20));
            mainLayout.setStyle("-fx-background-color: #2E3440");
            mainLayout.getChildren().addAll(leftPane, middlePane, tableView);

            Scene scene = new Scene(mainLayout);

            stage.setScene(scene);
            stage.setResizable(false);
            stage.setTitle("Binary Classification Library");
            stage.centerOnScreen();
            stage.show();

            Platform.runLater(() -> {
                Node axis = splitSlider.lookup(".axis");
                if (axis != null) {
                    axis.setStyle("-fx-tick-label-fill: white;");
                }
            });

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

        Label labelIndex = createLabel("Label Index (0-based)");

        TextField labelIndexTextField = createTextField("Set label index in dataset");
        labelIndexTextField.setText("1");

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
                    clearResults();

                    int index = Integer.parseInt(labelIndexTextField.getText().trim());
                    List<Instance<Double, String>> rawData = CsvReader.loadFromCsv(file.getAbsolutePath(), index);
                    processedData = LabelEncoder.encode(rawData);
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

        Label classifierLabel = createLabel("Classifier model:");
        VBox.setMargin(classifierLabel, new Insets(40, 0, 0, 0));

        Label hyperparametersLabel = createLabel("Hyperparameters:");
        VBox.setMargin(hyperparametersLabel, new Insets(25, 0, 0, 0));
        TextField hyperparametersTextField = createTextField("Hyperparameters");

        classifierSelection = new ComboBox<>();
        classifierSelection.getItems().addAll("Naive Bayes", "Decision Tree", "Logistic Regression");
        classifierSelection.setValue("Naive Bayes");
        classifierSelection.setMaxWidth(Double.MAX_VALUE);
        classifierSelection.setStyle("-fx-base: #4C566A; -fx-text-fill: white; -fx-font-size: 16");

        Label trainSplitLabel = createLabel("Train split: 80%");
        VBox.setMargin(trainSplitLabel, new Insets(25, 0, 0, 0));
        Label testSplitLabel = createLabel("Test split: 20%");

        splitSlider = new Slider(10, 90, 80);
        splitSlider.setShowTickLabels(true);
        splitSlider.setShowTickMarks(true);
        splitSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double value = newVal.doubleValue();
            trainSplitLabel.setText(String.format("Train split: %.0f%%", value));
            testSplitLabel.setText(String.format("Test split: %.0f%%", 100.0 - value));
        });

        Button trainButton = createButton("Train and Evaluate");
        VBox.setMargin(trainButton, new Insets(25, 0, 0, 0));
        trainButton.setOnAction(event -> trainAndEvaluate());

        vBox.getChildren().addAll(labelIndex, labelIndexTextField,
                selectedFileLabel, chooseDatasetButton,
                classifierLabel, classifierSelection,
                hyperparametersLabel, hyperparametersTextField,
                trainSplitLabel, testSplitLabel, splitSlider,
                trainButton);

        return vBox;
    }

    private VBox createMiddlePane(Stage stage) {
        VBox vBox = new VBox(15);
        vBox.setPrefWidth(450);
        vBox.setMinWidth(450);
        vBox.setAlignment(Pos.TOP_CENTER);
        vBox.setPadding(new Insets(0, 10, 0, 10));

        Label header = createLabel("Results");

        visualResultsContainer = new VBox(20);
        visualResultsContainer.setAlignment(Pos.TOP_CENTER);
        visualResultsContainer.setPadding(new Insets(10));

        placeholder = createLabel("No model trained");
        visualResultsContainer.getChildren().add(placeholder);

        ScrollPane scrollPane = new ScrollPane(visualResultsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #2E3440; -fx-background-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        vBox.getChildren().addAll(header, scrollPane);

        return vBox;
    }

    private void trainAndEvaluate() {
        if (processedData == null || processedData.isEmpty()) {
            showAlert("Please choose a dataset");
            return;
        }
        
        try {
            List<Instance<Double, Double>> dataCopy = new ArrayList<>(processedData);
            Collections.shuffle(dataCopy);

            double splitRatio = splitSlider.getValue() / 100.0;
            int splitIndex = (int) (dataCopy.size() * splitRatio);

            List<Instance<Double, Double>> trainSet = dataCopy.subList(0, splitIndex);
            List<Instance<Double, Double>> testSet = dataCopy.subList(splitIndex, dataCopy.size());

            Model<Double, Double> model = null;
            String selectedModel = classifierSelection.getValue();

            results = new TextArea();
            results.setEditable(false);
            results.setWrapText(true);
            results.setPrefHeight(200);
            results.setStyle("-fx-control-inner-background: #3B4252; -fx-text-fill: white; -fx-font-size: 18");

            switch (selectedModel) {
                case "Naive Bayes":
                    model = new GaussianNaiveBayes();
                    break;
                case "Decision tree":
                    showAlert("Decision tree not implemented yet");
                    return;
                case "Logistic Regression":
                    showAlert("Logistic Regression not implemented yet");
                    return;
            }
            model.train(trainSet);
            List<Double> predictions = model.test(testSet);

            EvaluationMeasure<Double, Double> accuracyMeasure = new Accuracy();
            EvaluationMeasure<Double, Double> precisionMeasure = new Precision();
            EvaluationMeasure<Double, Double> recallMeasure = new Recall();
            EvaluationMeasure<Double, Double> f1ScoreMeasure = new F1Score();

            double accuracy = accuracyMeasure.evaluate(testSet, predictions);
            double precision = precisionMeasure.evaluate(testSet, predictions);
            double recall = recallMeasure.evaluate(testSet, predictions);
            double f1Score = f1ScoreMeasure.evaluate(testSet, predictions);

            String resultsString = String.format("Accuracy: %.2f%%\n", accuracy * 100) +
                    String.format("Precision: %.2f%%\n", precision * 100) +
                    String.format("Recall: %.2f%%\n", recall * 100) +
                    String.format("F1 Score: %.2f%%\n", f1Score * 100);
            results.setText(resultsString);

            int truePositive = 0;
            int falsePositive = 0;
            int trueNegative = 0;
            int falseNegative = 0;
            Double positiveClass = 1.0;

            for (int i = 0; i < testSet.size(); i++) {
                Double actual = testSet.get(i).getOutput();
                Double predicted = predictions.get(i);

                if (predicted.equals(positiveClass)) {
                    if (actual.equals(positiveClass)) {
                        truePositive++;
                    } else {
                        falsePositive++;
                    }
                } else {
                    if (actual.equals(positiveClass)) {
                        falseNegative++;
                    } else {
                        trueNegative++;
                    }
                }
            }
            
            int finalTruePositive = truePositive;
            int finalFalsePositive = falsePositive;
            int finalTrueNegative = trueNegative;
            int finalFalseNegative = falseNegative;

            Platform.runLater(() -> {
                visualResultsContainer.getChildren().clear();

                Label matrixLabel = createLabel("Confusion Matrix");
                matrixLabel.setAlignment(Pos.CENTER);
                matrixLabel.setPadding(new Insets(20, 0, 10, 0));

                GridPane matrixGrid = createConfusionMatrix(finalTruePositive, finalFalsePositive, finalTrueNegative, finalFalseNegative);
                visualResultsContainer.getChildren().addAll(results, matrixLabel, matrixGrid);
            });

        } catch (Exception e) {
            showAlert(e.getMessage());
        }

    }

    private GridPane createConfusionMatrix(int truePositive, int falsePositive, int trueNegative, int falseNegative) {
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setAlignment(Pos.CENTER);
        grid.setStyle("-fx-padding: 10; -fx-background-color: #434C5E; -fx-background-radius: 5;");

        grid.add(createLabel("Pred. Pos."), 1, 0);
        grid.add(createLabel("Pred. Neg."), 2, 0);
        grid.add(createLabel("Act. Pos."), 0, 1);
        grid.add(createLabel("Act. Neg."), 0, 2);

        grid.add(createMatrixCell(truePositive, "#C3D59C", "TP"), 1, 1);
        grid.add(createMatrixCell(falseNegative, "#D29694", "FN"), 2, 1);
        grid.add(createMatrixCell(falsePositive, "#D29694", "FP"), 1, 2);
        grid.add(createMatrixCell(trueNegative, "#C3D59C", "TN"), 2, 2);

        return grid;
    }

    private StackPane createMatrixCell(int value, String colorHex, String label) {
        StackPane cell = new StackPane();
        cell.setPrefSize(80, 60);
        cell.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 5;");

        VBox content = new VBox(
                new Label(label),
                new Label(String.valueOf(value))
        );
        content.setAlignment(Pos.CENTER);

        Label labelText = (Label) content.getChildren().get(0);
        labelText.setStyle("-fx-font-size: 10; -fx-text-fill: #2E3440; -fx-font-weight: bold;");

        Label labelValue = (Label) content.getChildren().get(1);
        labelValue.setStyle("-fx-font-size: 18; -fx-text-fill: #2E3440; -fx-font-weight: bold;");

        cell.getChildren().add(content);
        return cell;
    }

    private void clearResults() {
        if (results != null) {
            results.clear();
        }

        if (visualResultsContainer != null) {
            visualResultsContainer.getChildren().clear();
            visualResultsContainer.getChildren().add(placeholder);
        }
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
        TextField textField = new TextField();
        textField.setPromptText(text);
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
