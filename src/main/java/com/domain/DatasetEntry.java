package com.domain;

public class DatasetEntry {
    public String name;
    public String path;
    public int labelIndex;

    public DatasetEntry(String name, String path, int labelIndex) {
        this.name = name;
        this.path = path;
        this.labelIndex = labelIndex;
    }

    @Override
    public String toString() {
        return name;
    }
}
