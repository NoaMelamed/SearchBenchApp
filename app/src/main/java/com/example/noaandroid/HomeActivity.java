package com.example.noaandroid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * The main activity that displays the home screen with a map, filter options, and navigation drawer.
 */
public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Location currentLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private final int FINE_PERMISSION_CODE = 1;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    private Map<String, Object> filters = new HashMap<>();

    /**
     * Called when the activity is created. Sets up layout, views, and map.
     */
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Show the filter fragment by default
        if (savedInstanceState == null) {
            BenchFiltersFragment filterFragment = new BenchFiltersFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.filterFragmentContainer, filterFragment)
                    .commit();
        }

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initNavigation(); // Set up the navigation drawer
        initViews();      // Set up UI views
        initMap();        // Initialize the map
    }

    /**
     * Initializes the Google Map fragment.
     */
    private void initMap() {
        // Load the map asynchronously
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Initializes the navigation drawer and sets item listeners.
     */
    private void initNavigation() {
        getLastLocation(); // Fetch user's last known location

        // Bind layout elements
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        // Handle back button click
        ImageView backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> onBackPressed());

        // Open drawer when menu icon clicked
        ImageView menuIcon = findViewById(R.id.menu_icon);
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.bringToFront();

        // Handle navigation menu item selection
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_add_bench) {
                Intent intent = new Intent(HomeActivity.this, AddBenchActivity.class);
                startActivity(intent);
            } else if (item.getItemId() == R.id.nav_find_bench) {
                Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
                startActivity(intent);
            }
            return true;
        });
    }

    /**
     * Initializes views like the filter text and find button.
     */
    private void initViews() {
        // Handle clicking on "Filter By" text
        TextView filterTextView = findViewById(R.id.filterByTextView);
        filterTextView.setOnClickListener(v -> showFilterFragment());

        // Handle "Find Me a Bench" button click
        Button findBenchBtn = findViewById(R.id.findBenchButton);
        findBenchBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, BenchesListActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Displays the BenchFiltersFragment with a fade transition.
     */
    private void showFilterFragment() {
        Fragment filterFragment = new BenchFiltersFragment();

        // Make the filter fragment container visible
        View fragmentContainer = findViewById(R.id.filterFragmentContainer);
        fragmentContainer.setVisibility(View.VISIBLE);

        // Hide the fragment on outside touch
        fragmentContainer.setOnTouchListener((v, event) -> {
            getSupportFragmentManager().popBackStack();
            v.setVisibility(View.GONE);
            return true;
        });

        // Animate and replace the current fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.filterFragmentContainer, filterFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * Called when the map is ready to use. Adds a marker to current location.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        BitmapDescriptor customIcon = createCustomMarker("Your Location");

        if (currentLocation != null) {
            LatLng current = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

            // Add marker at user's location
            mMap.addMarker(new MarkerOptions()
                    .position(current)
                    .icon(customIcon)
                    .title("Your Location"));

            // Zoom camera to current location
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15));
        }
    }

    /**
     * Requests the last known location and updates the map.
     */
    private void getLastLocation() {
        // Check location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }

        // Get last known location
        Task<Location> task = fusedLocationClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    // Initialize map again with updated location
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.mapFragment);
                    mapFragment.getMapAsync(HomeActivity.this);
                }
            }
        });
    }

    /**
     * Handles the result of the permission request.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation(); // Try to fetch location again
            } else {
                Toast.makeText(this, "Permission is denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Creates a custom map marker with text.
     *
     * @param text The text to display on the marker.
     * @return BitmapDescriptor for the custom marker.
     */
    private BitmapDescriptor createCustomMarker(String text) {
        // Inflate custom marker layout
        View markerView = getLayoutInflater().inflate(R.layout.custom_marker_layout, null);
        TextView textView = markerView.findViewById(R.id.customMarkerText);
        textView.setText(text);

        // Convert view to bitmap
        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        markerView.layout(0, 0, markerView.getMeasuredWidth(), markerView.getMeasuredHeight());
        markerView.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerView.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
