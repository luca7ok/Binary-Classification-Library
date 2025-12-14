package com.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DatabaseImporter {
    public static void convertCsvToSqlite(String csvFilePath, String dbFilePath, String tableName) {
        String url = "jdbc:sqlite:" + dbFilePath;

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(csvFilePath))) {

            String headerLine = bufferedReader.readLine();
            String[] rawColumns = headerLine.split(",");
            List<String> cleanColumns = new ArrayList<>();

            for (String column : rawColumns) {
                String cleanColumn = column.replace("\"", "").trim().replace(" ", "_");
                if (!cleanColumn.isEmpty()) {
                    cleanColumns.add(cleanColumn);
                }
            }

            String createTableSQL = buildCreateTableSQL(tableName, cleanColumns);
            String insertSQL = buildInsertSQL(tableName, cleanColumns.size());

            try (Connection connection = DriverManager.getConnection(url)) {
                Statement statement = connection.createStatement();

                statement.execute(createTableSQL);
                statement.execute("DELETE FROM " + tableName);

                PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);

                String line = bufferedReader.readLine();
                while (line != null) {
                    String[] values = line.split(",", -1);
                    if (values.length != cleanColumns.size()) {
                        continue;
                    }

                    for (int i = 0; i < values.length; i++) {
                        String value = values[i].replace("\"", "").trim();
                        preparedStatement.setString(i + 1, value);
                    }
                    preparedStatement.addBatch();

                    line = bufferedReader.readLine();
                }
                preparedStatement.executeBatch();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String buildCreateTableSQL(String tableName, List<String> columns) {
        String parameters = columns.stream()
                .map(column -> column + " TEXT")
                .collect(Collectors.joining(", "));
        return "CREATE TABLE IF NOT EXISTS " + tableName + " (" + parameters + ")";
    }

    private static String buildInsertSQL(String tableName, int columnCount) {
        StringBuilder stringBuilder = new StringBuilder("INSERT INTO " + tableName + " VALUES (");
        for (int i = 0; i < columnCount; i++) {
            stringBuilder.append("?");
            if (i < columnCount - 1) {
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }
}
