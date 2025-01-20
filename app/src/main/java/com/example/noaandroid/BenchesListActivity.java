package com.example.noaandroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class BenchesListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<Bench> benches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_benches_list);

                initRecyclerView();
                initNavigation();
                loadBenchesFromFirestore();
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.benches_recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        benches = new ArrayList<>();
        BenchesAdapter adapter = new BenchesAdapter(benches);  // Use empty list initially
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



    private void loadBenchesFromFirestore() {
        // Create a reference to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query the "benches" collection
        db.collection("benches")
                .get()  // Fetch all documents in the collection
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Bench> benches = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            // Map each Firestore document to a Bench object
                            String benchName = document.getString("name");
                            GeoPoint location = document.getGeoPoint("location");
                            Float rating = document.getDouble("rating").floatValue();
                            Boolean isShaded = document.getBoolean("isShaded");
                            Boolean quietStreet = document.getBoolean("quietStreet");
                            Boolean nearCafe = document.getBoolean("nearCafe");
                            String size = document.getString("size");
                            String imageUriString = document.getString("imageUri");

                            // Create a new Bench object and add it to the list
                            Bench bench = new Bench(benchName, location, rating, isShaded, quietStreet, nearCafe, size, imageUriString);
                            benches.add(bench);
                        }
                        // Pass the benches list to the adapter to update the RecyclerView
                        updateRecyclerView(benches);
                    } else {
                        Toast.makeText(this, "Error getting documents.", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void updateRecyclerView (List<Bench> benches) {
        // Create a new BenchAdapter with the list of benches
        BenchesAdapter adapter = new BenchesAdapter(benches);

        // Set the adapter to the RecyclerView
        recyclerView.setAdapter(adapter);
    }


}
