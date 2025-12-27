package com.utils;

import com.domain.Instance;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvReader {

    private static final Map<Integer, Map<String, Double>> columnMappings = new HashMap<>();

    public static List<Instance<Double, String>> loadFromCsv(String filePath, int labelIndex) {
        List<Instance<Double, String>> dataset = new ArrayList<>();
        columnMappings.clear();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = bufferedReader.readLine();
            boolean skipFirstColumn = skipFirstColumn(headerLine);
            boolean skipSecondColumn = skipSecondColumn(headerLine);

            String line = bufferedReader.readLine();

            while (line != null) {
                String[] tokens = line.split(",");
                List<Double> features = new ArrayList<>();
                String label = null;

                for (int i = 0; i < tokens.length; i++) {
                    if (skipFirstColumn && i == 0) {
                        continue;
                    }
                    if (skipSecondColumn && i == 1) {
                        continue;
                    }
                    
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
                boolean skipFirstColumn = skipFirstColumn(line);
                boolean skipSecondColumn = skipSecondColumn(line);
                String[] tokens = line.trim().split(",");

                for (int i = 0; i < tokens.length; i++) {
                    if (skipFirstColumn && i == 0) {
                        continue;
                    }
                    if (skipSecondColumn && i == 1) {
                        continue;
                    }
                    String header = tokens[i].trim().replace("\"", "").replace(" ", "_");
                    if (i != labelIndex) {
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

    private static boolean skipFirstColumn(String headerLine) {
        if (headerLine == null) return false;

        String[] headers = headerLine.split(",");
        if (headers.length > 0) {
            String firstHeader = headers[0].trim().replace("\"", "");

            return firstHeader.equalsIgnoreCase("id")
                    || firstHeader.equalsIgnoreCase("no")
                    || firstHeader.toLowerCase().endsWith("id")
                    || firstHeader.isEmpty();
        }
        return false;
    }

    private static boolean skipSecondColumn(String headerLine) {
        if (headerLine == null) return false;

        String[] headers = headerLine.split(",");
        if (headers.length > 0) {
            String secondHeader = headers[1].trim().replace("\"", "");

            return secondHeader.equalsIgnoreCase("id")
                    || secondHeader.equalsIgnoreCase("no")
                    || secondHeader.toLowerCase().endsWith("id")
                    || secondHeader.isEmpty();
        }
        return false;
    }
}
