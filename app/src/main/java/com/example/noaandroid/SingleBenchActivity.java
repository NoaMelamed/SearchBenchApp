package com.example.noaandroid;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;



public class SingleBenchActivity extends AppCompatActivity implements OnMapReadyCallback, RatingFragment.OnRatingSubmittedListener {

    private TextView benchTitle;
    private Button viewGalleryButton, rateBenchButton;
    Button submitButton;

    private RatingBar ratingBar;
    private ImageView benchImage;
    private GoogleMap mMap;
    private Bench bench;
    private String benchId;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 200;
    private List<Uri> selectedImages = new ArrayList<>();
    private String imageUri;
    private Uri newImageUri;
    private ProgressDialog progressDialog;
    private FirebaseStorage storage;
    private StorageReference storageRef;


    /**
     * Called when the activity is created. Initializes views, listeners, and navigation.
     * Retrieves the bench ID from the intent and loads bench details if the ID is found.
     *
     * @param savedInstanceState The saved instance state (if any).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_bench);

        initViews();
        initListeners();
        initNavigation();

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Get benchId from the intent
        benchId = getIntent().getStringExtra("benchDocId");

        if (benchId != null) {
            loadBenchDetails(benchId); // Load details if ID exists
        } else {
            Toast.makeText(this, "Error: Bench ID not found", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if no ID is provided
        }

        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this); // Load map asynchronously
        }
    }

    /**
     * Initializes view elements by finding them from the layout.
     */
    private void initViews() {
        benchTitle = findViewById(R.id.bench_title);
        viewGalleryButton = findViewById(R.id.view_gallery);
        rateBenchButton = findViewById(R.id.rate_bench);
        ratingBar = findViewById(R.id.rating_bar);
        benchImage = findViewById(R.id.bench_image);
        submitButton = findViewById(R.id.submit_button);
    }

