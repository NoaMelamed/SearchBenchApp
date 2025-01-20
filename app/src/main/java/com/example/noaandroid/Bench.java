package com.example.noaandroid;

import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class Bench {
    private String name;
    private GeoPoint location;
    private Float rating;
    private Boolean isShaded;
    private Boolean quietStreet;
    private Boolean nearCafe;
    private String size;
    private String imageUri;

    // Empty constructor (for Firestore)
    public Bench() {
    }

    // Full constructor
    public Bench(String name, GeoPoint location, Float rating, Boolean isShaded, Boolean quietStreet, Boolean nearCafe, String size, String imageUri) {
        this.name = name;
        this.location = location;
        this.rating = rating;
        this.isShaded = isShaded;
        this.quietStreet = quietStreet;
        this.nearCafe = nearCafe;
        this.size = size;
        this.imageUri = imageUri;
    }

    public String getBenchName() {
        return name;
    }

    public Float getRating() {
        return rating;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri (String imageUri)
    {
        this.imageUri = imageUri;
    }

    public static Map<String, Object> toHashMap(Bench bench) {
        Map<String, Object> benchMap = new HashMap<>();
        benchMap.put("name", bench.name);
        benchMap.put("location", bench.location);
        benchMap.put("rating", bench.rating);
        benchMap.put("isShaded", bench.isShaded);
        benchMap.put("quietStreet", bench.quietStreet);
        benchMap.put("nearCafe", bench.nearCafe);
        benchMap.put("size", bench.size);
        benchMap.put("imageUri", bench.imageUri);
        return benchMap;
    }
}


