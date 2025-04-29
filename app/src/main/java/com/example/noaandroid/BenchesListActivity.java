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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BenchesListActivity extends AppCompatActivity {

    // UI and adapter
    private RecyclerView recyclerView;
    private BenchesAdapter adapter;

    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private double userLatitude;
    private double userLongitude;
    private static final double DEFAULT_LATITUDE = 32.0767;
    private static final double DEFAULT_LONGITUDE = 34.7778;

    // Permission codes
    private final int FINE_PERMISSION_CODE = 1;
    private static final int REQUEST_CODE = 1001;

    // Pending data for location permission callback
    private Map<String, Object> pendingFilters;
    private Map<String, Object> pendingAllFields;

    /** Called when the activity is starting */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_benches_list);

        // Set up location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize UI components
        initRecyclerView();
        initNavigation();

        //Check for necessary services
        checkGooglePlayServices();

        // Fetch filter data and benches
        prepareFiltersAndFetchData();
    }

    /** Initializes the RecyclerView and its adapter */
    private void initRecyclerView() {
        // Set up layout manager and adapter
        recyclerView = findViewById(R.id.benches_recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BenchesAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    /** Sets up the side navigation drawer and toolbar */
    private void initNavigation() {
        // Connect drawer and toolbar
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);

        // Set toolbar as action bar
        setSupportActionBar(toolbar);

        // Handle toolbar buttons
        ImageView backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> onBackPressed());

        ImageView menuIcon = findViewById(R.id.menu_icon);
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        navigationView.bringToFront();

        // Handle menu item selection
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

    /** Checks if Google Play Services are available */
    private void checkGooglePlayServices() {
        // Validate Google services
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

    /** Loads one document to extract fields and fetch benches with filters */
    private void prepareFiltersAndFetchData() {
        // Fetch initial fields from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("benches").limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                Map<String, Object> allFields = new HashMap<>();
                if (document.getData() != null) {
                    allFields.putAll(document.getData());
                }

                // Get filters and request permission if needed
                Map<String, Object> filters = retrieveFilters();
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    getLastLocationAndThenFetch(filters, allFields);
                } else {
                    pendingFilters = filters;
                    pendingAllFields = allFields;
                    requestLocationPermission();
                }
            } else {
                Log.e("Firestore", "Error fetching fields", task.getException());
                showNoResultsMessage("Error fetching initial data. Please try again later.");
            }
        });
    }

    /** Retrieves filter settings from the intent */
    private Map<String, Object> retrieveFilters() {
        // Parse intent extras
        Intent intent = getIntent();
        Map<String, Object> filters = new HashMap<>();
        String size = intent.getStringExtra("size");
        if (size != null && !size.trim().isEmpty()) {
            filters.put("size", size);
        } else {
            filters.put("size", Arrays.asList("Single", "Regular", "Picnic"));
        } if (intent.getBooleanExtra("inShade", false)) {
            filters.put("shade", true);
        } else {
            filters.put("shade", Arrays.asList(true, false));
        }
        if (intent.getBooleanExtra("quietStreet", false)) {
            filters.put("quietStreet", true);
        }
        else {
            filters.put("quietStreet", Arrays.asList(true, false));
        }
        if (intent.getBooleanExtra("nearCafe", false)) {
            filters.put("nearCafe", true);
        }
        else {
            filters.put("nearCafe", Arrays.asList(true, false));
        }

        //
        filters.put("shortDistance", intent.getBooleanExtra("shortDistance", false));
        filters.put("highRated", intent.getBooleanExtra("highRated", false));


        Log.d("DEBUG", "Retrieved Filters: " + filters.toString());
        return filters;
    }

    /** Fetches benches using Firestore based on filters */
    private void fetchFilteredBenches(Map<String, Object> userFilters, Map<String, Object> allFields) {
        // Build and run Firestore query
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference benchesRef = db.collection("benches");
        Query query = applyFilters(benchesRef, userFilters, allFields);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Bench> benches = task.getResult().toObjects(Bench.class);
                if (Boolean.TRUE.equals(userFilters.get("shortDistance"))) {
                    GeoPoint userLocation = new GeoPoint(userLatitude, userLongitude);
                    benches = filterByDistance(benches, userLocation, 500);
                }
                if (Boolean.TRUE.equals(userFilters.get("highRated"))) {
                    //TODO filter by rating
                }
                    updateRecyclerView(benches);
            } else {
                Log.e("Firestore", "Error fetching benches", task.getException());
                showNoResultsMessage("No matching benches found.");
            }
        });
    }

    /** Applies user filters to a Firestore query */
    private Query applyFilters(Query query, Map<String, Object> userFilters, Map<String, Object> allFields) {
        // Filter fields
        for (Map.Entry<String, Object> entry : allFields.entrySet()) {
            String fieldName = entry.getKey();
            Object filterValue = userFilters.get(fieldName);
            if (!fieldName.equals("highRated") && !fieldName.equals("shortDistance")) {
                query = query.whereEqualTo(fieldName, filterValue);
            }
        }
//        if (Boolean.TRUE.equals(userFilters.get("highRated"))) {
//            query = query.whereGreaterThanOrEqualTo("averageRating", 4.0);
//        }
        return query;
    }

    /** Applies a single user filter to a query */
