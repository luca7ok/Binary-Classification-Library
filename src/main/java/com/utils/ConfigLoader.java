package com.utils;

import com.domain.DatasetEntry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigLoader {

    public static List<DatasetEntry> loadConfig(){
        List<DatasetEntry> datasets = new ArrayList<>();
        String filename = "src/main/resources/datasets.json";
        
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filename))) {
            JSONTokener jsonTokener = new JSONTokener(bufferedReader);
            JSONArray jsonArray = new JSONArray(jsonTokener);

            for (int index = 0; index < jsonArray.length(); index++) {
                JSONObject jsonObject = jsonArray.getJSONObject(index);

                String name = jsonObject.getString("name");
                String path = jsonObject.getString("path");
                int labelIndex = jsonObject.getInt("labelIndex");

                DatasetEntry datasetEntry = new DatasetEntry(name, path, labelIndex);
                datasets.add(datasetEntry);
            }
            return datasets;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read from file: " + filename);
        } catch (JSONException e) {
            throw new RuntimeException("Failed to parse json: " + filename);
        }
    }
}
