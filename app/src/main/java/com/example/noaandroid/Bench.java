package com.example.noaandroid;

import com.google.firebase.firestore.GeoPoint;

public class Bench {
    private String name;
    private GeoPoint location;
    private Float rating;
    private Boolean isShaded;
    private Boolean quietStreet;
    private Boolean nearCafe;
    private String size;
    private String imageUrl;

    // Empty constructor (for Firestore)
    public Bench() {}

    // Full constructor
    public Bench(String name, GeoPoint location, Float rating, Boolean isShaded, Boolean quietStreet, Boolean nearCafe, String size, String imageUrl) {
        this.name = name;
        this.location = location;
        this.rating = rating;
        this.isShaded = isShaded;
        this.quietStreet = quietStreet;
        this.nearCafe = nearCafe;
        this.size = size;
        this.imageUrl = imageUrl;
    }

}
