package com.example.homeservice.activities;
import com.example.homeservice.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etEmail;
    private Button btnSendLink;
    private TextView tvTitle;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        btnBack = findViewById(R.id.btnBack);
        etEmail = findViewById(R.id.etEmail);
        btnSendLink = findViewById(R.id.btnSendLink);
        tvTitle = findViewById(R.id.tvTitle);

        btnBack.setOnClickListener(v -> finish());

        btnSendLink.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.isEmpty()) {
                etEmail.setError("Email is required");
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Enter a valid email");
                return;
            }

            btnSendLink.setEnabled(false);
            btnSendLink.setText("Sending...");

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Reset link sent to " + email, Toast.LENGTH_LONG).show();
                            new Handler(Looper.getMainLooper()).postDelayed(this::finish, 1500);
                        } else {
                            Toast.makeText(this, "Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            btnSendLink.setEnabled(true);
                            btnSendLink.setText("Send Reset Link");
                        }
                    });
        });
    }
}