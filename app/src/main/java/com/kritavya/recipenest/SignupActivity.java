package com.kritavya.recipenest;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private ImageView ivToggle;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initializing views
        etFullName = findViewById(R.id.et_fullname);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
        ivToggle = findViewById(R.id.iv_toggle);


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

                    Toast.makeText(SignupActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();


                    Intent intent = new Intent(SignupActivity.this, PersonalizationActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
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
    }
}