    /**
     * Loads the bench details from Firestore using the provided benchId.
     * Updates the UI with the bench data if successfully fetched, otherwise shows an error.
     *
     * @param benchId The unique identifier of the bench.
     */
    private void loadBenchDetails(String benchId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference benchRef = db.collection("benches").document(benchId);

        benchRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                bench = documentSnapshot.toObject(Bench.class);
                if (bench != null) {
                    populateBenchDetails(); // Populate details if bench object is available
                }
            } else {
                Toast.makeText(SingleBenchActivity.this, "Bench not found", Toast.LENGTH_SHORT).show();
                finish(); // Close activity if bench does not exist
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(SingleBenchActivity.this, "Error loading bench", Toast.LENGTH_SHORT).show();
            Log.e("Firestore", "Error fetching bench data", e);
        });
    }

    /**
     * Populates the UI with the loaded bench details such as title, rating, images, and map location.
     */
    private void populateBenchDetails() {
        benchTitle.setText(bench.getName());
        ratingBar.setRating((float) bench.getAverageRating());

        updateFilterIcons();

        // Load first image if available
        if (bench.getImageUri() != null && !bench.getImageUri().isEmpty()) {
            String firstImageUrl = bench.getImageUri().get(0);
            Glide.with(this).load(firstImageUrl).into(benchImage); // Load image into ImageView
        } else {
            benchImage.setImageResource(R.drawable.new_logo_icon_only); // Set placeholder if no images
        }

        // Add marker to map if location is available
        if (mMap != null && bench.getLocation() != null) {
            LatLng benchLocation = new LatLng(bench.getLocation().getLatitude(), bench.getLocation().getLongitude());
            mMap.addMarker(new MarkerOptions().position(benchLocation).title(bench.getName()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(benchLocation, 15)); // Zoom in on bench location
        }
    }

    /**
     * Updates the icons representing features such as shade, noise, and cafe.
     */
    private void updateFilterIcons() {
        ImageView shadedIcon = findViewById(R.id.shaded_icon);
        ImageView noiseIcon = findViewById(R.id.noise_icon);
        ImageView cafeIcon = findViewById(R.id.cafe_icon);

        updateIconStatus(shadedIcon, bench.getIsShaded());
        updateIconStatus(noiseIcon, bench.getQuietStreet());
        updateIconStatus(cafeIcon, bench.getNearCafe());


    }

    /**
     * Updates the individual icon's status based on the value of isChecked.
     */
    private void updateIconStatus(ImageView iconView, boolean isChecked) {
        if (isChecked) {
            iconView.setImageResource(R.drawable.ic_check); // Set check icon if checked
        } else {
            iconView.setImageResource(R.drawable.ic_cross); // Set cross icon if unchecked
        }
    }

    /**
     * Initializes listeners for button clicks.
     * Includes listeners for viewing the gallery, rating the bench, opening the camera, and submitting images.
     */
    private void initListeners() {
        viewGalleryButton.setOnClickListener(v -> showGalleryFragment()); // Show image gallery
        rateBenchButton.setOnClickListener(v -> showRatingFragment()); // Show rating fragment
        Button cameraButton = findViewById(R.id.camera_button);
        Button galleryButton = findViewById(R.id.gallery_button);

        cameraButton.setOnClickListener(v -> openCamera()); // Open camera
        galleryButton.setOnClickListener(v -> openGallery()); // Open gallery
        submitButton.setOnClickListener(v -> {
            submitImage(); // Submit selected image
        });
    }

    /**
     * Displays the gallery fragment with images if available.
     */
    private void showGalleryFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment existingFragment = fragmentManager.findFragmentByTag("GalleryFragment");

        if (existingFragment != null) {
            fragmentManager.beginTransaction().remove(existingFragment).commit(); // Ensure previous fragment is removed
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

    /**
     * Shows the rating fragment where users can submit a rating.
     */
    private void showRatingFragment() {
        RatingFragment ratingFragment = new RatingFragment();
        ratingFragment.setOnRatingSubmittedListener(this); // Set listener to handle rating submission
        ratingFragment.show(getSupportFragmentManager(), "ratingFragment");
    }

    /**
     * Handles the rating submission from the rating fragment.
     *
     * @param rating The rating value submitted by the user.
     */
    public void onRatingSubmitted(float rating) {
        updateUserRating(benchId, rating); // Update the rating in the database
    }

    /**
     * Updates the user's rating in Firestore and refreshes the bench details.
     *
     * @param benchId The unique identifier of the bench.
     * @param newRating The new rating submitted by the user.
     */
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

            bench.addRating(newRating); // updates both, the ratings (list) and their average
            transaction.set(benchRef, bench); // Use set to update the entire document
            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(SingleBenchActivity.this, "Rating updated!", Toast.LENGTH_SHORT).show();
            // Refresh UI, this will trigger a recalculation of the average in the Bench class
            loadBenchDetails(benchId); // Reload the bench details after rating update
        }).addOnFailureListener(e -> {
            Toast.makeText(SingleBenchActivity.this, "Error updating rating", Toast.LENGTH_SHORT).show();
            Log.e("Firestore", "Error updating rating", e);
        });
    }
    /**
     * Called when the map is ready to be used.
     * This method sets up the map and places a marker on it at the bench's location.
     *
     * @param googleMap The GoogleMap object to be used for displaying the map.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (bench != null && bench.getLocation() != null) {
            LatLng benchLocation = new LatLng(bench.getLocation().getLatitude(), bench.getLocation().getLongitude());
            mMap.addMarker(new MarkerOptions().position(benchLocation).title(bench.getName()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(benchLocation, 15));
        }
    }

    /**
     * Initializes the navigation drawer and toolbar for the activity.
     * Sets up toolbar buttons and navigation item selections.
     */
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
                startActivity(intent);
            }
            return true;
        });
    }

    /**
     * Opens the camera to take a photo.
     * Starts an activity to capture an image from the camera.
     */
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    /**
     * Opens the gallery to allow the user to select one or more images.
     * Starts an activity to choose images from the device's gallery.
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    /**
     * Handles the result of the camera or gallery activity.
     * Retrieves selected images and adds them to the selectedImages list.
     *
     * @param requestCode The request code passed in startActivityForResult().
     * @param resultCode  The result code from the activity.
     * @param data        The data returned from the activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE && data != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                newImageUri = getImageUri(photo); // Convert Bitmap to URI
                selectedImages.add(newImageUri); // Add to list
            } else if (requestCode == GALLERY_REQUEST_CODE && data != null) {
                if (data.getClipData() != null) {
                    // If multiple images are selected
                    for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                        Uri newImageUri = data.getClipData().getItemAt(i).getUri();
                        selectedImages.add(newImageUri); // Add to list
                    }
                } else {
                    // If a single image is selected
                    newImageUri = data.getData();
                    selectedImages.add(newImageUri); // Add to list
                }
            }
        }
    }

    /**
     * Converts a Bitmap object into a URI that can be used for saving or sharing the image.
     *
     * @param bitmap The Bitmap object to convert.
     * @return The URI pointing to the saved image.
     */
    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "BenchImage", null);
        return Uri.parse(path);
    }

    /**
     * Submits the selected images for upload to Firebase Storage.
     * If no images are selected, it shows a toast. Otherwise, it starts the upload process.
     */
    private void submitImage() {
        if (selectedImages.isEmpty()) {
            Toast.makeText(this, "Please select an image.", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading images...");
        progressDialog.show();

        // Upload selected images to Firebase Storage
        uploadImagesToStorage(selectedImages, progressDialog);
    }

    /**
     * Uploads the selected images to Firebase Storage and retrieves their download URLs.
     * Once all images are uploaded, it calls saveImagesToBench() to store the URLs.
     *
     * @param images        The list of image URIs to upload.
     * @param progressDialog The ProgressDialog used to show upload status.
     */
    private void uploadImagesToStorage(List<Uri> images, ProgressDialog progressDialog) {
        List<String> imageUrls = new ArrayList<>();
        AtomicInteger uploadCount = new AtomicInteger(0);

        for (Uri imageUri : images) {
            StorageReference imageRef = storageRef.child("bench_images/" + System.currentTimeMillis() + "_" + uploadCount.getAndIncrement() + ".jpg");

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            imageUrls.add(uri.toString());

                            if (imageUrls.size() == images.size()) {
                                saveImagesToBench(imageUrls, progressDialog); // Save image URLs to Firestore
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(SingleBenchActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /**
     * Saves the newly uploaded image URLs to the Firestore database for the specified bench.
     * Updates the bench document with the new image URLs.
     *
     * @param newImageUrls   The list of new image URLs to save.
     * @param progressDialog The ProgressDialog used to show upload status.
     */
    private void saveImagesToBench(List<String> newImageUrls, ProgressDialog progressDialog) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference benchRef = db.collection("benches").document(benchId);

        benchRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Bench bench = documentSnapshot.toObject(Bench.class);
                if (bench != null) {
                    List<String> existingImages = bench.getImageUri();
                    if (existingImages == null) {
                        existingImages = new ArrayList<>();
                    }
                    existingImages.addAll(newImageUrls); // Add new images to existing ones
                    benchRef.update("imageUri", existingImages)
                            .addOnSuccessListener(aVoid -> {
                                progressDialog.dismiss();
                                Toast.makeText(SingleBenchActivity.this, "Images uploaded and saved!", Toast.LENGTH_SHORT).show();
                                loadBenchDetails(benchId); // Reload bench details to reflect changes
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(SingleBenchActivity.this, "Failed to save image URLs", Toast.LENGTH_SHORT).show();
                            });
                }
            } else {
                progressDialog.dismiss();
                Toast.makeText(SingleBenchActivity.this, "Bench not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(SingleBenchActivity.this, "Error loading bench", Toast.LENGTH_SHORT).show();
        });
    }

}



