package com.example.createwardrobe.classes;

import java.util.List;

public class Outfit {
    private String id;
    private String name;
    private List<String> imageBase64List;

    public Outfit(String id, String name, List<String> imageBase64List) {
        this.id = id;
        this.name = name;
        this.imageBase64List = imageBase64List;
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
}
