package com.example.vkcupfiles;

public class FileData {
    private String path;
    private String name;
    private String type;
    private String size;

    public FileData(String path, String name, String type) {
        this.path = path;
        this.name = name;
        this.type = type;

    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getType() {
        return type;
    }
}
