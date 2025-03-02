package com.example.noaandroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import com.example.noaandroid.Bench;


public class SingleBenchActivity extends AppCompatActivity implements OnMapReadyCallback, RatingFragment.OnRatingSubmittedListener {

    private TextView benchTitle;
    private Button viewGalleryButton, rateBenchButton;
    private RatingBar ratingBar;
    private Switch switchShade, switchNoise, switchCafe;
    private ImageView benchImage;
    private GoogleMap mMap;
    private Bench bench;
    private String benchId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_bench);

        initViews();
        initListeners();
        initNavigation();

        // Get benchId from the intent
        benchId = getIntent().getStringExtra("benchDocId");

        if (benchId != null) {
            loadBenchDetails(benchId);
        } else {
            Toast.makeText(this, "Error: Bench ID not found", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if no ID is provided
        }

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        initListeners();
    }

    private void initViews() {
        benchTitle = findViewById(R.id.bench_title);
        viewGalleryButton = findViewById(R.id.view_gallery);
        rateBenchButton = findViewById(R.id.rate_bench);
        ratingBar = findViewById(R.id.rating_bar);
        switchShade = findViewById(R.id.switch_shade);
        switchNoise = findViewById(R.id.switch_noise);
        switchCafe = findViewById(R.id.switch_cafe);
        benchImage = findViewById(R.id.bench_image);
    }

    private void loadBenchDetails(String benchId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference benchRef = db.collection("benches").document(benchId);

        benchRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                bench = documentSnapshot.toObject(Bench.class);
                if (bench != null) {
                    populateBenchDetails();
                }
            } else {
                Toast.makeText(SingleBenchActivity.this, "Bench not found", Toast.LENGTH_SHORT).show();
                finish(); // Close activity if the bench doesn't exist
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(SingleBenchActivity.this, "Error loading bench", Toast.LENGTH_SHORT).show();
            Log.e("Firestore", "Error fetching bench data", e);
        });
    }

    private void populateBenchDetails() {
        benchTitle.setText(bench.getName());
        ratingBar.setRating((float) bench.getAverageRating());
        switchShade.setChecked(bench.getIsShaded());
        switchNoise.setChecked(bench.getQuietStreet());
        switchCafe.setChecked(bench.getNearCafe());

        // Load first image if available (assuming getImageUri() returns a list of URLs)
        if (bench.getImageUri() != null && !bench.getImageUri().isEmpty()) {
            // Get the first image URL from the list
            String firstImageUrl = bench.getImageUri().get(0);

            // Load the first image URL into the ImageView
            Glide.with(this).load(firstImageUrl).into(benchImage);
        } else {
            // Load a placeholder image if no images are available
            benchImage.setImageResource(R.drawable.new_logo_icon_only);
        }

        // Add marker to map if location is available
        if (mMap != null && bench.getLocation() != null) {
            LatLng benchLocation = new LatLng(bench.getLocation().getLatitude(), bench.getLocation().getLongitude());
            mMap.addMarker(new MarkerOptions().position(benchLocation).title(bench.getName()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(benchLocation, 15));
        }
    }

    private void initListeners() {
        viewGalleryButton.setOnClickListener(v -> showGalleryFragment());
        rateBenchButton.setOnClickListener(v -> showRatingFragment());
    }

    private void showGalleryFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment existingFragment = fragmentManager.findFragmentByTag("GalleryFragment");

        if (existingFragment != null) {
            fragmentManager.beginTransaction().remove(existingFragment).commit(); // Ensure it's removed
            fragmentManager.popBackStack(); // Clear back stack to allow re-opening
        }

        if (bench != null && bench.getImageUri() != null && !bench.getImageUri().isEmpty()) {
            ImageGalleryFragment imageGalleryFragment = ImageGalleryFragment.newInstance(bench.getImageUri());
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, imageGalleryFragment, "GalleryFragment") // Tag for reference
                    .addToBackStack(null)
                    .commitAllowingStateLoss(); // Ensure transaction commits properly
        } else {
            Toast.makeText(this, "No images to display", Toast.LENGTH_SHORT).show();
        }
    }





    private void showRatingFragment() {
        RatingFragment ratingFragment = new RatingFragment();
        ratingFragment.setOnRatingSubmittedListener(this);
        ratingFragment.show(getSupportFragmentManager(), "ratingFragment");
    }

    public void onRatingSubmitted(float rating) {
        updateUserRating(benchId, rating);
    }

    private void updateUserRating(String benchId, float newRating) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference benchRef = db.collection("benches").document(benchId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(benchRef);
            if (!snapshot.exists()) {
                return null;
            }
            Bench bench = snapshot.toObject(Bench.class);
            if (bench == null) return null;

            List<Float> ratings = bench.getRating();
            if (ratings == null) {
                ratings = new ArrayList<>();
            }
            ratings.add(newRating);
            bench.setRating(ratings); // Update the ratings list in the Bench object

            transaction.set(benchRef, bench); // Use set to update the entire document
            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(SingleBenchActivity.this, "Rating updated!", Toast.LENGTH_SHORT).show();
            // Refresh UI, this will trigger a recalculation of the average in the Bench class
            loadBenchDetails(benchId);
        }).addOnFailureListener(e -> {
            Toast.makeText(SingleBenchActivity.this, "Error updating rating", Toast.LENGTH_SHORT).show();
            Log.e("Firestore", "Error updating rating", e);
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (bench != null && bench.getLocation() != null) {
            LatLng benchLocation = new LatLng(bench.getLocation().getLatitude(), bench.getLocation().getLongitude());
            mMap.addMarker(new MarkerOptions().position(benchLocation).title(bench.getName()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(benchLocation, 15));
        }
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
                Intent intent = new Intent(SingleBenchActivity.this, AddBenchActivity.class);
                startActivity(intent);
            } else if (item.getItemId() == R.id.nav_find_bench) {
                Intent intent = new Intent(SingleBenchActivity.this, HomeActivity.class);
            }
            return true;
        });
    }
}
