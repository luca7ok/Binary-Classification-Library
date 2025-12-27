package com.models;

import com.domain.*;

import java.util.*;

public class DecisionTree implements Model<Double, Double> {
    private Node root;
    private final int maxDepth = 5;
    private final int minSampleSplit = 50;

    @Override
    public void train(List<Instance<Double, Double>> instances) {
        this.root = buildTree(instances, 0);
    }

    @Override
    public List<Double> test(List<Instance<Double, Double>> instances) {
        List<Double> predictions = new ArrayList<>();
        for (Instance<Double, Double> instance : instances) {
            predictions.add(root.predict(instance.getInput()));
        }
        return predictions;
    }

    private Node buildTree(List<Instance<Double, Double>> instances, int depth) {
        double giniIndex = calculateGiniImpurity(instances);

        if (depth >= maxDepth || giniIndex == 0 || instances.size() < minSampleSplit) {
            return new LeafNode(getMajorityClass(instances));
        }

        Split bestSplit = findBestSplit(instances);

        if (bestSplit == null || bestSplit.getGain() == 0) {
            return new LeafNode(getMajorityClass(instances));
        }

        Node leftChild = buildTree(bestSplit.getLeftInstances(), depth + 1);
        Node rightChild = buildTree(bestSplit.getRightInstances(), depth + 1);

        return new InternalNdoe(bestSplit.getFeatureIndex(), bestSplit.getThreshold(), leftChild, rightChild);

    }

    private Split findBestSplit(List<Instance<Double, Double>> instances) {
        Split bestSplit = null;
        double currentGiniImpurity = calculateGiniImpurity(instances);
        double maxGain = 0.0;
        int numFeatures = instances.getFirst().getInput().size();

        for (int i = 0; i < numFeatures; i++) {
            List<Double> uniqueValues = new ArrayList<>();
            for (Instance<Double, Double> instance : instances) {
                uniqueValues.add(instance.getInput().get(i));
            }
            Collections.sort(uniqueValues);
            List<Double> candidates = new ArrayList<>();
            int step = uniqueValues.size() / 10;
            for (int j = 0; j < uniqueValues.size(); j += step) {
                candidates.add(uniqueValues.get(j));    
            }


            for (Double threshold : candidates) {
                List<Instance<Double, Double>> leftInstances = new ArrayList<>();
                List<Instance<Double, Double>> rightInstances = new ArrayList<>();

                for (Instance<Double, Double> instance : instances) {
                    if (instance.getInput().get(i) <= threshold) {
                        leftInstances.add(instance);
                    } else {
                        rightInstances.add(instance);
                    }
                }

                if (leftInstances.isEmpty() || rightInstances.isEmpty()) {
                    continue;
                }
                double giniImpurityLeft = calculateGiniImpurity(leftInstances);
                double giniImpurityRight = calculateGiniImpurity(rightInstances);

                double weightedGiniImpurity = (1.0 * leftInstances.size() / instances.size()) * giniImpurityLeft +
                        (1.0 * rightInstances.size() / instances.size()) * giniImpurityRight;

                double gain = currentGiniImpurity - weightedGiniImpurity;
                if (gain > maxGain) {
                    maxGain = gain;
                    bestSplit = new Split(i, threshold, leftInstances, rightInstances, gain);
                }
            }
        }
        return bestSplit;
    }

    private double getMajorityClass(List<Instance<Double, Double>> instances) {
        Map<Double, Integer> counts = new HashMap<>();
        Double majority = null;
        int maxCount = -1;

        for (Instance<Double, Double> instance : instances) {
            int count = counts.getOrDefault(instance.getOutput(), 0) + 1;
            counts.put(instance.getOutput(), count);
            if (majority == null || count > maxCount) {
                maxCount = count;
                majority = instance.getOutput();
            }
        }
        return majority;
    }

    private double calculateGiniImpurity(List<Instance<Double, Double>> instances) {
        Map<Double, Integer> counts = new HashMap<>();
        for (Instance<Double, Double> instance : instances) {
            counts.put(instance.getOutput(), counts.getOrDefault(instance.getOutput(), 0) + 1);
        }

        double giniImpurity = 1.0;
        for (int count : counts.values()) {
            double probability = 1.0 * count / instances.size();
            giniImpurity -= Math.pow(probability, 2);
        }
        return giniImpurity;
    }

}
