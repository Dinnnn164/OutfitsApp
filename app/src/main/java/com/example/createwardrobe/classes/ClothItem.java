package com.example.createwardrobe.classes;

public class ClothItem {
    private String imageBase64;
    private long lastWorn;



    public ClothItem() {

    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public long getLastWorn() {
        return lastWorn;
    }

    public void setLastWorn(long lastWorn) {
        this.lastWorn = lastWorn;
    }


}