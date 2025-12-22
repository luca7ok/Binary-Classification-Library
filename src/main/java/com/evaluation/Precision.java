package com.evaluation;

import com.domain.Instance;

import java.util.List;

public class Precision implements EvaluationMeasure<Double, Double> {
    @Override
    public double evaluate(List<Instance<Double, Double>> instances, List<Double> predictions) {
        int truePositives = 0;
        int falsePositives = 0;

        for (int i = 0; i < instances.size(); i++) {
            Double actual = instances.get(i).getOutput();
            Double predicted = predictions.get(i);

            if (actual.equals(1.0)) {
                if (predicted.equals(1.0)) {
                    truePositives++;
                }
            } else {
                if (predicted.equals(1.0)) {
                    falsePositives++;
                }
            }
        }

        return 1.0 * truePositives / (truePositives + falsePositives);
    }
}
