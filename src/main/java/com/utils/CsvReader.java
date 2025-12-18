package com.utils;

import com.domain.Instance;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CsvReader {

    public static List<Instance<Double, String>> loadFromCsv(String filePath, int labelIndex) {
        List<Instance<Double, String>> dataset = new ArrayList<>();

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
                            throw new RuntimeException("Could not parse real values");
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
