package com.domain;

import java.util.List;

public class InternalNdoe implements Node {
    private final int featureIndex;
    private final double threshold;
    private final Node left;
    private final Node right;

    public InternalNdoe(int featureIndex, double threshold, Node left, Node right) {
        this.featureIndex = featureIndex;
        this.threshold = threshold;
        this.left = left;
        this.right = right;
    }

    @Override
    public double predict(List<Double> features) {
        if (features.get(featureIndex) < threshold) {
            return left.predict(features);
        } else {
            return right.predict(features);
        }
    }

}
