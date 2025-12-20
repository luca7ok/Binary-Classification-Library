package com.evaluation;

import com.domain.Instance;

import java.util.List;

public class F1Score implements EvaluationMeasure<Double, Double> {
    private final Double positiveClass;

    public F1Score(Double positiveClass) {
        this.positiveClass = positiveClass;
    }

    @Override
    public double evaluate(List<Instance<Double, Double>> instances, List<Double> predictions) {
        EvaluationMeasure<Double, Double> precisionEvaluationMeasure = new Precision(positiveClass);
        double precision = precisionEvaluationMeasure.evaluate(instances, predictions);

        EvaluationMeasure<Double, Double> recallEvaluationMeasure = new Recall(positiveClass);
        double recall = recallEvaluationMeasure.evaluate(instances, predictions);

        return 2 * precision * recall / (precision + recall);
    }
}
