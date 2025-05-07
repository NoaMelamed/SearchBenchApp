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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BenchesListActivity extends AppCompatActivity {

    public static final double HIGH_RATING_MIN_VAL = 4.0;
    public static final int SHORT_DISTANCE_MIN_VALUE = 500;
    public static final List<String> ALL_SIZES = Arrays.asList("Single", "Regular", "Picnic");
    public static final List<Boolean> ALL_BOOLS = Arrays.asList(true, false);
    // UI and adapter
    private RecyclerView recyclerView;
    private BenchesAdapter adapter;

    private FusedLocationProviderClient fusedLocationClient;

    /**
     * Called when the activity is starting
     */
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

        displaySelectedBenches();
    }

    /**
     * Initializes the RecyclerView and its adapter
     */
    private void initRecyclerView() {
        // Set up layout manager and adapter
        recyclerView = findViewById(R.id.benches_recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BenchesAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    /**
     * Sets up the side navigation drawer and toolbar
     */
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

    /**
     * Checks if Google Play Services are available
     */
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

    private void displaySelectedBenches() {
        final Bundle filtersBundle;
        if (getIntent().getExtras() == null) {
            // when no filter is defined, we initialize the bundle accordingly
            filtersBundle = new Bundle();
            filtersBundle.putString("size", null);
            filtersBundle.putBoolean("isShaded", false);
            filtersBundle.putBoolean("quietStreet", false);
            filtersBundle.putBoolean("nearCafe", false);
            filtersBundle.putBoolean("shortDistance", false);
            filtersBundle.putBoolean("highRated", false);
        } else {
            // the following Bundle was created by BenchFiltersFragment.saveFilters()
            filtersBundle = getIntent().getExtras();
        }

        createDbQuery(filtersBundle).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                this.onQueryComplete(task.getResult().toObjects(Bench.class), filtersBundle);
            } else {
                // database is not functioning as expected
                Log.e("Noa's", "Error fetching benches", task.getException());
            }
        });
    }

    @NonNull
    private static Query createDbQuery(Bundle filtersBundle) {
        /**
         * assumes composite index of fields in the following order:
         * [size, isShaded, quietStreet, nearCafe, averageRating]
         */
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("benches");

        Log.d("Noa's", filtersBundle.toString());

        String size = (String) filtersBundle.get("size");
        if (size != null) {
            query = query.whereEqualTo("size", size);
        } else {
            query = query.whereIn("size", ALL_SIZES);
        }

        for (String boolFilter : Arrays.asList("isShaded", "quietStreet", "nearCafe")) {
            // NOTE: all bool-fields are non-null
            if ((boolean) filtersBundle.get(boolFilter)) {
                query = query.whereEqualTo(boolFilter, true);
            } else {
                query = query.whereIn(boolFilter, ALL_BOOLS);
            }
        }

        // NOTE: highRated field is non-null
        if ((boolean) filtersBundle.get("highRated")) {
            // Log.d("Noa's", boolFilter + "=true");
            query = query.whereGreaterThanOrEqualTo("averageRating", HIGH_RATING_MIN_VAL);
        } else {
            // Log.d("Noa's", boolFilter + "=" + ALL_BOOLS);
            query = query.whereGreaterThanOrEqualTo("averageRating", 0.0);
        }

        return query;
    }

    private void onQueryComplete(List<Bench> benches, Bundle filtersBundle) {
        if (benches.isEmpty()) {
            Log.w("Noa's", "No matching benches found");
            Toast.makeText(this, "No matching benches found", Toast.LENGTH_SHORT).show();
        } else {
            Log.i("Noa's", "Bench Queries returned " + benches.size() + " benches");
            boolean shortDistance = (boolean) filtersBundle.get("shortDistance");
            boolean hasLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            if (shortDistance && hasLocationPermission) {
                fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                    final List<Bench> geoFilteredBenches;
                    if (task.isSuccessful() && task.getResult() != null) {
                        Log.i("Noa's", "Device has LastLocation");
                        Location location = task.getResult();
                        GeoPoint userLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                        geoFilteredBenches = filterByDistance(benches, userLocation, SHORT_DISTANCE_MIN_VALUE);
                        Log.i("Noa's", "Bench Queries shrunk to " + benches.size() + " benches");
                    } else {
                        Log.w("Noa's", "Device failed to get LastLocation");
                        Toast.makeText(this, "Location not available, not filtering by distance", Toast.LENGTH_SHORT).show();
                        geoFilteredBenches = benches;
                    }
                    updateRecyclerView(geoFilteredBenches);
                });
            } else {
                updateRecyclerView(benches);
            }
        }
    }

    /**
     * Filters a list of benches by distance
     */
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

    /**
     * Returns distance in meters between two GeoPoints
     */
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

    /**
     * Updates the UI with a list of benches
     */
    private void updateRecyclerView(List<Bench> benchesList) {
        // Update adapter and hide no results
        TextView noResultsMessage = findViewById(R.id.noResultsMessage);
        recyclerView.setVisibility(View.VISIBLE);
        noResultsMessage.setVisibility(View.GONE);
        adapter.updateData(benchesList);
    }
}
