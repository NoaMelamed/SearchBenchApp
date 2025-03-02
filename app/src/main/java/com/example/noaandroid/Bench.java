package com.example.noaandroid;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.DoubleStream;

public class Bench {
    private String name;
    public GeoPoint location;
    private List<Float> rating;
    private Boolean isShaded;
    private Boolean quietStreet;
    private Boolean nearCafe;
    private String size;
    private List<String> imageUri;
    private String benchId;
    private double averageRating;

    // Empty constructor (for Firestore)
    public Bench() {
        rating = new ArrayList<>();
        imageUri = new ArrayList<>();
        averageRating = 0.0; // Initialize averageRating
    }

    // Full constructor
    public Bench(String name, GeoPoint location, Boolean isShaded, Boolean quietStreet, Boolean nearCafe, String size, List<String> imageUri, List<Float> rating, String benchId) {
        this.name = name;
        this.location = location;
        this.rating = rating;
        this.isShaded = isShaded;
        this.quietStreet = quietStreet;
        this.nearCafe = nearCafe;
        this.size = size;
        this.imageUri = imageUri;
        this.benchId = benchId;
        this.averageRating = getAverageRating(); // Calculate averageRating on creation
    }

    public double getAverageRating() {
        if (rating == null || rating.isEmpty()) {
            return 0.0; // Handle empty or null ratings list
        }
        return getRatingStream().average().orElse(0.0);
    }

    public DoubleStream getRatingStream(){
        return rating.stream().mapToDouble(Float::doubleValue);
    }


    // Getters and setters

    public void setBenchId(String benchId) {
        this.benchId = benchId;
    }


    public List<Float> getRating() {
        return rating;
    }

    public void setRating(List<Float> rating) {
        this.rating = rating;
    }

    public String getBenchId() {
        return benchId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public Boolean getIsShaded() {
        return isShaded;
    }

    public void setIsShaded(Boolean isShaded) {
        this.isShaded = isShaded;
    }

    public Boolean getQuietStreet() {
        return quietStreet;
    }

    public void setQuietStreet(Boolean quietStreet) {
        this.quietStreet = quietStreet;
    }

    public Boolean getNearCafe() {
        return nearCafe;
    }

    public void setNearCafe(Boolean nearCafe) {
        this.nearCafe = nearCafe;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public List<String> getImageUri() {
        return imageUri;
    }

    public void setImageUri(List<String> imageUri) {
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
        benchMap.put("averageRating", bench.averageRating);
        return benchMap;
    }
}
