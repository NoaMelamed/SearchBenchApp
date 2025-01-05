package com.example.noaandroid;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
    private Button btnSelectLocation, btnUploadImage, btnSubmit, btn_camera;
    private ImageView imagePreview, iv_photo;
    private Uri imageUri = null; // URI for selected image
    private double lat = 0.0; // Dummy Latitude
    private double lng = 0.0; // Dummy Longitude
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    ActivityResultLauncher<Intent> arSmall;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bench);
        initilizeViews();
        initSpinner();
        initNavigation();
        initCamera();

        // Set listeners
        btnSelectLocation.setOnClickListener(v -> selectLocation());
        btnSubmit.setOnClickListener(v -> submitBench());

    }


    // Method to simulate selecting a location
    private void selectLocation() {
        requestLocationPermission();
    }

    // Method to allow users to upload an image
    private void uploadImage() {
        // Intent to open image gallery (you can use an image picker library)
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    // Handle the result of the image picker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imagePreview.setImageURI(imageUri);  // Display the selected image
        }
    }

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

        String imageUriString = (imageUri != null) ? imageUri.toString() : null;
        // Create a Bench object to save to Firestore
        Bench bench = new Bench(benchName, location, rating, isShaded, quietStreet, nearCafe, size, imageUriString);

        // If there is an image, upload it to Firebase Storage and get the URL
        if (imageUri != null) {
            uploadImageToStorage(imageUri, bench);
        } else {
            saveBenchToFirestore(bench);
        }
    }

    // Method to upload image to Firebase Storage
    private void uploadImageToStorage(Uri imageUri, Bench bench) {
        // Create a reference to the storage location
        StorageReference imageRef = storage.getReference().child("bench_images/" + System.currentTimeMillis() + ".jpg");

        // Convert the image to a byte array
        imagePreview.setDrawingCacheEnabled(true);
        imagePreview.buildDrawingCache();
        Bitmap bitmap = imagePreview.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();

        // Upload the image
        UploadTask uploadTask = imageRef.putBytes(imageBytes);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Get the image URL and update the Bench object
                bench.setImageUri(uri.toString());
                saveBenchToFirestore(bench);
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(AddBenchActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void getRealLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                        Toast.makeText(this, "Location set: Latitude = " + lat + ", Longitude = " + lng, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Unable to get location. Make sure location is enabled.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void requestLocationPermission() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getRealLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            switch (requestCode) {
                case LOCATION_PERMISSION_REQUEST_CODE:
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        getRealLocation(); // Call your method to get the location
                    } else {
                        Toast.makeText(this, "Location permission is required to get your current location.", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case CAMERA_PERMISSION_REQUEST_CODE:
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                    }
                    break;

                default:
                    // Handle any other cases if needed
                    break;
            }
        }

    public void initilizeViews () {

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
        btnUploadImage = findViewById(R.id.btn_upload_image);
        btnSubmit = findViewById(R.id.btn_submit);
        imagePreview = findViewById(R.id.image_preview);

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
                Intent intent = new Intent(AddBenchActivity.this, AddBenchActivity.class);
                startActivity(intent);
            } else if (item.getItemId() == R.id.nav_find_bench) {
                Intent intent = new Intent(AddBenchActivity.this, HomeActivity.class);
                startActivity(intent);
            }
            return true;
        });
    }

    public void initSpinner() {
        Spinner benchSizeSpinner = spinnerSize;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.bench_size_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        benchSizeSpinner.setAdapter(adapter);
    }

    public void initCamera () {
        // Handle camera permissions and functionality
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE},
                1);
        btn_camera = findViewById(R.id.btn_camera);
        iv_photo = findViewById(R.id.iv_photo);

        arSmall = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getExtras() != null) {
                        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                        iv_photo.setImageBitmap(bitmap);
                    }
                }
            }
        });

        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                arSmall.launch(intent);
            }
        });
    }

}
