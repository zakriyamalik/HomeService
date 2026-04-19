package com.example.homeservice.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.homeservice.R;
import com.example.homeservice.utils.KeyUtils;

public class SignupActivity extends AppCompatActivity {
    private EditText etName, etUsername, etPassword, etConfirmPassword;
    private Button btnSignup, btnCancel;
    private TextView tvLogin;
    private SharedPreferences sPref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        init();

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btnCancel.setOnClickListener(v -> finish());

        btnSignup.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString();
            String confirmPassword = etConfirmPassword.getText().toString();

            if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Prevent signup if any account already exists (single-user demo)
            String existingUsername = sPref.getString(KeyUtils.KEY_USERNAME, "");
            if (!existingUsername.isEmpty()) {
                Toast.makeText(this, "An account already exists. Please login.", Toast.LENGTH_SHORT).show();
                return;
            }

            editor.putString(KeyUtils.KEY_NAME, name);
            editor.putString(KeyUtils.KEY_USERNAME, username);
            editor.putString(KeyUtils.KEY_PASSWORD, password);
            editor.putBoolean(KeyUtils.KEY_IS_LOGIN, true);
            editor.apply();

            Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT).show();

            // Clear back stack and go to Home
            Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void init() {
        etName = findViewById(R.id.etName);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignup = findViewById(R.id.btnSignup);
        btnCancel = findViewById(R.id.btnCancel);
        tvLogin = findViewById(R.id.tvLogin);
        sPref = getSharedPreferences("USER", MODE_PRIVATE);
        editor = sPref.edit();
    }
}