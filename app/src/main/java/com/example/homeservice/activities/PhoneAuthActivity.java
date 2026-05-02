package com.example.homeservice.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.homeservice.MyApplication;
import com.example.homeservice.R;
import com.example.homeservice.database.LocalRepository;
import com.example.homeservice.utils.KeyUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneAuthActivity extends AppCompatActivity {

    private TextInputEditText etPhone, etOtp;
    private Button btnSendOtp, btnVerifyOtp;
    private TextView tvStatus;
    private FirebaseAuth mAuth;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);
        init();

        mAuth = FirebaseAuth.getInstance();
        // Optional: create LocalRepository if you need to store user info later
        // LocalRepository repository = new LocalRepository(MyApplication.getDatabaseHelper());

        btnSendOtp.setOnClickListener(v -> sendOtp());
        btnVerifyOtp.setOnClickListener(v -> verifyOtp());
    }

    private void sendOtp() {
        String phoneNumber = etPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNumber)) {
            etPhone.setError("Phone number required");
            return;
        }

        btnSendOtp.setEnabled(false);
        btnSendOtp.setText("Sending...");
        tvStatus.setText("Sending OTP...");

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        signInWithCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        btnSendOtp.setEnabled(true);
                        btnSendOtp.setText("Send OTP");
                        tvStatus.setText("Failed: " + e.getMessage());
                        Toast.makeText(PhoneAuthActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        PhoneAuthActivity.this.verificationId = verificationId;
                        resendToken = token;
                        btnSendOtp.setEnabled(true);
                        btnSendOtp.setText("Resend OTP");
                        tvStatus.setText("OTP sent. Enter code.");
                        etOtp.setEnabled(true);
                        btnVerifyOtp.setEnabled(true);
                    }
                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyOtp() {
        String code = etOtp.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            etOtp.setError("Enter code");
            return;
        }

        btnVerifyOtp.setEnabled(false);
        btnVerifyOtp.setText("Verifying...");

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = task.getResult().getUser().getUid();
                        String phone = task.getResult().getUser().getPhoneNumber();

                        SharedPreferences prefs = getSharedPreferences("USER", MODE_PRIVATE);
                        prefs.edit()
                                .putString("firebase_uid", uid)
                                .putString("user_phone", phone)
                                .putBoolean(KeyUtils.KEY_IS_LOGIN, true)
                                .apply();

                        Toast.makeText(PhoneAuthActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(PhoneAuthActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        btnVerifyOtp.setEnabled(true);
                        btnVerifyOtp.setText("Verify");
                        tvStatus.setText("Verification failed");
                        Toast.makeText(PhoneAuthActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void init() {
        etPhone = findViewById(R.id.etPhone);
        etOtp = findViewById(R.id.etOtp);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        tvStatus = findViewById(R.id.tvStatus);
        etOtp.setEnabled(false);
        btnVerifyOtp.setEnabled(false);
    }
}