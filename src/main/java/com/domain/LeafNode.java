package com.domain;

import java.util.List;

public class LeafNode implements Node {
    private final double label;

    public LeafNode(double predictClass) {
        this.label = predictClass;
    }

    @Override
    public double predict(List<Double> features) {
        return label;
    }
}
