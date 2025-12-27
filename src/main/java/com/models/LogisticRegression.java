package com.models;

import com.domain.Instance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class LogisticRegression implements Model<Double, Double> {
    private final List<Double> weights = new ArrayList<>();
    private double bias = 0.0;
    private final int epochs;
    private final double learningRate;

    private List<Double> means;
    private List<Double> standardDeviations;

    public LogisticRegression(double learningRate, int epochs) {
        this.learningRate = learningRate;
        this.epochs = epochs;
    }

    @Override
    public void train(List<Instance<Double, Double>> instances) {
        fitNormalizationParameters(instances);
        List<Instance<Double, Double>> trainingData = applyZScoreNormalization(instances);

        int featuresCount = instances.getFirst().getInput().size();
        Random random = new Random();
        weights.clear();
        for (int i = 0; i < featuresCount; i++) {
            weights.add((random.nextDouble() - 0.5) * 0.02);
        }

        for (int epoch = 0; epoch < epochs; epoch++) {
            Collections.shuffle(trainingData);

            for (Instance<Double, Double> instance : trainingData) {
                List<Double> features = instance.getInput();
                double output = instance.getOutput();
                double prediction = predict(features);
                double error = output - prediction;

                if (error != 0.0) {
                    for (int i = 0; i < weights.size(); i++) {
                        weights.set(i, weights.get(i) + learningRate * error * features.get(i));
                    }
                    bias += learningRate * error;
                }
            }
        }
    }

    @Override
    public List<Double> test(List<Instance<Double, Double>> instances) {
        List<Instance<Double, Double>> testData = applyZScoreNormalization(instances);
        return testData.stream()
                .map(instance -> predict(instance.getInput()) >= 0.5 ? 1.0 : 0.0)
                .toList();
    }

    private double predict(List<Double> features) {
        double sum = bias;
        for (int i = 0; i < features.size(); i++) {
            sum += features.get(i) * weights.get(i);
        }
        return 1.0 / (1.0 + Math.exp(-sum));
    }


    private void fitNormalizationParameters(List<Instance<Double, Double>> instances) {
        int featuresCount = instances.getFirst().getInput().size();

        means = new ArrayList<>(Collections.nCopies(featuresCount, 0.0));
        standardDeviations = new ArrayList<>(Collections.nCopies(featuresCount, 1e-9));

        for (int i = 0; i < featuresCount; i++) {
            for (Instance<Double, Double> instance : instances) {
                double value = instance.getInput().get(i);
                means.set(i, means.get(i) + value);
            }
            means.set(i, means.get(i) / instances.size());
            for (Instance<Double, Double> instance : instances) {
                double value = instance.getInput().get(i);
                standardDeviations.set(i, standardDeviations.get(i) + Math.pow(value - means.get(i), 2));
            }
            standardDeviations.set(i, standardDeviations.get(i) / instances.size());
            standardDeviations.set(i, Math.sqrt(standardDeviations.get(i)));
        }
    }

    private List<Instance<Double, Double>> applyZScoreNormalization(List<Instance<Double, Double>> instances) {
        List<Instance<Double, Double>> normalizedInstances = new ArrayList<>();

        for (Instance<Double, Double> instance : instances) {
            List<Double> features = instance.getInput();
            List<Double> normalizedFeatures = new ArrayList<>();

            for (int i = 0; i < features.size(); i++) {
                normalizedFeatures.add((features.get(i) - means.get(i)) / standardDeviations.get(i));
            }
            normalizedInstances.add(new Instance<>(normalizedFeatures, instance.getOutput()));
        }
        return normalizedInstances;
    }
}
