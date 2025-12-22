package com;

import com.evaluation.*;
import com.gui.gui;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        try {
            Application.launch(gui.class, args);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}