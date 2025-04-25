package com.example.createwardrobe;

public class Outfit {
    private String id;
    private String name;
    private String imageBase64;

    public Outfit() {}

    public Outfit(String id, String name, String imageBase64) {
        this.id = id;
        this.name = name;
        this.imageBase64 = imageBase64;
    }

    public String getId() { return id; }

    public String getName() { return name; }

    public String getImageBase64() { return imageBase64; }
}
