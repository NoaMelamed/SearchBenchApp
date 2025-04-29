    package com.example.noaandroid;

    import com.google.firebase.firestore.GeoPoint;

    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.stream.DoubleStream;
    public class Bench {
        private String name; // The name of the bench
        public GeoPoint location; // The geographical location of the bench
        private List<Float> rating; // List to store individual ratings of the bench
        private Boolean isShaded; // Whether the bench has shade or not
        private Boolean quietStreet; // Whether the bench is on a quiet street or not
        private Boolean nearCafe; // Whether the bench is near a cafe or not
        private String size; // The size of the bench (e.g., single, regular)
        private List<String> imageUri; // List of image URIs associated with the bench
        private String benchId; // Unique identifier for the bench
        private double averageRating; // The average rating for the bench

        // Empty constructor (for Firestore)
        /**
         * Default constructor to be used by Firestore for object deserialization.
         * Initializes the ratings list, imageUri list, and sets the averageRating to 0.0.
         */
        public Bench() {
            rating = new ArrayList<>();
            imageUri = new ArrayList<>();
            averageRating = 0.0; // Initialize averageRating
        }

        // Full constructor
        /**
         * Full constructor to initialize a bench object with all the details.
         *
         * @param name The name of the bench.
         * @param location The geographical location of the bench.
         * @param isShaded Whether the bench has shade.
         * @param quietStreet Whether the bench is on a quiet street.
         * @param nearCafe Whether the bench is near a cafe.
         * @param size The size of the bench.
         * @param imageUri List of image URIs associated with the bench.
         * @param rating List of individual ratings for the bench.
         * @param benchId The unique identifier for the bench.
         */
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

        /**
         * Calculates the average rating from the list of ratings.
         *
         * @return The average rating, or 0.0 if there are no ratings.
         */
        public double getAverageRating() {
            if (rating == null || rating.isEmpty()) {
                return 0.0; // Handle empty or null ratings list
            }
            return getRatingStream().average().orElse(0.0); // Calculate average of the ratings
        }

        /**
         * Converts the rating list into a stream of doubles.
         *
         * @return A DoubleStream representing the ratings.
         */
        public DoubleStream getRatingStream(){
            return rating.stream().mapToDouble(Float::doubleValue); // Convert Float to Double for averaging
        }

        // Getters and setters

        /**
         * Sets the benchId of the bench.
         *
         * @param benchId The unique identifier for the bench.
         */
        public void setBenchId(String benchId) {
            this.benchId = benchId;
        }

        /**
         * Gets the list of ratings for the bench.
         *
         * @return List of ratings.
         */
        public List<Float> getRating() {
            return rating;
        }

        /**
         * Sets the list of ratings for the bench.
         *
         * @param rating List of ratings.
         */
        public void setRating(List<Float> rating) {
            this.rating = rating;
        }

        /**
         * Gets the benchId of the bench.
         *
         * @return The unique identifier for the bench.
         */
        public String getBenchId() {
            return benchId;
        }

        /**
         * Gets the name of the bench.
         *
         * @return The name of the bench.
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the name of the bench.
         *
         * @param name The name of the bench.
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Gets the location of the bench.
         *
         * @return The geographical location of the bench.
         */
        public GeoPoint getLocation() {
            return location;
        }

        /**
         * Sets the location of the bench.
         *
         * @param location The geographical location of the bench.
         */
        public void setLocation(GeoPoint location) {
            this.location = location;
        }

        /**
         * Gets whether the bench is shaded.
         *
         * @return True if the bench has shade, false otherwise.
         */
        public Boolean getIsShaded() {
            return isShaded;
        }

        /**
         * Sets whether the bench is shaded.
         *
         * @param isShaded True if the bench has shade, false otherwise.
         */
        public void setIsShaded(Boolean isShaded) {
            this.isShaded = isShaded;
        }

        /**
         * Gets whether the bench is on a quiet street.
         *
         * @return True if the bench is on a quiet street, false otherwise.
         */
        public Boolean getQuietStreet() {
            return quietStreet;
        }

        /**
         * Sets whether the bench is on a quiet street.
         *
         * @param quietStreet True if the bench is on a quiet street, false otherwise.
         */
        public void setQuietStreet(Boolean quietStreet) {
            this.quietStreet = quietStreet;
        }

        /**
         * Gets whether the bench is near a cafe.
         *
         * @return True if the bench is near a cafe, false otherwise.
         */
        public Boolean getNearCafe() {
            return nearCafe;
        }

        /**
         * Sets whether the bench is near a cafe.
         *
         * @param nearCafe True if the bench is near a cafe, false otherwise.
         */
        public void setNearCafe(Boolean nearCafe) {
            this.nearCafe = nearCafe;
        }

        /**
         * Gets the size of the bench.
         *
         * @return The size of the bench (e.g., single, regular).
         */
        public String getSize() {
            return size;
        }

        /**
         * Sets the size of the bench.
         *
         * @param size The size of the bench.
         */
        public void setSize(String size) {
            this.size = size;
        }

        /**
         * Gets the list of image URIs associated with the bench.
         *
         * @return The list of image URIs.
         */
        public List<String> getImageUri() {
            return imageUri;
        }

        /**
         * Sets the list of image URIs associated with the bench.
         *
         * @param imageUri The list of image URIs.
         */
        public void setImageUri(List<String> imageUri) {
            this.imageUri = imageUri;
        }

        /**
         * Converts the bench object to a map that can be uploaded to Firestore.
         *
         * @param bench The bench object to be converted.
         * @return A map representing the bench data for Firestore.
         */
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
