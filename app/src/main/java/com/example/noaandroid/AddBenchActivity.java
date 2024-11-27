package com.example.noaandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import androidx.drawerlayout.widget.DrawerLayout;


public class AddBenchActivity extends AppCompatActivity {

    private EditText etBenchName;
    private RatingBar ratingBar;
    private CheckBox cbShade, cbQuietStreet, cbNearCafe;
    private Spinner spinnerSize;
    private Button btnSelectLocation, btnUploadImage, btnSubmit;
    private ImageView imagePreview;
    private Uri imageUri = null; // URI for selected image
    private double lat = 0.0; // Dummy Latitude
    private double lng = 0.0; // Dummy Longitude

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bench);

        // Initialize Firestore and Storage
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Initialize views
        etBenchName = findViewById(R.id.et_bench_name);
        ratingBar = findViewById(R.id.rating_bar);
        cbShade = findViewById(R.id.cb_shade);
        cbQuietStreet = findViewById(R.id.cb_quiet_street);
        cbNearCafe = findViewById(R.id.cb_near_cafe);
        spinnerSize = findViewById(R.id.sp_size);
        btnSelectLocation = findViewById(R.id.btn_select_location);
//        btnUploadImage = findViewById(R.id.btn_upload_image);
        btnSubmit = findViewById(R.id.btn_submit);
//        imagePreview = findViewById(R.id.image_preview);

        // Set listeners
        btnSelectLocation.setOnClickListener(v -> selectLocation());
//        btnUploadImage.setOnClickListener(v -> uploadImage());
        btnSubmit.setOnClickListener(v -> submitBench());

        // Handle Spinner
        Spinner benchSizeSpinner = spinnerSize;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.bench_size_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        benchSizeSpinner.setAdapter(adapter);


        // Initialize the DrawerLayout and NavigationView
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Set toolbar as ActionBar

        // Set up NavigationItemSelectedListener for NavigationView
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_add_bench) {
                // Navigate to AddBenchActivity (it's already this activity)
                Toast.makeText(AddBenchActivity.this, "Already in Add Bench", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_find_bench) {
                // Start FindBenchActivity (or another activity)
                Intent intent = new Intent(AddBenchActivity.this, HomeActivity.class);
                startActivity(intent);
            }

            // Close the navigation drawer after selecting an item
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });


    } // Closing the onCreate method


    // Method to simulate selecting a location
    private void selectLocation() {
        // For now, we simulate a location (this will later be replaced with actual GeoPoint selection)
        lat = 40.748817;  // Dummy Latitude (e.g., New York)
        lng = -73.985428; // Dummy Longitude
        Toast.makeText(this, "Location set: Latitude = " + lat + ", Longitude = " + lng, Toast.LENGTH_SHORT).show();
    }

    // Method to allow users to upload an image
//    private void uploadImage() {
//        // Intent to open image gallery (you can use an image picker library)
//        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.setType("image/*");
//        startActivityForResult(intent, 100);
//    }

    // Handle the result of the image picker
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
//            imageUri = data.getData();
//            imagePreview.setImageURI(imageUri);  // Display the selected image
//        }
//    }

    // Method to submit the bench data to Firestore
    private void submitBench() {
        String benchName = etBenchName.getText().toString().trim();
        Float rating = ratingBar.getRating();
        Boolean isShaded = cbShade.isChecked();
        Boolean quietStreet = cbQuietStreet.isChecked();
        Boolean nearCafe = cbNearCafe.isChecked();
        String size = spinnerSize.getSelectedItem().toString();

        if (benchName.isEmpty() || lat == 0.0 || lng == 0.0) {
            Toast.makeText(this, "Please fill in the required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        GeoPoint location = new GeoPoint(lat, lng);

        // Create a Bench object to save to Firestore
        Bench bench = new Bench(benchName, location, rating, isShaded, quietStreet, nearCafe, size, null);

//        // If there is an image, upload it to Firebase Storage and get the URL
//        if (imageUri != null) {
//            uploadImageToStorage(imageUri, bench);
//        } else {
//            saveBenchToFirestore(bench);
//        }
    }

    // Method to upload image to Firebase Storage
//    private void uploadImageToStorage(Uri imageUri, Bench bench) {
//        // Create a reference to the storage location
//        StorageReference imageRef = storage.getReference().child("bench_images/" + System.currentTimeMillis() + ".jpg");
//
//        // Convert the image to a byte array
//        imagePreview.setDrawingCacheEnabled(true);
//        imagePreview.buildDrawingCache();
//        Bitmap bitmap = imagePreview.getDrawingCache();
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//        byte[] imageBytes = baos.toByteArray();
//
//        // Upload the image
//        UploadTask uploadTask = imageRef.putBytes(imageBytes);
//        uploadTask.addOnSuccessListener(taskSnapshot -> {
//            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
//                // Get the image URL and update the Bench object
//                bench.setImageUrl(uri.toString());
//                saveBenchToFirestore(bench);
//            });
//        }).addOnFailureListener(e -> {
//            Toast.makeText(AddBenchActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        });
//    }

    // Method to save the bench data to Firestore
    private void saveBenchToFirestore(Bench bench) {
        db.collection("benches")
                .add(bench)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddBenchActivity.this, "Bench added successfully!", Toast.LENGTH_SHORT).show();
                    finish();  // Go back to the previous screen
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddBenchActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
