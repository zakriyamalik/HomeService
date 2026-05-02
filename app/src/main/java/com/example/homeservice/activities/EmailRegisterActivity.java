package com.example.homeservice.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.homeservice.MyApplication;
import com.example.homeservice.R;
import com.example.homeservice.database.LocalRepository;
import com.example.homeservice.utils.KeyUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class EmailRegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private Button btnRegister;
    private ProgressBar progressBar;
    private TextView tvLoginLink;
    private FirebaseAuth mAuth;
    private LocalRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_register);

        mAuth = FirebaseAuth.getInstance();
        repository = new LocalRepository(MyApplication.getDatabaseHelper());

        initViews();

        btnRegister.setOnClickListener(v -> attemptRegister());
        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(EmailRegisterActivity.this, EmailLoginActivity.class);
            startActivity(intent);
        });
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        tvLoginLink = findViewById(R.id.tvLoginLink);
    }

    private void attemptRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }
        if (name.length() < 2) {
            etName.setError("Name must be at least 2 characters");
            etName.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        showLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (isFinishing()) return;

                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser == null) {
                                showLoading(false);
                                Toast.makeText(EmailRegisterActivity.this,
                                        "Registration failed", Toast.LENGTH_LONG).show();
                                return;
                            }

                            String uid = firebaseUser.getUid();

                            // Save display name to Firebase (optional but useful)
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            firebaseUser.updateProfile(profileUpdates);

                            // Save to local SQLite
                            repository.insertOrUpdateUser(uid, name, email, "", "");

                            // Save session
                            SharedPreferences userPrefs = getSharedPreferences("USER", MODE_PRIVATE);
                            userPrefs.edit()
                                    .putString("firebase_uid", uid)
                                    .putString(KeyUtils.KEY_NAME, name)
                                    .putString(KeyUtils.KEY_EMAIL, email)
                                    .putBoolean(KeyUtils.KEY_IS_LOGIN, true)
                                    .apply();

                            Toast.makeText(EmailRegisterActivity.this,
                                    "Account created", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(EmailRegisterActivity.this, HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            showLoading(false);
                            String errorMsg = "Registration failed";
                            if (task.getException() != null) {
                                String msg = task.getException().getMessage();
                                if (msg != null) {
                                    if (msg.contains("email address is already in use")) {
                                        errorMsg = "This email is already registered";
                                    } else if (msg.contains("weak password")) {
                                        errorMsg = "Password is too weak";
                                    } else if (msg.contains("network")) {
                                        errorMsg = "Network error. Check connection";
                                    } else {
                                        errorMsg = msg;
                                    }
                                }
                            }
                            Toast.makeText(EmailRegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
    }
}