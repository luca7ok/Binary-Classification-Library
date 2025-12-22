package com.models;

import com.domain.Instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GaussianNaiveBayes implements Model<Double, Double> {
    private Map<Double, List<Double>> means = new HashMap<>();
    private Map<Double, List<Double>> variances = new HashMap<>();
    private Map<Double, Double> classPriors = new HashMap<>();
    private List<Double> classes;

    @Override
    public void train(List<Instance<Double, Double>> instances) {
        Map<Double, List<Instance<Double, Double>>> separated = new HashMap<>();
        for (Instance<Double, Double> instance : instances) {
            separated.computeIfAbsent(instance.getOutput(), k -> new ArrayList<>()).add(instance);
        }
        classes = new ArrayList<>(separated.keySet());
        int countFeatures = instances.getFirst().getInput().size();
        int totalRows = instances.size();

        for (Double label : classes) {
            List<Instance<Double, Double>> classRows = separated.get(label);

            classPriors.put(label, 1.0 * classRows.size() / totalRows);

            List<Double> classMeans = new ArrayList<>();
            List<Double> classVariances = new ArrayList<>();

            for (int i = 0; i < countFeatures; i++) {
                double mean = 0;
                for (Instance<Double, Double> instance : classRows) {
                    mean += instance.getInput().get(i);
                }
                mean /= classRows.size();

                double variance = 1e-9;
                for (Instance<Double, Double> instance : classRows) {
                    variance += Math.pow(instance.getInput().get(i) - mean, 2);
                }
                variance /= classRows.size();

                classMeans.add(mean);
                classVariances.add(variance);
            }
            means.put(label, classMeans);
            variances.put(label, classVariances);
        }
    }

    @Override
    public List<Double> test(List<Instance<Double, Double>> instances) {
        List<Double> predictions = new ArrayList<>();
        for (Instance<Double, Double> instance : instances) {
            predictions.add(predictSingle(instance.getInput()));
        }
        return predictions;
    }

    private Double predictSingle(List<Double> features) {
        Double bestClass = null;
        double maxProbability = -1.0;
        for (Double label : classes) {
            double probability = classPriors.get(label);
            List<Double> classMeans = means.get(label);
            List<Double> classVariances = variances.get(label);

            for (int i = 0; i < features.size(); i++) {
                probability *= calculatePDF(features.get(i), classMeans.get(i), classVariances.get(i));
            }

            if (bestClass == null || probability > maxProbability) {
                maxProbability = probability;
                bestClass = label;
            }
        }
        return bestClass;
    }

    private double calculatePDF(double x, double mean, double variance) {
        double exponent = Math.exp(-(Math.pow(x - mean, 2) / (2 * variance)));
        return (1 / Math.sqrt(2 * Math.PI * variance)) * exponent;
    }
}
