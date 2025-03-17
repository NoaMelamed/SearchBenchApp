package com.example.noaandroid;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class BenchesListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private BenchesAdapter adapter;
    private FusedLocationProviderClient fusedLocationClient;
    private final int FINE_PERMISSION_CODE = 1;
    private static final int REQUEST_CODE = 1001; // You can change the number if needed

    private static final double DEFAULT_LATITUDE = 32.0767; // Slightly off-center Tel Aviv
    private static final double DEFAULT_LONGITUDE = 34.7778;
    private double userLatitude;
    private double userLongitude;
    Location currentLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_benches_list);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initRecyclerView();
        initNavigation();

        checkGooglePlayServices();

        fetchAllFields();

    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.benches_recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BenchesAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    private void initNavigation() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar); // Set toolbar as ActionBar

        // Handle toolbar buttons
        ImageView backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> onBackPressed());

        ImageView menuIcon = findViewById(R.id.menu_icon);
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        navigationView.bringToFront();

        // Setup navigation item selection
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_add_bench) {
                Intent intent = new Intent(BenchesListActivity.this, AddBenchActivity.class);
                startActivity(intent);
            } else if (item.getItemId() == R.id.nav_find_bench) {
                Intent intent = new Intent(BenchesListActivity.this, HomeActivity.class);
                startActivity(intent);
            }
            return true;
        });
    }


    private void fetchAllFields() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("benches").limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                DocumentSnapshot document = task.getResult().getDocuments().get(0);

                // Explicitly create a HashMap to prevent type confusion
                Map<String, Object> allFields = new HashMap<>();
                if (document.getData() != null) {
                    allFields.putAll(document.getData()); // Copy data safely
                }

                // Pass a Runnable instead of a Map
                onRequestPermission(() -> fetchFilteredBenches(retrieveFilters(), allFields));

            } else {
                Log.e("Firestore", "Error fetching fields", task.getException());
                showNoResultsMessage("Error fetching initial data. Please try again later.");
            }
        });
    }


    private Map<String, Object> retrieveFilters() { // only selected ones
        Intent intent = getIntent();
        Map<String, Object> filters = new HashMap<>();
        // Only add the "size" filter if it's provided (non-null and non-empty)
        String size = intent.getStringExtra("size");
        if (size != null && !size.trim().isEmpty()) { //Handle empty string case
            filters.put("size", size);
        }
        // For boolean filters, only include them if they are true (i.e., selected by the user)
        if (intent.getBooleanExtra("inShade", false)) {
            filters.put("shade", true);
        } if (intent.getBooleanExtra("quietStreet", false)) {
            filters.put("quietStreet", true);
        } if (intent.getBooleanExtra("nearCafe", false)) {
            filters.put("nearCafe", true);
        } if (intent.getBooleanExtra("shortDistance", false)) {
            filters.put("shortDistance", true);
        } if (intent.getBooleanExtra("highRated", false)) {
            filters.put("highRated", true);
        }

        Log.d("DEBUG", "Retrieved Filters: " + filters.toString()); // 🔥 Log retrieved filters
        return filters;
    }


    private void fetchFilteredBenches(Map<String, Object> userFilters, Map<String, Object> allFields) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference benchesRef = db.collection("benches");
        Query query = applyFilters(benchesRef, userFilters, allFields);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Bench> benches = task.getResult().toObjects(Bench.class);
                Log.d("DEBUG", "Fetched " + benches.size() + " benches before distance filtering");

                // Apply distance filtering if shortDistance is selected
                if (Boolean.TRUE.equals(userFilters.get("shortDistance"))) {
                    GeoPoint userLocation = new GeoPoint(userLatitude, userLongitude);
                    benches = filterByDistance(benches, userLocation, 500); // Filter benches within 500m
                }

                Log.d("DEBUG", "Benches after distance filtering: " + benches.size());
                updateRecyclerView(benches);
            } else {
                Log.e("Firestore", "Error fetching benches", task.getException());
                showNoResultsMessage("No matching benches found.");
            }
        });
    }

    private Query applyFilters(Query query, Map<String, Object> userFilters, Map<String, Object> allFields) {
        Log.d("DEBUG", "applyFilters called with userFilters: " + userFilters.toString());

        for (Map.Entry<String, Object> entry : allFields.entrySet()) {
            String fieldName = entry.getKey();
            Object fieldValue = userFilters.get(fieldName); // Get filter value from userFilters

            Log.d("DEBUG", "Checking field: " + fieldName + ", Value: " + fieldValue);

            if (fieldValue != null) {
                Log.d("DEBUG", "Applying filter for field: " + fieldName);
                query = applyUserFilter(query, fieldName, fieldValue);
            }
        }

        if (Boolean.TRUE.equals(userFilters.get("highRated"))) {
            Log.d("DEBUG", "Manually applying highRated filter (averageRating >= 4.0)");
            query = query.whereGreaterThanOrEqualTo("averageRating", 4.0);
        }

        return query;
    }

    private Query applyUserFilter(Query query, String fieldName, Object filterValue) {
        Log.d("DEBUG", "Applying filter: " + fieldName + " = " + filterValue);

        if (filterValue instanceof Boolean) {
            return query.whereEqualTo(fieldName, filterValue);
        } else if (filterValue instanceof Number) {
            return query.whereGreaterThanOrEqualTo(fieldName, filterValue);
        } else if (filterValue instanceof String) {
            // Handle String filters (case-insensitive)
            return query.whereEqualTo(fieldName, filterValue); //or use a more complex query
        } else {
            Log.w("FirestoreQuery", "Unsupported filter type: " + filterValue.getClass() + " for field: " + fieldName);
            return query;
        }
    }


    private boolean matchesStringFilter(Bench bench, Map<String, Object> userFilters, String fieldName) {
        Object filterValue = userFilters.get(fieldName);
        if (filterValue == null) return true; // No filter for this field

        switch (fieldName) {
            case "size":
                return filterValue.equals(bench.getSize());
            case "name":
                return filterValue.equals(bench.getName());
            case "imageUri":
                return filterValue.equals(bench.getImageUri());
            default:
                return true; //Shouldn't happen. Add a Log message here for debugging purposes
        }
    }



    private Query applyAllValuesFilter(Query query, String fieldName, Object fieldValue) {
        if (fieldValue == null) {
            Log.w("FirestoreQuery", "Field value is null for field: " + fieldName + ". Skipping filter.");
            return query; //Skip the filtering if the value is null
            // Handle "all values" case differently based on the field type
        }
            if (fieldValue instanceof Boolean) {
                // Query for both true and false (using 'in' operator)
                return query.whereIn(fieldName, Arrays.asList(true, false));
            } else if (fieldValue instanceof Number) {
                //Query all numbers (example: greater than or equals to 0)
                return query.whereGreaterThanOrEqualTo(fieldName, 0);
            } else if (fieldValue instanceof String) {
                //This will not work efficiently without a specific filter. Consider revising.
                Log.w("FirestoreQuery", "Cannot efficiently query all values for String field: " + fieldName);
                return query;
            } else {
                Log.w("FirestoreQuery", "Unsupported field type: " + fieldValue.getClass() + " for field: " + fieldName);
                return query;
            }
        }




    private void updateRecyclerView(List<Bench> benchesList) {
        Log.d("DEBUG", "Updating RecyclerView with " + benchesList.size() + " benches"); // 🔥 Log list size

        TextView noResultsMessage = findViewById(R.id.noResultsMessage);
        recyclerView.setVisibility(View.VISIBLE);
        noResultsMessage.setVisibility(View.GONE);
        adapter.updateData(benchesList);
    }

    private void showNoResultsMessage(String message) {
        TextView noResultsMessage = findViewById(R.id.noResultsMessage);
        RecyclerView recyclerView = findViewById(R.id.benches_recycle_view);
        noResultsMessage.setText(message);
        noResultsMessage.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void checkGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, 9000).show();
            } else {
                Toast.makeText(this, "This device is not supported.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    private void getLastLocation(Runnable callback) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        currentLocation = task.getResult();
                        userLatitude = currentLocation.getLatitude();
                        userLongitude = currentLocation.getLongitude();
                        Log.d("DEBUG", "User location retrieved: " + userLatitude + ", " + userLongitude);
                    } else {
                        Log.w("Location", "Failed to get location", task.getException());
                        userLatitude = DEFAULT_LATITUDE;
                        userLongitude = DEFAULT_LONGITUDE;
                    }
                    callback.run(); // Execute callback function
                });
    }


    private List<Bench> filterByDistance(List<Bench> benches, GeoPoint userLocation, double radius) {
        List<Bench> nearbyBenches = new ArrayList<>();
        for (Bench bench : benches) {
            if (bench.getLocation() != null && getDistanceBetween(userLocation, bench.getLocation()) <= radius) {
                nearbyBenches.add(bench);
            }
        }
        return nearbyBenches;
    }


    private double getDistanceBetween(GeoPoint loc1, GeoPoint loc2) {
        double earthRadius = 6371; // Radius of the earth in km
        double lat1 = Math.toRadians(loc1.getLatitude());
        double lon1 = Math.toRadians(loc1.getLongitude());
        double lat2 = Math.toRadians(loc2.getLatitude());
        double lon2 = Math.toRadians(loc2.getLongitude());
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;
        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanceInKilometers = earthRadius * c;
        return distanceInKilometers * 1000; // Convert to meters
    }

    private void onRequestPermission(Runnable actionIfGranted) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            actionIfGranted.run(); // This expects a Runnable
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }
    }

}
