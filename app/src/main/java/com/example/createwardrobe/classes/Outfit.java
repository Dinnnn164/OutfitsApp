package com.example.createwardrobe.classes;

import java.util.List;

public class Outfit {
    private String id;
    private String name;
    private List<String> imageBase64List;

    private String category;

    public Outfit(String id, String name, List<String> imageBase64List) {
        this.id = id;
        this.name = name;
        this.imageBase64List = imageBase64List;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getImageBase64List() {
        return imageBase64List;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
