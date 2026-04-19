package com.example.homeservice.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.homeservice.R;

public class VerifyOtpActivity extends AppCompatActivity {
    private EditText etOtp;
    private Button btnVerify, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);
        init();

        String email = getIntent().getStringExtra("email"); // not used, just for demo

        btnCancel.setOnClickListener(v -> finish());

        btnVerify.setOnClickListener(v -> {
            String otp = etOtp.getText().toString().trim();
            if (otp.length() == 6) {
                Toast.makeText(this, "OTP verified. You can now reset password.", Toast.LENGTH_LONG).show();
                // Clear back stack to prevent navigation loop
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid OTP. Please enter 6 digits.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void init() {
        etOtp = findViewById(R.id.etOtp);
        btnVerify = findViewById(R.id.btnVerify);
        btnCancel = findViewById(R.id.btnCancel);
    }
}