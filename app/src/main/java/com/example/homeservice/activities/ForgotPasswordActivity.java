package com.example.homeservice.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.HapticFeedbackConstants;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.homeservice.R;
import com.google.android.material.snackbar.Snackbar;
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
                v.performHapticFeedback(HapticFeedbackConstants.REJECT);
                etEmail.setError("Email is required");
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                v.performHapticFeedback(HapticFeedbackConstants.REJECT);
                etEmail.setError("Enter a valid email");
                return;
            }

            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            btnSendLink.setEnabled(false);
            btnSendLink.setText("Sending...");

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showSnack("Reset link sent to " + email, Snackbar.LENGTH_LONG);
                            new Handler(Looper.getMainLooper()).postDelayed(this::finish, 1500);
                        } else {
                            String msg = "Failed";
                            if (task.getException() != null && task.getException().getMessage() != null) {
                                msg = task.getException().getMessage();
                            }
                            showSnack(msg, Snackbar.LENGTH_LONG);
                            btnSendLink.setEnabled(true);
                            btnSendLink.setText("Send Reset Link");
                        }
                    });
        });
    }

    private void showSnack(String message, int length) {
        Snackbar.make(findViewById(android.R.id.content), message, length)
                .setBackgroundTint(getColor(R.color.color_surface))
                .setTextColor(getColor(R.color.color_text_primary))
                .show();
    }
}