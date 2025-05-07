package com.kritavya.recipenest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Patterns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    private EditText etFullName, etEmail, etPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private ImageView ivToggle;
    private boolean isPasswordVisible = false;
    
    // Loading and message views
    private View loadingOverlay;
    private TextView tvLoadingMessage;
    private View successMessage;
    private TextView tvSuccessMessage;
    private View errorMessage;
    private TextView tvErrorMessage;
    
    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        
        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initializing views
        etFullName = findViewById(R.id.et_fullname);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
        ivToggle = findViewById(R.id.iv_toggle);
        
        // Initialize loading and message views
        loadingOverlay = findViewById(R.id.loading_overlay);
        tvLoadingMessage = loadingOverlay.findViewById(R.id.tv_loading_message);
        successMessage = findViewById(R.id.success_message);
        tvSuccessMessage = findViewById(R.id.tv_success_message);
        errorMessage = findViewById(R.id.error_message);
        tvErrorMessage = findViewById(R.id.tv_error_message);


        ivToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // Hide Password
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    ivToggle.setImageResource(R.drawable.ic_eye_off);
                    isPasswordVisible = false;
                } else {
                    // Show Password
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    ivToggle.setImageResource(R.drawable.ic_eye_on);
                    isPasswordVisible = true;
                }
                etPassword.setSelection(etPassword.length());
            }
        });


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = etFullName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();


                if (fullName.isEmpty()) {
                    etFullName.setError("Full Name is required");
                    etFullName.requestFocus();
                } else if (email.isEmpty()) {
                    etEmail.setError("Email is required");
                    etEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.setError("Please enter a valid Email address");
                    etEmail.requestFocus();
                } else if (password.isEmpty()) {
                    etPassword.setError("Password is required");
                    etPassword.requestFocus();
                } else if (password.length() < 6) {
                    etPassword.setError("Password must be at least 6 characters");
                    etPassword.requestFocus();
                } else {
                    registerUser(fullName, email, password);
                }
            }
        });


        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        
        // Setup message click listeners to dismiss them
        successMessage.setOnClickListener(v -> hideMessage(successMessage));
        errorMessage.setOnClickListener(v -> hideMessage(errorMessage));
    }
    
    @Override
    public void onBackPressed() {
        // If loading is visible, cancel the registration process
        if (loadingOverlay.getVisibility() == View.VISIBLE) {
            hideLoading();
            disableForm(false);
            
            // Check if user was created but UI didn't update
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                // Sign out the user to prevent auto-login
                mAuth.signOut();
                showErrorMessage("Registration cancelled. User was signed out.");
            } else {
                showErrorMessage("Registration cancelled.");
            }
        } else {
            super.onBackPressed();
        }
    }
    
    private void registerUser(String fullName, String email, String password) {
        // Show loading overlay and disable form
        showLoading("Creating your account...");
        disableForm(true);
        
        // Add timeout handler to prevent indefinite loading
        final Handler timeoutHandler = new Handler();
        final Runnable timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if (loadingOverlay.getVisibility() == View.VISIBLE) {
                    hideLoading();
                    disableForm(false);
                    
                    // Check if user was created but UI didn't update
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null) {
                        // Sign out the user to prevent auto-login
                        mAuth.signOut();
                        showErrorMessage("Request timed out but account may have been created. Please try logging in.");
                    } else {
                        showErrorMessage("Request timed out. Please check your internet connection and try again.");
                    }
                }
            }
        };
        
        // Set a much shorter timeout (8 seconds)
        timeoutHandler.postDelayed(timeoutRunnable, 8000);
        
        // Add another handler to check if Firebase took too long
        final Handler firebaseCheckHandler = new Handler();
        final Runnable firebaseCheckRunnable = new Runnable() {
            @Override
            public void run() {
                // If we're still loading but user is already authenticated, proceed
                if (loadingOverlay.getVisibility() == View.VISIBLE && mAuth.getCurrentUser() != null) {
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    hideLoading();
                    showSuccessMessage("Registration successful!");
                    
                    // Proceed immediately since authentication is already done
                    proceedToNextScreen();
                }
            }
        };
        
        // Check much sooner - after just 2 seconds
        firebaseCheckHandler.postDelayed(firebaseCheckRunnable, 2000);
        
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Cancel timeout handler as we got a response
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        firebaseCheckHandler.removeCallbacks(firebaseCheckRunnable);
                        
                        if (task.isSuccessful()) {
                            // Registration succeeded
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Set display name for Firebase Auth
                                user.updateProfile(new UserProfileChangeRequest.Builder()
                                        .setDisplayName(fullName)
                                        .build())
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "User display name set successfully");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error setting display name: " + e.getMessage());
                                    });
                                
                                // Immediately show success since auth worked
                                hideLoading();
                                showSuccessMessage("Registration successful!");
                                
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("fullName", fullName);
                                userData.put("email", email);
                                userData.put("createdAt", System.currentTimeMillis());
                                
                                db.collection("users")
                                        .document(user.getUid())
                                        .set(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            // Data saved, proceed to next screen
                                            proceedToNextScreen();
                                        })
                                        .addOnFailureListener(e -> {
                                            // Even if Firestore fails, still proceed since authentication succeeded
                                            System.out.println("Error storing user data: " + e.getMessage());
                                            proceedToNextScreen();
                                        });
                            } else {
                                // This shouldn't happen but handle it anyway
                                hideLoading();
                                disableForm(false);
                                showErrorMessage("Failed to get user information. Please try logging in.");
                            }
                        } else {
                            // If sign up fails, display a message to the user.
                            hideLoading();
                            disableForm(false);
                            showErrorMessage("Registration failed: " + task.getException().getMessage());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Cancel timeout handler as we got a response
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    firebaseCheckHandler.removeCallbacks(firebaseCheckRunnable);
                    hideLoading();
                    disableForm(false);
                    showErrorMessage("Network error: " + e.getMessage());
                });
    }
    
    // Helper method to proceed to next screen
    private void proceedToNextScreen() {
        // Use Handler.postDelayed to ensure UI has time to update 
        // before proceeding, but keep the delay very short
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SignupActivity.this, PersonalizationActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        }, 500);
    }
    
    private void showLoading(String message) {
        tvLoadingMessage.setText(message);
        loadingOverlay.setVisibility(View.VISIBLE);
    }
    
    private void hideLoading() {
        loadingOverlay.setVisibility(View.GONE);
    }
    
    private void showSuccessMessage(String message) {
        hideMessage(errorMessage);
        tvSuccessMessage.setText(message);
        successMessage.setVisibility(View.VISIBLE);
        
        // Auto-hide after 3 seconds
        new Handler().postDelayed(() -> hideMessage(successMessage), 3000);
    }
    
    private void showErrorMessage(String message) {
        hideMessage(successMessage);
        tvErrorMessage.setText(message);
        errorMessage.setVisibility(View.VISIBLE);
        
        // Auto-hide after 3 seconds
        new Handler().postDelayed(() -> hideMessage(errorMessage), 3000);
    }
    
    private void hideMessage(View messageView) {
        messageView.setVisibility(View.GONE);
    }
    
    private void disableForm(boolean disable) {
        etFullName.setEnabled(!disable);
        etEmail.setEnabled(!disable);
        etPassword.setEnabled(!disable);
        btnRegister.setEnabled(!disable);
    }
}
