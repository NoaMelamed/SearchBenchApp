package com.example.noaandroid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;


public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap; // Reference to the GoogleMap object
    Location currentLocation; // Stores the user's current location
    private FusedLocationProviderClient fusedLocationClient; // Client for accessing location services
    private final int FINE_PERMISSION_CODE = 1; // Code used to identify permission requests
    FirebaseFirestore db = FirebaseFirestore.getInstance(); // Instance of Firestore for database operations

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize the location services client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this); // Set callback for when the map is ready
        }

        // Load the FilterFragment dynamically on the first launch
        if (savedInstanceState == null) {
            BenchFiltersFragment filterFragment = new BenchFiltersFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.filterFragmentContainer, filterFragment)
                    .commit();
        }

        // Set up click listener for the "Filter By" TextView
        TextView filterTextView = findViewById(R.id.filterByTextView);
        filterTextView.setOnClickListener(v -> showFilterFragment());

        // Request the user's last known location
        getLastLocation();

        // Initialize views
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);

        // Setup toolbar
        setSupportActionBar(toolbar);

        // Handle toolbar buttons
        ImageView backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> onBackPressed());

        ImageView menuIcon = findViewById(R.id.menu_icon);
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        navigationView.bringToFront();
        // Setup navigation item selection
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_add_bench) {
                Intent intent = new Intent(HomeActivity.this, AddBenchActivity.class);
                startActivity(intent);
            } else if (item.getItemId() == R.id.nav_find_bench) {
                Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
                startActivity(intent);
            }

//            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

    }


    /**
     * Displays the filter fragment dynamically when the user clicks the "Filter By" text.
     */
    private void showFilterFragment() {
        Fragment filterFragment = new BenchFiltersFragment();

        // Make the container visible
        View fragmentContainer = findViewById(R.id.filterFragmentContainer);
        fragmentContainer.setVisibility(View.VISIBLE);

        // Add a click listener to dismiss the fragment when tapping outside
        fragmentContainer.setOnTouchListener((v, event) -> {
            getSupportFragmentManager().popBackStack(); // Removes the fragment from the back stack
            v.setVisibility(View.GONE); // Hides the container
            return true;
        });

        // Begin a fragment transaction to show the filter fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.filterFragmentContainer, filterFragment); // Replace to avoid overlap
        transaction.addToBackStack(null); // Allow back navigation
        transaction.commit();
    }


    /**
     * Called when the map is ready to be used.
     * @param googleMap The GoogleMap object to manipulate and add markers to.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker for the user's current location, if available
        if (currentLocation != null) {
            LatLng current = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.addMarker(new MarkerOptions().position(current).title("I am here"));
        } else {
            Toast.makeText(this, "Unable to fetch current location", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Requests the user's last known location and updates the map accordingly.
     */
    private void getLastLocation() {
        // Check if location permissions are granted; if not, request them
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }

        // Get the last known location
        Task<Location> task = fusedLocationClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;

                    // Notify the map fragment that the map is ready to display the location
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.mapFragment);
                    mapFragment.getMapAsync(HomeActivity.this);
                }
            }
        });
    }

    /**
     * Handles the result of a runtime permission request.
     * @param requestCode The code of the permission request.
     * @param permissions The permissions requested.
     * @param grantResults The results for the corresponding permissions.
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // If permission is granted, fetch the last known location
                getLastLocation();
            } else {
                // Notify the user if permission is denied
                Toast.makeText(this, "Permission is denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
