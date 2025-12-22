package com.utils;

import com.domain.Instance;

import java.util.*;

public class LabelEncoder {
    public static List<Instance<Double, Double>> encode(List<Instance<Double, String>> rawData) {
        List<Instance<Double, Double>> numericData = new ArrayList<>();
        Map<String, Double> map = new HashMap<>();
        double nextCode = 0.0;

        Set<String> uniqueLabels = new TreeSet<>();
        for (Instance<Double, String> instance : rawData) {
            uniqueLabels.add(instance.getOutput());
        }

        for (String label : uniqueLabels) {
            map.put(label, nextCode++);
        }

        for (Instance<Double, String> instance : rawData) {
            numericData.add(new Instance<>(instance.getInput(), map.get(instance.getOutput())));
        }
        return numericData;
    }
}
