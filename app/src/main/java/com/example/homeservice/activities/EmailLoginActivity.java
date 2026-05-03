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

        btnLogin.setOnClickListener(v -> attemptLogin());

        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(EmailLoginActivity.this, EmailRegisterActivity.class);
            startActivity(intent);
            finish();
        });

        // FIXED: Now opens full ForgotPasswordActivity screen instead of Toast
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

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

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
                                Toast.makeText(EmailLoginActivity.this, "Login failed", Toast.LENGTH_LONG).show();
                                return;
                            }

                            String uid = firebaseUser.getUid();
                            String name = firebaseUser.getDisplayName();
                            if (name == null || name.isEmpty()) name = "User";

                            repository.insertOrUpdateUser(uid, name, email, "", "");

                            SharedPreferences userPrefs = getSharedPreferences("USER", MODE_PRIVATE);
                            userPrefs.edit()
                                    .putString("firebase_uid", uid)
                                    .putString(KeyUtils.KEY_NAME, name)
                                    .putString(KeyUtils.KEY_EMAIL, email)
                                    .putBoolean(KeyUtils.KEY_IS_LOGIN, true)
                                    .apply();

                            Toast.makeText(EmailLoginActivity.this, "Welcome back", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(EmailLoginActivity.this, HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
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
                            Toast.makeText(EmailLoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
    }
}