package com;

import com.utils.DatabaseImporter;

public class Main {
    public static void main(String[] args) {
        String csvFile = "src/main/resources/wdbc.csv";
        String dbFile = "data/breast_cancer_data.db";

        try {
            DatabaseImporter.convertCsvToSqlite(csvFile, dbFile, "cancer_data");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}