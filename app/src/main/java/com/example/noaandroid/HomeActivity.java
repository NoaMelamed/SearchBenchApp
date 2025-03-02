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

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        if (savedInstanceState == null) {
            BenchFiltersFragment filterFragment = new BenchFiltersFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.filterFragmentContainer, filterFragment)
                    .commit();
        }

        TextView filterTextView = findViewById(R.id.filterByTextView);
        filterTextView.setOnClickListener(v -> showFilterFragment());

        getLastLocation();

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        ImageView backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> onBackPressed());

        ImageView menuIcon = findViewById(R.id.menu_icon);
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        navigationView.bringToFront();

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

        Button findBenchBtn = findViewById(R.id.findBenchButton);
        findBenchBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, BenchesListActivity.class);
            intent.putExtra("defaultFilters", true);
            startActivity(intent);
        });
    }

    private void showFilterFragment() {
        Fragment filterFragment = new BenchFiltersFragment();

        View fragmentContainer = findViewById(R.id.filterFragmentContainer);
        fragmentContainer.setVisibility(View.VISIBLE);

        fragmentContainer.setOnTouchListener((v, event) -> {
            getSupportFragmentManager().popBackStack();
            v.setVisibility(View.GONE);
            return true;
        });

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.filterFragmentContainer, filterFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (currentLocation != null) {
            LatLng current = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.addMarker(new MarkerOptions().position(current).title("I am here"));
        } else {
            Toast.makeText(this, "Unable to fetch current location", Toast.LENGTH_SHORT).show();
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }

        Task<Location> task = fusedLocationClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.mapFragment);
                    mapFragment.getMapAsync(HomeActivity.this);
                }
            }
        });
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Permission is denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
