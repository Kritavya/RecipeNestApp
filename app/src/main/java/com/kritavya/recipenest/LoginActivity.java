package com.kritavya.recipenest;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private ImageView ivToggle;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        ivToggle = findViewById(R.id.iv_toggle);
        Button btnLogin = findViewById(R.id.btn_login);
        TextView tvForgotPassword = findViewById(R.id.tv_forgot_password);
        TextView tvRegister = findViewById(R.id.tv_register);


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

                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        });


        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "Forgot Password Clicked", Toast.LENGTH_SHORT).show();
             startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });


        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }
}
