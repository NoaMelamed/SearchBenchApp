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


import com.google.android.material.navigation.NavigationView;

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


// imports...

public class AddBenchActivity extends AppCompatActivity {

    // Constants for camera and gallery request codes
    private static final int CAMERA_REQUEST_CODE = 101;
    private static final int GALLERY_REQUEST_CODE = 102;

    // UI Elements
    private EditText etBenchName;
    private RatingBar ratingBar;
    private CheckBox cbShade, cbQuietStreet, cbNearCafe;
    private Spinner spinnerSize;
    private Button btnSelectLocation, btnUploadImage, btnSubmit, btnCamera;
    private ImageView imagePreview, ivPhoto;

    // Data and Firebase
    private Uri imageUri = null;
    private double lat = 0.0;
    private double lng = 0.0;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private List<Uri> selectedImages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bench);

        initViews();      // Initialize UI components
        initSpinner();    // Initialize spinner options
        initNavigation(); // Setup navigation drawer
        initListeners();  // Set listeners for buttons

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Sets click listeners for buttons.
     */
    private void initListeners() {
        btnCamera.setOnClickListener(v -> openCamera());
        btnUploadImage.setOnClickListener(v -> openGallery());
        btnSubmit.setOnClickListener(v -> submitBench());
        btnSelectLocation.setOnClickListener(v -> selectLocation());
    }

    /**
     * Opens the camera app to capture an image.
     */
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    /**
     * Opens the gallery to select an image.
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    /**
     * Initiates the location selection by checking permissions.
     */
    private void selectLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            getLocation();
        }
    }

    /**
     * Retrieves the user's current location if permission is granted.
     */
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            Toast.makeText(this, "Location permission not granted. Please allow it in settings.", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                        btnSelectLocation.setText("Location Selected.");
                        btnSubmit.setEnabled(true);
                    } else {
                        Toast.makeText(this, "Failed to retrieve location. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error retrieving location: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Validates inputs and uploads a new bench to Firestore.
     */
    private void submitBench() {
        String benchName = etBenchName.getText().toString().trim();
        if (benchName.isEmpty()) {
            etBenchName.setError("Please enter the bench name");
            return;
        }

        if (lat == 0.0 && lng == 0.0) {
            Toast.makeText(this, "Please select a location.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImages.isEmpty()) {
            Toast.makeText(this, "Please select at least one image.", Toast.LENGTH_SHORT).show();
            return;
        }

        float rating = ratingBar.getRating();
        boolean hasShade = cbShade.isChecked();
        boolean isQuietStreet = cbQuietStreet.isChecked();
        boolean isNearCafe = cbNearCafe.isChecked();
        String size = spinnerSize.getSelectedItem().toString();
        GeoPoint benchLocation = new GeoPoint(lat, lng);

        List<Float> ratings = new ArrayList<>();
        ratings.add(rating);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading bench...");
        progressDialog.show();

        uploadImagesAndCreateBench(benchName, benchLocation, hasShade, isQuietStreet, isNearCafe, size, rating, rating);

        progressDialog.dismiss();
        Toast.makeText(this, "Bench uploaded successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(AddBenchActivity.this, HomeActivity.class);
        startActivity(intent);
    }

    /**
     * Uploads selected images to Firebase Storage and creates a bench entry.
     */
    private void uploadImagesAndCreateBench(String benchName, GeoPoint benchLocation, boolean hasShade, boolean isQuietStreet, boolean isNearCafe, String size, float rating, double averageRating) {
        List<String> imageUrls = new ArrayList<>();
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading images...");
        progressDialog.show();

        uploadImagesToStorage(selectedImages, imageUrls, progressDialog, benchName, benchLocation, hasShade, isQuietStreet, isNearCafe, size, rating, averageRating);
    }

    /**
     * Uploads the bench object to Firestore database.
     */
    private void uploadToFirestore(Map<String, Object> benchMap, Bench bench) {
        db.collection("benches")
                .add(benchMap)
                .addOnSuccessListener(documentReference -> {
                    String benchDocId = documentReference.getId();
                    documentReference.update("benchId", benchDocId)
                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "Bench ID successfully updated"))
                            .addOnFailureListener(e -> Log.e("Firestore", "Failed to update Bench ID", e));
                    benchMap.put("benchId", benchDocId);
                    bench.setBenchId(benchDocId);
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error adding bench", e));
    }


    /**
     * This method is called when an activity (such as the camera or gallery) finishes.
     * It processes the result of the image selection or capture.
     *
     * @param requestCode The request code used to identify the request.
     * @param resultCode The result code indicating the success or failure of the activity.
     * @param data The intent containing the result data, which may include an image.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE && data != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                ivPhoto.setImageBitmap(photo);
                imageUri = getImageUri(photo);
                selectedImages.add(imageUri);
            }
        }
    }

    /**
     * Uploads multiple images to Firebase Storage.
     */
    private void uploadImagesToStorage(List<Uri> images, List<String> imageUrls, ProgressDialog progressDialog, String benchName, GeoPoint benchLocation, boolean hasShade, boolean isQuietStreet, boolean isNearCafe, String size, float rating, double averageRating) {
        AtomicInteger index = new AtomicInteger(0);
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

    /**
     * Creates a bench object and uploads it to Firestore.
     */
    private void createAndUploadBench(final String benchName, final GeoPoint benchLocation, final boolean hasShade, final boolean isQuietStreet, final boolean isNearCafe, final String size, final List<String> imageUrls, final float rating) {
        Bench bench = new Bench(benchName, benchLocation, hasShade, isQuietStreet, isNearCafe, size, imageUrls, rating);
        Map<String, Object> benchMap = Bench.toHashMap(bench);
        uploadToFirestore(benchMap, bench);
    }

    /**
     * Converts a Bitmap to a URI.
     */
    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "BenchImage", null);
        return Uri.parse(path);
    }

    /**
     * Checks if the given file URI is valid.
     */
    private boolean isFileUriValid(Uri uri) {
        if (uri == null) return false;
        String scheme = uri.getScheme();
        return scheme != null && scheme.equals("file");
    }

    /**
     * Initializes the views.
     */
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

    /**
     * Initializes the size spinner.
     */
    private void initSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.bench_size_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSize.setAdapter(adapter);
    }

    /**
     * Initializes the navigation drawer.
     */
    private void initNavigation() {
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
            int id = item.getItemId();
            if (id == R.id.nav_add_bench) {
                startActivity(new Intent(AddBenchActivity.this, AddBenchActivity.class));
            } else if (id == R.id.nav_find_bench) {
                startActivity(new Intent(AddBenchActivity.this, HomeActivity.class));
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }
}
