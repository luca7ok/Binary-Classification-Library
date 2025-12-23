package com.utils;

import com.domain.Instance;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvReader {

    private static Map<Integer, Map<String, Double>> columnMappings = new HashMap<>();
    
    public static List<Instance<Double, String>> loadFromCsv(String filePath, int labelIndex) {
        List<Instance<Double, String>> dataset = new ArrayList<>();
        columnMappings.clear();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            bufferedReader.readLine();
            String line = bufferedReader.readLine();

            while (line != null) {
                String[] tokens = line.split(",");
                List<Double> features = new ArrayList<>();
                String label = null;

                for (int i = 0; i < tokens.length; i++) {
                    if (i == labelIndex) {
                        label = tokens[i].trim();
                    } else {
                        try {
                            double value = Double.parseDouble(tokens[i].trim());
                            features.add(value);
                        } catch (NumberFormatException e) {
                            double encodedValue = encodeCategoricalValue(i, tokens[i]);
                            features.add(encodedValue);
                        }
                    }
                }
                if (label != null && !features.isEmpty()) {
                    dataset.add(new Instance<>(features, label));
                }

                line = bufferedReader.readLine();
            }
            return dataset;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static double encodeCategoricalValue(int colIndex, String value) {
        columnMappings.putIfAbsent(colIndex, new HashMap<>());
        Map<String, Double> map = columnMappings.get(colIndex);

        if (!map.containsKey(value)) {
            double newId = map.size(); 
            map.put(value, newId);
        }

        return map.get(value);
    }
    
    public static List<String> readHeaders(String filePath, int labelIndex) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            String line = bufferedReader.readLine();
            List<String> headers = new ArrayList<>();
            
            if (line != null) {
                String[] tokens = line.trim().split(",");
                for (int i = 0; i < tokens.length; i++) {
                    String header = tokens[i].trim().replace("\"", "").replace(" ", "_");
                    if(i != labelIndex){
                        headers.add(header);
                    }
                }
                return headers;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new ArrayList<>();
    }
}
