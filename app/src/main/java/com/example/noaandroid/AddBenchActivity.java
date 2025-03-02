package com.example.noaandroid;

import static com.example.noaandroid.Bench.toHashMap;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;




import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AddBenchActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 101;
    private static final int GALLERY_REQUEST_CODE = 102;

    private EditText etBenchName;
    private RatingBar ratingBar;
    private CheckBox cbShade, cbQuietStreet, cbNearCafe;
    private Spinner spinnerSize;
    private Button btnSelectLocation, btnUploadImage, btnSubmit, btnCamera;
    private ImageView imagePreview, ivPhoto;
    private Uri imageUri = null; // URI for selected image
    private double lat = 0.0; // Dummy Latitude
    private double lng = 0.0; // Dummy Longitude
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private List<Uri> selectedImages = new ArrayList<>(); // List to hold selected image URIs


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bench);

        initViews();
        initSpinner();
        initNavigation();
        initListeners();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();

        // Set listeners


    }

    private void initListeners() {
        btnCamera.setOnClickListener(v -> openCamera());
        btnUploadImage.setOnClickListener(v -> openGallery());
        btnSubmit.setOnClickListener(v -> submitBench());
        btnSelectLocation.setOnClickListener(v -> selectLocation());
    }
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    private void selectLocation() {
        // Check for location permission
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            // If permission is already granted, get the location
            getLocation();
        }
    }
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            Toast.makeText(this, "Location permission not granted. Please allow it in settings.", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        lat = location.getLatitude();
                        lng = location.getLongitude();

                        // Update the button text to reflect the selected location
                        btnSelectLocation.setText("Location Selected: Lat " + lat + ", Lng " + lng);

                        // Optionally, enable other actions or save the location for submission
                        btnSubmit.setEnabled(true); // Enable the submit button
                    } else {
                        Toast.makeText(AddBenchActivity.this, "Failed to retrieve location. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddBenchActivity.this, "Error retrieving location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



    private void submitBench() {
        // Collect data
        String benchName = etBenchName.getText().toString().trim();
        if (benchName.isEmpty()) {
            etBenchName.setError("Please enter the bench name");
            return;
        }

        // Validate the location
        if (lat == 0.0 && lng == 0.0) {
            Toast.makeText(this, "Please select a location.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate image selection
        if (selectedImages.isEmpty()) {
            Toast.makeText(this, "Please select at least one image.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Proceed to upload and store bench data
        float rating = ratingBar.getRating();
        boolean hasShade = cbShade.isChecked();
        boolean isQuietStreet = cbQuietStreet.isChecked();
        boolean isNearCafe = cbNearCafe.isChecked();
        String size = spinnerSize.getSelectedItem().toString();
        GeoPoint benchLocation = new GeoPoint(lat, lng);

        // Create an ArrayList to hold the rating
        List<Float> ratings = new ArrayList<>();
        ratings.add(rating); // Add the current rating to the list

        // Show progress dialog while uploading
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading bench...");
        progressDialog.show();

        // Upload images and create bench data
        uploadImagesAndCreateBench(benchName, benchLocation, hasShade, isQuietStreet, isNearCafe, size, rating);

        // Dismiss the progress dialog and show success toast once the upload finishes
        progressDialog.dismiss();
        Toast.makeText(this, "Bench uploaded successfully", Toast.LENGTH_SHORT).show();

        // Navigate to the home activity
        Intent intent = new Intent(AddBenchActivity.this, HomeActivity.class);
        startActivity(intent);
    }




    private void uploadImagesAndCreateBench(String benchName, GeoPoint benchLocation, boolean hasShade, boolean isQuietStreet, boolean isNearCafe, String size, float rating) {
        List<String> imageUrls = new ArrayList<>();
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading images...");
        progressDialog.show();

        uploadImagesToStorage(selectedImages, imageUrls, progressDialog, benchName, benchLocation, hasShade, isQuietStreet, isNearCafe, size, rating);
    }


    private void uploadToFirestore(Map<String, Object> benchMap, Bench bench) {
        db.collection("benches")
                .add(benchMap)
                .addOnSuccessListener(documentReference -> {
                    String benchDocId = documentReference.getId(); // Get Firestore-generated ID

                    // Update the Firestore document with its own ID
                    documentReference.update("benchId", benchDocId)
                            .addOnSuccessListener(aVoid ->
                                    Log.d("Firestore", "Bench ID successfully updated in Firestore")

                            )
                            .addOnFailureListener(e ->
                                    Log.e("Firestore", "Failed to update Bench ID", e)
                            );

                    // update local items with firebase id
                    benchMap.put("benchId", benchDocId);
                    bench.setBenchId(benchDocId);


                })
                .addOnFailureListener(e ->
                        Log.e("Firestore", "Error adding bench", e)
                );
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST_CODE && data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        selectedImages.add(imageUri);
                    }
                } else {
                    selectedImages.add(data.getData());
                }
            }
        }
    }


    private void uploadImagesToStorage(List<Uri> images, List<String> imageUrls, ProgressDialog progressDialog, String benchName, GeoPoint benchLocation, boolean hasShade, boolean isQuietStreet, boolean isNearCafe, String size, float rating) {
        AtomicInteger index = new AtomicInteger(0);  // Use AtomicInteger for the index
        for (Uri imageUri : images) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference imageRef = storageRef.child("bench_images/" + System.currentTimeMillis() + "_" + index.get() + ".jpg");

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                imageUrls.add(uri.toString());
                                if (index.incrementAndGet() == images.size()) {
                                    createAndUploadBench(benchName, benchLocation, hasShade, isQuietStreet, isNearCafe, size, imageUrls, rating);
                                    progressDialog.dismiss();
                                }
                            }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    });
        }
    }



    private void createAndUploadBench(final String benchName, final GeoPoint benchLocation, final boolean hasShade, final boolean isQuietStreet, final boolean isNearCafe, final String size, final List<String> imageUrls, final float rating) {
        List<Float> ratings = new ArrayList<>();
        ratings.add(rating);
        Bench bench = new Bench(benchName, benchLocation, hasShade, isQuietStreet, isNearCafe, size, imageUrls, ratings, null); // averageRating is updated in setRating
        Map<String, Object> benchMap = Bench.toHashMap(bench);
        uploadToFirestore(benchMap, bench);
    }



    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "BenchImage", null);
        return Uri.parse(path);
    }


    // Helper method to check if a file URI is valid
    private boolean isFileUriValid(Uri uri) {
        if (uri == null) {
            return false;
        }

        String scheme = uri.getScheme();
        return scheme != null && scheme.equals("file");
    }


    private void initViews() {

        etBenchName = findViewById(R.id.et_bench_name);
        ratingBar = findViewById(R.id.rating_bar);
        cbShade = findViewById(R.id.cb_shade);
        cbQuietStreet = findViewById(R.id.cb_quiet_street);
        cbNearCafe = findViewById(R.id.cb_near_cafe);
        spinnerSize = findViewById(R.id.sp_size);
        btnSelectLocation = findViewById(R.id.btn_select_location);
        btnUploadImage = findViewById(R.id.btn_upload_image);
        btnSubmit = findViewById(R.id.btn_submit);
        btnCamera = findViewById(R.id.btn_camera);
        ivPhoto = findViewById(R.id.iv_photo);
    }

    private void initSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.bench_size_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSize.setAdapter(adapter);
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
            int id = item.getItemId(); // Get the selected item's ID
            if (id == R.id.nav_add_bench) {
                Intent intent = new Intent(AddBenchActivity.this, AddBenchActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_find_bench) {
                Intent intent = new Intent(AddBenchActivity.this, HomeActivity.class);
                startActivity(intent);
            }
            drawerLayout.closeDrawer(GravityCompat.START); // Close the navigation drawer
            return true;
        });
    }




}
