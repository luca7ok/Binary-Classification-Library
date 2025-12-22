package com.evaluation;

import com.domain.Instance;

import java.util.List;

public class Accuracy implements EvaluationMeasure<Double, Double> {
    @Override
    public double evaluate(List<Instance<Double, Double>> instances, List<Double> predictions) {
        int truePositives = 0;
        int trueNegatives = 0;
        double positiveClass = 1.0;
        double negativeClass = 0.0;

        for (int i = 0; i < instances.size(); i++) {
            Double actual = instances.get(i).getOutput();
            Double predicted = predictions.get(i);

            if (actual.equals(positiveClass)) {
                if(predicted.equals(positiveClass)){
                    truePositives++;
                }
            }
            else{
                if(predicted.equals(negativeClass)){
                    trueNegatives++;
                }
            }
        }
        return 1.0 * (truePositives + trueNegatives) / instances.size();
    }
}
