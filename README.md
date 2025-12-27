# Binary Classification Library

A custom machine learning library written in Java, designed to perform binary classification tasks. This project implements various classification algorithms, evaluation metrics, and a JavaFX-based Graphical User Interface (GUI), demonstrating advanced Java concepts such as Object-Oriented Design, Generics, Streams.

##  Overview

This library was developed as a semester project to explore the implementation of machine learning algorithms from scratch. It allows users to load datasets (e.g., CSV), train different binary classifiers, tune hyperparameters, and evaluate performance using comprehensive metrics.

##  Features

### Classifiers
The library offers a selection of binary classification algorithms, allowing users to choose the best fit for their data:

* **The Perceptron**
* **Naive Bayes**
* **Logistic Regression**

###  Evaluation Metrics
To assess model performance, the library provides various evaluation measures:
* **Confusion Matrix** (True Positives, False Positives, True Negatives, False Negatives)
* **Accuracy**
* **Recall**
* **F1 Score**


###  Graphical User Interface (GUI)
A user-friendly **JavaFX** interface that enables:

* **Model Configuration**: Select a classifier and adjust its hyperparameters via the UI.
* **Data Splitting**: Customize the percentage for Training vs. Testing sets.
* **Visual Results**: View test results, including the Confusion Matrix.

### Adding a Dataset
To use your own data with this library, you must register it within the application configuration:

1. Place your dataset file (.csv) into the project's resources directory: src/main/resources/

2. Update Configuration: Open the datasets.json file located in the resources folder.

3. Add a new entry to the JSON array with the following details:

    * name: The display name of the dataset.

    * fileName: The nam of the file (e.g., my_data.csv).

    * labelIndex: The column index (0-based) where the target label is located in your CSV.

	### Example `datasets.json` entry:
	```json
	{
	  "name": "My Data",
	  "fileName": "my_data.csv",
	  "labelIndex": 4
	}
	```

## Application Workflow

1. Start the JavaFX application.
2. Choose a dataset from the dropdown menu.
3. Select your desired classifier (Perceptron, Naive Bayes, or Logistic Regression).
4. Configure:

   * Adjust specific hyperparameters such as Learning Rate or Epochs based on the chosen algorithm.

   * Set the Training/Testing Split using the slider (e.g., 80% Training / 20% Testing).

5. Click the "Train & Evaluate" button to train the model and validate it against the testing set.

6. Review the generated Confusion Matrix, Accuracy, Precision, Recall, and F1 Score displayed on the dashboard.


## License

[MIT](https://choosealicense.com/licenses/mit/)

