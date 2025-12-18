package com;

import com.gui.gui;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        try {
            // List<Instance<Double, String>> instances = CsvReader.loadFromCsv("src/main/resources/wdbc.csv", 1);
            Application.launch(gui.class, args);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}