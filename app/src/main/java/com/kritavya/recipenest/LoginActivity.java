package com.kritavya.recipenest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ImageView ivToggle;
    private boolean isPasswordVisible = false;
    
    // Loading and message views
    private View loadingOverlay;
    private TextView tvLoadingMessage;
    private View successMessage;
    private TextView tvSuccessMessage;
    private View errorMessage;
    private TextView tvErrorMessage;
    
    // Firebase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        ivToggle = findViewById(R.id.iv_toggle);
        btnLogin = findViewById(R.id.btn_login);
        TextView tvForgotPassword = findViewById(R.id.tv_forgot_password);
        TextView tvRegister = findViewById(R.id.tv_register);
        
        // Initialize loading and message views
        loadingOverlay = findViewById(R.id.loading_overlay);
        tvLoadingMessage = loadingOverlay.findViewById(R.id.tv_loading_message);
        successMessage = findViewById(R.id.success_message);
        tvSuccessMessage = findViewById(R.id.tv_success_message);
        errorMessage = findViewById(R.id.error_message);
        tvErrorMessage = findViewById(R.id.tv_error_message);

        ivToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivToggle.setImageResource(R.drawable.ic_eye_off);
            } else {
                etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivToggle.setImageResource(R.drawable.ic_eye_on);
            }
            isPasswordVisible = !isPasswordVisible;
            etPassword.setSelection(etPassword.getText().length());
        });

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty()) {
                etEmail.setError("Please enter your email");
                etEmail.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Please enter a valid email");
                etEmail.requestFocus();
            } else if (password.isEmpty()) {
                etPassword.setError("Please enter your password");
                etPassword.requestFocus();
            } else {
                loginUser(email, password);
            }
        });

        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
        
        // Setup message click listeners to dismiss them
        successMessage.setOnClickListener(v -> hideMessage(successMessage));
        errorMessage.setOnClickListener(v -> hideMessage(errorMessage));
    }
    
    private void loginUser(String email, String password) {
        // Show loading overlay and disable form
        showLoading("Logging in...");
        disableForm(true);
        
        // Add timeout handler to prevent indefinite loading
        final Handler timeoutHandler = new Handler();
        final Runnable timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if (loadingOverlay.getVisibility() == View.VISIBLE) {
                    hideLoading();
                    disableForm(false);
                    
                    // Check if user was logged in but UI didn't update
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null) {
                        // Sign out the user
                        mAuth.signOut();
                        showErrorMessage("Login timed out but you may be authenticated. Please try again.");
                    } else {
                        showErrorMessage("Request timed out. Please check your internet connection and try again.");
                    }
                }
            }
        };
        
        // Set a shorter timeout (8 seconds)
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
                    showSuccessMessage("Login successful!");
                    
                    // Proceed immediately
                    proceedToMainActivity();
                }
            }
        };
        
        // Check much sooner - after just 1.5 seconds
        firebaseCheckHandler.postDelayed(firebaseCheckRunnable, 1500);
        
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Cancel timeout handler as we got a response
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        firebaseCheckHandler.removeCallbacks(firebaseCheckRunnable);
                        hideLoading();
                        
                        if (task.isSuccessful()) {
                            // Sign in success
                            showSuccessMessage("Login successful!");
                            
                            // Use a shorter delay
                            new Handler().postDelayed(() -> {
                                proceedToMainActivity();
                            }, 300);
                        } else {
                            // If sign in fails
                            disableForm(false);
                            showErrorMessage("Authentication failed: " + task.getException().getMessage());
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
        etEmail.setEnabled(!disable);
        etPassword.setEnabled(!disable);
        btnLogin.setEnabled(!disable);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    // Add this method to handle back button press
    @Override
    public void onBackPressed() {
        // If loading is visible, cancel the login process
        if (loadingOverlay.getVisibility() == View.VISIBLE) {
            hideLoading();
            disableForm(false);
            
            // Check if user was authenticated but UI didn't update
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                // Sign out the user
                mAuth.signOut();
                showErrorMessage("Login cancelled. User was signed out.");
            } else {
                showErrorMessage("Login cancelled.");
            }
        } else {
            super.onBackPressed();
        }
    }

    // Helper method to proceed to main activity
    private void proceedToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
