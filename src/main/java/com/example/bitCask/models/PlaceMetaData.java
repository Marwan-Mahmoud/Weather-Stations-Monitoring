package com.example.bitCask.models;

public class PlaceMetaData {
    String fileName;
    long valuePosition;

    public PlaceMetaData(String fileName, long valuePosition) {
        this.fileName = fileName;
        this.valuePosition = valuePosition;
    }

    public String getFileName() {
        return fileName;
    }

    public long getValuePosition() {
        return valuePosition;
    }


}
