package com.utils;

import com.domain.Instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LabelEncoder {
    public static List<Instance<Double, Double>> encode(List<Instance<Double, String>> rawData) {
        List<Instance<Double, Double>> numericData = new ArrayList<>();
        Map<String, Double> map = new HashMap<>();
        double nextCode = 0.0;

        for (Instance<Double, String> instance : rawData) {
            String label = instance.getOutput();
            if (!map.containsKey(label)) {
                map.put(label, nextCode++);
            }
            numericData.add(new Instance<>(instance.getInput(), map.get(label)));
        }
        return numericData;
    }
}
