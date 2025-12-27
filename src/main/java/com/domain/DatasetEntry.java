package com.domain;

public class DatasetEntry {
    public String name;
    public String fileName;
    public int labelIndex;

    public DatasetEntry(String name, String fileName, int labelIndex) {
        this.name = name;
        this.fileName = fileName;
        this.labelIndex = labelIndex;
    }

    @Override
    public String toString() {
        return name;
    }
}
