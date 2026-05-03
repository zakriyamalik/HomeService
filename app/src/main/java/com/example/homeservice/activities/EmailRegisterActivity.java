package com.example.homeservice.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.homeservice.MyApplication;
import com.example.homeservice.R;
import com.example.homeservice.database.LocalRepository;
import com.example.homeservice.utils.KeyUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
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

        btnRegister.setOnClickListener(v -> attemptRegister(v));
        tvLoginLink.setOnClickListener(v -> startAnimated(EmailLoginActivity.class));
    }

    private void startAnimated(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        tvLoginLink = findViewById(R.id.tvLoginLink);
    }

    private void showSnack(String message, int length) {
        Snackbar.make(findViewById(android.R.id.content), message, length)
                .setBackgroundTint(getColor(R.color.color_surface))
                .setTextColor(getColor(R.color.color_text_primary))
                .show();
    }

    private void attemptRegister(View v) {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty()) {
            v.performHapticFeedback(HapticFeedbackConstants.REJECT);
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }
        if (name.length() < 2) {
            v.performHapticFeedback(HapticFeedbackConstants.REJECT);
            etName.setError("Name must be at least 2 characters");
            etName.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            v.performHapticFeedback(HapticFeedbackConstants.REJECT);
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            v.performHapticFeedback(HapticFeedbackConstants.REJECT);
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            v.performHapticFeedback(HapticFeedbackConstants.REJECT);
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            v.performHapticFeedback(HapticFeedbackConstants.REJECT);
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
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
                                showSnack("Registration failed", Snackbar.LENGTH_LONG);
                                return;
                            }

                            String uid = firebaseUser.getUid();

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            firebaseUser.updateProfile(profileUpdates);

                            repository.insertOrUpdateUser(uid, name, email, "", "");

                            SharedPreferences userPrefs = getSharedPreferences("USER", MODE_PRIVATE);
                            userPrefs.edit()
                                    .putString("firebase_uid", uid)
                                    .putString(KeyUtils.KEY_NAME, name)
                                    .putString(KeyUtils.KEY_EMAIL, email)
                                    .putBoolean(KeyUtils.KEY_IS_LOGIN, true)
                                    .apply();

                            showSnack("Account created", Snackbar.LENGTH_SHORT);
                            showPhoneDialog(uid, name, email);
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
                            showSnack(errorMsg, Snackbar.LENGTH_LONG);
                        }
                    }
                });
    }

    private void showPhoneDialog(String uid, String name, String email) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_phone, null);
        TextInputEditText etPhone = dialogView.findViewById(R.id.etPhone);

        new AlertDialog.Builder(this)
                .setTitle("Add Phone Number")
                .setMessage("Please add your phone number for better service")
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton("Save", (dialog, which) -> {
                    String phone = etPhone.getText().toString().trim();
                    repository.updateUserPhone(uid, phone);
                    SharedPreferences userPrefs = getSharedPreferences("USER", MODE_PRIVATE);
                    userPrefs.edit().putString("user_phone", phone).apply();
                    proceedToHome();
                })
                .setNegativeButton("Skip", (dialog, which) -> proceedToHome())
                .show();
    }

    private void proceedToHome() {
        Intent intent = new Intent(EmailRegisterActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
    }
}