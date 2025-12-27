package com.domain;

import java.util.List;

public class Split {
    private final int featureIndex;
    private final double threshold;
    private final List<Instance<Double, Double>> leftInstances;
    private final List<Instance<Double, Double>> rightInstances;
    private final double gain;

    public Split(int featureIndex, double threshold, List<Instance<Double, Double>> leftInstances, List<Instance<Double, Double>> rightInstances, double gain) {
        this.featureIndex = featureIndex;
        this.threshold = threshold;
        this.leftInstances = leftInstances;
        this.rightInstances = rightInstances;
        this.gain = gain;
    }

    public int getFeatureIndex() {
        return featureIndex;
    }

    public double getThreshold() {
        return threshold;
    }

    public List<Instance<Double, Double>> getLeftInstances() {
        return leftInstances;
    }

    public List<Instance<Double, Double>> getRightInstances() {
        return rightInstances;
    }

    public double getGain() {
        return gain;
    }
}
