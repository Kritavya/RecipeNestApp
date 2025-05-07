package com.kritavya.recipenest;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.WindowCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kritavya.recipenest.adapters.CreateRecipeAdapter;
import com.kritavya.recipenest.models.Recipe;
import com.kritavya.recipenest.utils.CloudinaryHelper;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CreateRecipeActivity extends AppCompatActivity implements CreateRecipeAdapter.RecipeDataListener {

    private static final String TAG = "CreateRecipeActivity";
    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private static final int REQUEST_CAMERA_PERMISSION = 101;

    private ViewPager2 viewPager;
    private LinearLayout navHome;
    private ImageView[] stepDots;
    private CreateRecipeAdapter adapter;
    private int[] layouts = new int[]{
            R.layout.recipe_step1_basic_info,
            R.layout.recipe_step2_ingredients,
            R.layout.recipe_step3_instructions,
            R.layout.recipe_step4_additional_info,
            R.layout.recipe_step5_review
    };

    // Recipe data
    private Recipe currentRecipe;
    
    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    
    // Image upload
    private List<Uri> selectedImageUris = new ArrayList<>();
    private ProgressDialog progressDialog;
    private int uploadedImageCount = 0;
    
    // Activity result launchers
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> cropLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Configure window to handle system insets
        configureSystemUi();
        
        setContentView(R.layout.activity_create_recipe);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        
        // Initialize Cloudinary
        CloudinaryHelper.initCloudinary(this);
        
        // Initialize recipe model
        currentRecipe = new Recipe();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentRecipe.setUserId(currentUser.getUid());
            currentRecipe.setUserName(currentUser.getDisplayName());
            currentRecipe.setUserPhotoUrl(currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : null);
        }
        
        // Setup activity result launchers
        setupResultLaunchers();
        
        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading");
        progressDialog.setMessage("Please wait while we upload your recipe images...");
        progressDialog.setCancelable(false);

        // Initialize views
        viewPager = findViewById(R.id.viewPager);
        navHome = findViewById(R.id.nav_home);
        
        // Set up navbar clicks
        setupBottomNavigation();
        
        // Set up step indicators
        setupStepIndicators();
        
        // Set up the adapter
        adapter = new CreateRecipeAdapter(this, layouts);
        adapter.setDataListener(this);
        adapter.setRecipe(currentRecipe);
        viewPager.setAdapter(adapter);
        adapter.setViewPager(viewPager);
        viewPager.setUserInputEnabled(false); // Disable swiping
        
        // Set up page change callback
        setupPageChangeCallback();
        
        // Initialize the UI for first page
        updateUI(0);
    }
    
    private void setupResultLaunchers() {
        // Gallery picker launcher
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        // Start cropping
                        startCropActivity(selectedImageUri);
                    }
                }
            });
        
        // Camera launcher
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Get the image from the path where we saved it
                    File imageFile = new File(currentPhotoPath);
                    Uri imageUri = Uri.fromFile(imageFile);
                    // Start cropping
                    startCropActivity(imageUri);
                }
            });
        
        // Crop launcher
        cropLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri croppedImageUri = UCrop.getOutput(result.getData());
                    if (croppedImageUri != null) {
                        handleCroppedImage(croppedImageUri);
                    }
                } else if (result.getResultCode() == UCrop.RESULT_ERROR) {
                    Throwable error = UCrop.getError(result.getData());
                    Toast.makeText(this, "Error cropping image: " + error, Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void configureSystemUi() {
        // Make system bars (status and navigation) light
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        // Set status bar color to light background
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        
        // Make status bar icons dark (for light background)
        View decorView = getWindow().getDecorView();
        int flags = decorView.getSystemUiVisibility();
        flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        decorView.setSystemUiVisibility(flags);
        
        // Ensure status bar is visible
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    
    private void setupBottomNavigation() {
        navHome.setOnClickListener(v -> {
            finish(); // Go back to home
        });
    }
    
    private void setupStepIndicators() {
        View dotsLayout = findViewById(R.id.dotsLayout);
        stepDots = new ImageView[layouts.length];
        
        for (int i = 0; i < stepDots.length; i++) {
            stepDots[i] = new ImageView(this);
            stepDots[i].setImageResource(i == 0 ? R.drawable.dot_active : R.drawable.dot_inactive);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            ((LinearLayout) dotsLayout).addView(stepDots[i], params);
        }
    }
    
    private void setupPageChangeCallback() {
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateUI(position);
            }
        });
    }
    
    private void updateUI(int position) {
        // Update step indicator dots
        for (int i = 0; i < stepDots.length; i++) {
            stepDots[i].setImageResource(i <= position ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }
    
    public void goToNextStep() {
        int currentItem = viewPager.getCurrentItem();
        if (currentItem < layouts.length - 1) {
            // Save current step data
            adapter.saveCurrentStepData(currentItem);
            
            // Move to next step
            viewPager.setCurrentItem(currentItem + 1);
        } else {
            finishRecipeCreation();
        }
    }
    
    public void goToPreviousStep() {
        int currentItem = viewPager.getCurrentItem();
        if (currentItem > 0) {
            // Save current step data
            adapter.saveCurrentStepData(currentItem);
            
            // Move to previous step
            viewPager.setCurrentItem(currentItem - 1);
        } else {
            finish();
        }
    }
    
    public void finishRecipeCreation() {
        // Save the last step data
        adapter.saveCurrentStepData(viewPager.getCurrentItem());
        
        // Check if we have all required data
        if (!validateRecipe()) {
            return;
        }
        
        // Start uploading images
        if (!selectedImageUris.isEmpty()) {
            uploadImagesToCloudinary();
        } else {
            // No images to upload, just save the recipe
            saveRecipeToFirebase();
        }
    }
    
    private boolean validateRecipe() {
        if (currentRecipe.getName() == null || currentRecipe.getName().trim().isEmpty()) {
            Toast.makeText(this, "Please enter a recipe name", Toast.LENGTH_SHORT).show();
            viewPager.setCurrentItem(0);
            return false;
        }
        
        if (selectedImageUris.isEmpty()) {
            Toast.makeText(this, "Please select at least one image", Toast.LENGTH_SHORT).show();
            viewPager.setCurrentItem(0);
            return false;
        }
        
        if (currentRecipe.getIngredients().isEmpty()) {
            Toast.makeText(this, "Please add at least one ingredient", Toast.LENGTH_SHORT).show();
            viewPager.setCurrentItem(1);
            return false;
        }
        
        if (currentRecipe.getInstructions().isEmpty()) {
            Toast.makeText(this, "Please add at least one instruction", Toast.LENGTH_SHORT).show();
            viewPager.setCurrentItem(2);
            return false;
        }
        
        return true;
    }
    
    private void uploadImagesToCloudinary() {
        progressDialog.show();
        uploadedImageCount = 0;
        
        for (Uri imageUri : selectedImageUris) {
            CloudinaryHelper.uploadImage(this, imageUri, "recipe_images", new CloudinaryHelper.CloudinaryUploadCallback() {
                @Override
                public void onUploadStarted() {
                    // Update progress dialog
                    progressDialog.setMessage("Uploading image " + (uploadedImageCount + 1) + " of " + selectedImageUris.size());
                }

                @Override
                public void onProgressUpdate(double progress) {
                    // Update progress dialog
                    int percent = (int) (progress * 100);
                    progressDialog.setMessage("Uploading image " + (uploadedImageCount + 1) + " of " + selectedImageUris.size() + " (" + percent + "%)");
                }

                @Override
                public void onSuccess(String url, String publicId) {
                    // Add image URL to recipe
                    currentRecipe.addImageUrl(url);
                    uploadedImageCount++;
                    
                    // Check if all images are uploaded
                    if (uploadedImageCount == selectedImageUris.size()) {
                        // All images uploaded, save recipe to Firebase
                        saveRecipeToFirebase();
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    // Handle error
                    progressDialog.dismiss();
                    String userMessage = "Error uploading image: ";
                    
                    if (errorMessage.contains("401")) {
                        userMessage += "Authentication failed. Please check Cloudinary credentials.";
                        Log.e(TAG, "Cloudinary auth error: " + errorMessage);
                    } else if (errorMessage.contains("network")) {
                        userMessage += "Network error. Please check your internet connection.";
                    } else {
                        userMessage += errorMessage;
                    }
                    
                    Toast.makeText(CreateRecipeActivity.this, userMessage, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error uploading image: " + errorMessage);
                }
            });
        }
    }
    
    private void saveRecipeToFirebase() {
        progressDialog.setMessage("Saving recipe...");
        
        // Generate a unique ID for the recipe
        String recipeId = mDatabase.child("recipes").push().getKey();
        currentRecipe.setId(recipeId);
        
        // Set creation and update timestamps
        currentRecipe.setCreatedAt(new Date());
        currentRecipe.setUpdatedAt(new Date());
        
        // Save recipe to Firebase
        mDatabase.child("recipes").child(recipeId).setValue(currentRecipe)
            .addOnSuccessListener(aVoid -> {
                // Recipe saved successfully
                progressDialog.dismiss();
                Toast.makeText(CreateRecipeActivity.this, "Recipe saved successfully!", Toast.LENGTH_SHORT).show();
                
                // Also save to user's recipes
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    mDatabase.child("users").child(user.getUid()).child("recipes").child(recipeId).setValue(true);
                }
                
                // Go back to home or recipe detail
                Intent intent = new Intent(CreateRecipeActivity.this, RecipeDetailActivity.class);
                intent.putExtra("recipe_id", recipeId);
                startActivity(intent);
                finish();
            })
            .addOnFailureListener(e -> {
                // Failed to save recipe
                progressDialog.dismiss();
                Toast.makeText(CreateRecipeActivity.this, "Error saving recipe: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error saving recipe: " + e.getMessage());
            });
    }
    
    public void pickImage() {
        // Create dialog for image source selection
        String[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) { // Take Photo
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                } else {
                    launchCamera();
                }
            } else if (which == 1) { // Choose from Gallery
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // For Android 13+ use READ_MEDIA_IMAGES
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_STORAGE_PERMISSION);
                    } else {
                        launchGallery();
                    }
                } else {
                    // For older Android versions use READ_EXTERNAL_STORAGE
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
                    } else {
                        launchGallery();
                    }
                }
            } else {
                dialog.dismiss();
            }
        });
        builder.show();
    }
    
    private void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a file for the image
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Continue if file was created successfully
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.kritavya.recipenest.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            cameraLauncher.launch(takePictureIntent);
        }
    }
    
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        
        // Save a file path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    
    private void launchGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }
    
    private void startCropActivity(Uri sourceUri) {
        String destinationFileName = UUID.randomUUID().toString() + ".jpg";
        
        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(80);
        options.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        options.setToolbarTitle("Crop Image");
        
        // Start crop activity
        UCrop uCrop = UCrop.of(sourceUri, Uri.fromFile(new File(getCacheDir(), destinationFileName)))
                .withAspectRatio(16, 9)
                .withOptions(options);
                
        cropLauncher.launch(uCrop.getIntent(this));
    }
    
    private void handleCroppedImage(Uri croppedImageUri) {
        // Add to selected images
        selectedImageUris.add(croppedImageUri);
        
        // Update UI
        adapter.updateImagePreview(croppedImageUri);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchGallery();
            } else {
                Toast.makeText(this, "Permission denied. Cannot access gallery images.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Toast.makeText(this, "Permission denied. Cannot take photos.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    public void onRecipeDataChanged(Recipe recipe) {
        this.currentRecipe = recipe;
    }
    
    @Override
    public void onImagePickRequested() {
        pickImage();
    }

    public List<Uri> getSelectedImageUris() {
        return selectedImageUris;
    }
} 