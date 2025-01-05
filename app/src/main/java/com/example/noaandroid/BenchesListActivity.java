package com.example.noaandroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class BenchesListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_benches_list);

                initRecyclerView();
                initNavigation();
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.benches_recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<String> benchesList = createSampleData(); //TODO: replace w actual data
        BenchesAdapter adapter = new BenchesAdapter(benchesList);
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

    private List<String> createSampleData() {
        List<String> benchesList = new ArrayList<>();
        benchesList.add("Bench 1:Near the lake");
        benchesList.add("Bench 2:Under the big tree");
        benchesList.add("Bench 3:Near the playground");
        return benchesList;
    }

}
