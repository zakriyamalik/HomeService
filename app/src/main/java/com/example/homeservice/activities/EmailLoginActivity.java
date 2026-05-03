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
import com.example.homeservice.models.User;
import com.example.homeservice.utils.KeyUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailLoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvRegisterLink;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private LocalRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);

        mAuth = FirebaseAuth.getInstance();
        repository = new LocalRepository(MyApplication.getDatabaseHelper());

        initViews();

        btnLogin.setOnClickListener(v -> attemptLogin(v));

        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(EmailLoginActivity.this, EmailRegisterActivity.class);
            startActivity(intent);
            finish();
        });

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(EmailLoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
        progressBar = findViewById(R.id.progressBar);
    }

    private void showSnack(String message, int length) {
        Snackbar.make(findViewById(android.R.id.content), message, length)
                .setBackgroundTint(getColor(R.color.color_surface))
                .setTextColor(getColor(R.color.color_text_primary))
                .show();
    }

    private void attemptLogin(View v) {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

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

        v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        showLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (isFinishing()) return;

                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser == null) {
                                showLoading(false);
                                showSnack("Login failed", Snackbar.LENGTH_LONG);
                                return;
                            }

                            String uid = firebaseUser.getUid();
                            String name = firebaseUser.getDisplayName();
                            if (name == null || name.isEmpty()) name = "User";

                            User existingUser = repository.getUserByUid(uid);
                            String phone = (existingUser != null && !existingUser.getPhone().isEmpty())
                                    ? existingUser.getPhone() : "";

                            repository.insertOrUpdateUser(uid, name, email, phone, "");

                            SharedPreferences userPrefs = getSharedPreferences("USER", MODE_PRIVATE);
                            userPrefs.edit()
                                    .putString("firebase_uid", uid)
                                    .putString(KeyUtils.KEY_NAME, name)
                                    .putString(KeyUtils.KEY_EMAIL, email)
                                    .putString("user_phone", phone)
                                    .putBoolean(KeyUtils.KEY_IS_LOGIN, true)
                                    .apply();

                            showSnack("Welcome back", Snackbar.LENGTH_SHORT);

                            if (phone.isEmpty()) {
                                showPhoneDialog(uid, name, email);
                            } else {
                                proceedToHome();
                            }
                        } else {
                            showLoading(false);
                            String errorMsg = "Login failed";
                            if (task.getException() != null) {
                                String msg = task.getException().getMessage();
                                if (msg != null) {
                                    if (msg.contains("no user record")) {
                                        errorMsg = "No account found with this email";
                                    } else if (msg.contains("password is invalid")) {
                                        errorMsg = "Incorrect password";
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
                    if (phone.isEmpty()) phone = "";
                    repository.updateUserPhone(uid, phone);
                    SharedPreferences userPrefs = getSharedPreferences("USER", MODE_PRIVATE);
                    userPrefs.edit().putString("user_phone", phone).apply();
                    proceedToHome();
                })
                .setNegativeButton("Skip", (dialog, which) -> proceedToHome())
                .show();
    }

    private void proceedToHome() {
        Intent intent = new Intent(EmailLoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
    }
}