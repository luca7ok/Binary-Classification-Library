package com.gui;

import com.domain.DatasetEntry;
import com.domain.Instance;
import com.evaluation.*;
import com.models.DecisionTree;
import com.models.GaussianNaiveBayes;
import com.models.Model;
import com.models.Perceptron;
import com.utils.ConfigLoader;
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
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class gui extends Application {
    private TableView<Instance<Double, Double>> tableView;
    private Slider splitSlider;
    private List<Instance<Double, Double>> processedData;
    private ComboBox<String> classifierDropdown;
    private VBox visualResultsContainer;
    private TextArea results;
    private Label placeholder;
    private ComboBox<DatasetEntry> datasetDropdown;
    private VBox hyperparametersContainer;
    private TextField epochsTextField;
    private TextField learningRateTextField;

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

            VBox leftPane = createLeftPane();
            VBox middlePane = createMiddlePane();

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

            Node axis = splitSlider.lookup(".axis");
            if (axis != null) {
                axis.setStyle("-fx-tick-label-fill: white;");
            }

        } catch (Exception e) {
            showAlert(e.getMessage());
            Platform.exit();
        }
    }

    private VBox createLeftPane() {
        VBox vBox = new VBox(15);
        vBox.setPrefWidth(300);
        vBox.setMinWidth(300);
        vBox.setAlignment(Pos.TOP_LEFT);

        Label selectDataLabel = createLabel("Select Dataset:");

        datasetDropdown = new ComboBox<>();
        datasetDropdown.setMaxWidth(Double.MAX_VALUE);
        datasetDropdown.setStyle("-fx-base: #4C566A; -fx-text-fill: white; -fx-font-size: 14");

        List<DatasetEntry> configs = ConfigLoader.loadConfig();
        datasetDropdown.getItems().addAll(configs);
        datasetDropdown.setValue(configs.getFirst());

        Button loadButton = createButton("Load Dataset");
        loadButton.setOnAction(e -> loadSelectedDataset());
        
        Label classifierLabel = createLabel("Classifier model:");
        VBox.setMargin(classifierLabel, new Insets(40, 0, 0, 0));

        classifierDropdown = new ComboBox<>();
        classifierDropdown.getItems().addAll("Naive Bayes", "Perceptron", "Logistic Regression", "Decision Tree");
        classifierDropdown.setValue("Naive Bayes");
        classifierDropdown.setMaxWidth(Double.MAX_VALUE);
        classifierDropdown.setStyle("-fx-base: #4C566A; -fx-text-fill: white; -fx-font-size: 16");

        Label hyperparametersLabel = createLabel("Hyperparameters:");
        VBox.setMargin(hyperparametersLabel, new Insets(25, 0, 0, 0));

        hyperparametersContainer = new VBox(10);

        epochsTextField = createTextField("Add number of epochs");
        epochsTextField.setText("100");

        learningRateTextField = createTextField("Add learning rate");
        learningRateTextField.setText("0.01");

        classifierDropdown.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateHyperparameters(newVal);
        });
        updateHyperparameters(classifierDropdown.getValue());

        Label trainSplitLabel = createLabel("Train split: 75%");
        VBox.setMargin(trainSplitLabel, new Insets(25, 0, 0, 0));
        Label testSplitLabel = createLabel("Test split: 25%");

        splitSlider = new Slider(10, 90, 75);
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

        vBox.getChildren().addAll(selectDataLabel, datasetDropdown, loadButton,
                classifierLabel, classifierDropdown,
                hyperparametersLabel, hyperparametersContainer,
                trainSplitLabel, testSplitLabel, splitSlider,
                trainButton);

        return vBox;
    }

    private VBox createMiddlePane() {
        VBox vBox = new VBox(15);
        vBox.setPrefWidth(300);
        vBox.setMinWidth(300);
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
        try {
            if (processedData == null || processedData.isEmpty()) {
                throw new RuntimeException("Please load a dataset");
            }
            
            Pair<List<Instance<Double, Double>>, List<Instance<Double, Double>>> dataSplit =
                    splitData(processedData, splitSlider.getValue());

            List<Instance<Double, Double>> trainSet = dataSplit.getKey();
            List<Instance<Double, Double>> testSet = dataSplit.getValue();

            String selectedModel = classifierDropdown.getValue();
            Model<Double, Double> model = getModel(selectedModel);

            model.train(trainSet);
            List<Double> predictions = model.test(testSet);

            String resultsText = calculateResults(testSet, predictions);
            int[] matrixStats = calculateConfusionMatrixStats(testSet, predictions);

            updateResults(resultsText, matrixStats);
        
        } catch (Exception e) {
            showAlert(e.getMessage());
        }
    }

    private void loadSelectedDataset() {
        DatasetEntry selected = datasetDropdown.getValue();
        try {
            clearResults();

            String resourcePath = Objects.requireNonNull(getClass().getResource("/" + selected.path)).getPath();

            List<Instance<Double, String>> rawData = CsvReader.loadFromCsv(resourcePath, selected.labelIndex);
            processedData = LabelEncoder.encode(rawData);
            List<String> headers = CsvReader.readHeaders(resourcePath, selected.labelIndex);

            updateTable(processedData, headers);
            tableView.setItems(FXCollections.observableArrayList(processedData));
        } catch (Exception e) {
            showAlert(e.getMessage());
        }
    }

    private void updateHyperparameters(String selectedModel) {
        hyperparametersContainer.getChildren().clear();

        if ("Perceptron".equalsIgnoreCase(selectedModel)) {
            Label epochsLabel = createLabel("Epochs:");
            VBox.setMargin(epochsLabel, new Insets(15, 0, 0, 0));
            Label learningRateLabel = createLabel("Learning Rate:");

            hyperparametersContainer.getChildren().addAll(epochsLabel, epochsTextField, learningRateLabel, learningRateTextField);
        } else {
            Label noHyperparametersLabel = createLabel("No hyperparameters for " + selectedModel);
            VBox.setMargin(noHyperparametersLabel, new Insets(15, 0, 0, 0));
            noHyperparametersLabel.setWrapText(true);

            hyperparametersContainer.getChildren().add(noHyperparametersLabel);
        }
    }

    private Pair<List<Instance<Double, Double>>, List<Instance<Double, Double>>> splitData(List<Instance<Double, Double>> data, double sliderValue) {
        List<Instance<Double, Double>> positives = new ArrayList<>();
        List<Instance<Double, Double>> negatives = new ArrayList<>();

        for (Instance<Double, Double> instance : data) {
            if (instance.getOutput().equals(1.0)) {
                positives.add(instance);
            } else {
                negatives.add(instance);
            }
        }

        Collections.shuffle(positives);
        Collections.shuffle(negatives);

        double splitRatio = sliderValue / 100.0;
        int splitPositiveIndex = (int) (positives.size() * splitRatio);
        int splitNegativeIndex = (int) (negatives.size() * splitRatio);

        List<Instance<Double, Double>> trainSet = new ArrayList<>();
        trainSet.addAll(positives.subList(0, splitPositiveIndex));
        trainSet.addAll(negatives.subList(0, splitNegativeIndex));

        List<Instance<Double, Double>> testSet = new ArrayList<>();
        testSet.addAll(positives.subList(splitPositiveIndex, positives.size()));
        testSet.addAll(negatives.subList(splitNegativeIndex, negatives.size()));

        return new Pair<>(trainSet, testSet);
    }

    private Model<Double, Double> getModel(String selectedModel) {
        switch (selectedModel) {
            case "Naive Bayes":
                return new GaussianNaiveBayes();

            case "Perceptron":
                if (epochsTextField.getText().isEmpty()) {
                    throw new RuntimeException("Epochs cannot be empty");
                }
                if (learningRateTextField.getText().isEmpty()) {
                    throw new RuntimeException("Learning rate cannot be empty");
                }

                int epochs;
                double learningRate;

                try {
                    epochs = Integer.parseInt(epochsTextField.getText().trim());
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Number of epochs must be an integer");
                }

                try {
                    learningRate = Double.parseDouble(epochsTextField.getText().trim());
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Learning rate must be a real number");
                }
                return new Perceptron(learningRate, epochs);

            case "Logistic Regression":
                throw new RuntimeException("Logistic Regression not implemented yet");

            case "Decision Tree":
               throw new RuntimeException("Decision Tree not implemented yet");
        }
        return null;
    }

    private int[] calculateConfusionMatrixStats(List<Instance<Double, Double>> testSet, List<Double> predictions) {
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
        return new int[]{truePositive, falsePositive, trueNegative, falseNegative};
    }

    private String calculateResults(List<Instance<Double, Double>> testSet, List<Double> predictions) {
        EvaluationMeasure<Double, Double> accuracyMeasure = new Accuracy();
        EvaluationMeasure<Double, Double> precisionMeasure = new Precision();
        EvaluationMeasure<Double, Double> recallMeasure = new Recall();
        EvaluationMeasure<Double, Double> f1ScoreMeasure = new F1Score();

        double accuracy = accuracyMeasure.evaluate(testSet, predictions);
        double precision = precisionMeasure.evaluate(testSet, predictions);
        double recall = recallMeasure.evaluate(testSet, predictions);
        double f1Score = f1ScoreMeasure.evaluate(testSet, predictions);

        return String.format("Accuracy: %.2f%%\n", accuracy * 100) +
                String.format("Precision: %.2f%%\n", precision * 100) +
                String.format("Recall: %.2f%%\n", recall * 100) +
                String.format("F1 Score: %.2f%%\n", f1Score * 100);
    }
    
    private void updateResults(String text, int[] matrixStats) {
        results = new TextArea();
        results.setEditable(false);
        results.setWrapText(true);
        results.setPrefHeight(150);
        results.setStyle("-fx-control-inner-background: #3B4252; -fx-text-fill: white; -fx-font-size: 18");
        results.setText(text);

        visualResultsContainer.getChildren().clear();

        Label matrixLabel = createLabel("Confusion Matrix");
        matrixLabel.setAlignment(Pos.CENTER);
        matrixLabel.setPadding(new Insets(20, 0, 10, 0));

        GridPane matrixGrid = createConfusionMatrix(matrixStats[0], matrixStats[1], matrixStats[2], matrixStats[3]);
        visualResultsContainer.getChildren().addAll(results, matrixGrid);
    }

    private GridPane createConfusionMatrix(int truePositive, int falsePositive, int trueNegative, int falseNegative) {
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setAlignment(Pos.CENTER);
        grid.setStyle("-fx-padding: 10; -fx-background-color: #434C5E; -fx-background-radius: 5;");

        grid.add(createLabel("Pred Pos"), 1, 0);
        grid.add(createLabel("Pred Neg"), 2, 0);
        grid.add(createLabel("Act Pos"), 0, 1);
        grid.add(createLabel("Act Neg"), 0, 2);

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