//    private Query applyUserFilter(Query query, String fieldName, Object filterValue) {
//        // Add Firestore filter clause
//        if (filterValue instanceof Boolean) {
//            return query.whereEqualTo(fieldName, filterValue);
//        }
//        else if (filterValue instanceof String) {
//            return query.whereEqualTo(fieldName, filterValue);
//        } else {
//            Log.w("FirestoreQuery", "Unsupported filter type: " + filterValue.getClass());
//            return query;
//        }
//    }

    /** Filters a list of benches by distance */
    private List<Bench> filterByDistance(List<Bench> benches, GeoPoint userLocation, double radius) {
        // Keep only nearby benches
        List<Bench> nearbyBenches = new ArrayList<>();
        for (Bench bench : benches) {
            if (bench.getLocation() != null && getDistanceBetween(userLocation, bench.getLocation()) <= radius) {
                nearbyBenches.add(bench);
            }
        }
        return nearbyBenches;
    }

    /** Returns distance in meters between two GeoPoints */
    private double getDistanceBetween(GeoPoint loc1, GeoPoint loc2) {
        // Calculate haversine distance
        double earthRadius = 6371;
        double lat1 = Math.toRadians(loc1.getLatitude());
        double lon1 = Math.toRadians(loc1.getLongitude());
        double lat2 = Math.toRadians(loc2.getLatitude());
        double lon2 = Math.toRadians(loc2.getLongitude());
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;
        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c * 1000;
    }

    /** Updates the UI with a list of benches */
    private void updateRecyclerView(List<Bench> benchesList) {
        // Update adapter and hide no results
        TextView noResultsMessage = findViewById(R.id.noResultsMessage);
        recyclerView.setVisibility(View.VISIBLE);
        noResultsMessage.setVisibility(View.GONE);
        adapter.updateData(benchesList);
    }

    /** Shows a message when no results are found */
    private void showNoResultsMessage(String message) {
        // Show no results UI
        TextView noResultsMessage = findViewById(R.id.noResultsMessage);
        RecyclerView recyclerView = findViewById(R.id.benches_recycle_view);
        noResultsMessage.setText(message);
        noResultsMessage.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    /** Gets last known location and then fetches benches */
    private void getLastLocationAndThenFetch(Map<String, Object> filters, Map<String, Object> allFields) {
        // Use device location if permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            fetchFilteredBenches(filters, allFields);
            return;
        }

        fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                currentLocation = task.getResult();
                userLatitude = currentLocation.getLatitude();
                userLongitude = currentLocation.getLongitude();
            } else {
                userLatitude = DEFAULT_LATITUDE;
                userLongitude = DEFAULT_LONGITUDE;
            }
            fetchFilteredBenches(filters, allFields);
        });
    }

    /** Requests location permission */
    private void requestLocationPermission() {
        // Ask user for fine location access
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    /** Called when the user responds to permission request */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingFilters != null && pendingAllFields != null) {
                    getLastLocationAndThenFetch(pendingFilters, pendingAllFields);
                    pendingFilters = null;
                    pendingAllFields = null;
                } else {
                    getLastLocationAndThenFetch(retrieveFilters(), new HashMap<>());
                }
            } else {
                Toast.makeText(this, "Location permission is needed to show nearby benches", Toast.LENGTH_SHORT).show();
                if (pendingFilters != null && pendingAllFields != null) {
                    userLatitude = DEFAULT_LATITUDE;
                    userLongitude = DEFAULT_LONGITUDE;
                    fetchFilteredBenches(pendingFilters, pendingAllFields);
                    pendingFilters = null;
                    pendingAllFields = null;
                }
            }
        }
    }
}
