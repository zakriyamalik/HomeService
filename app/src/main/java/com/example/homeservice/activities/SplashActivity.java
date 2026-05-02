package com.example.homeservice.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.homeservice.R;
import com.example.homeservice.utils.KeyUtils;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SharedPreferences appPrefs = getSharedPreferences("APP", MODE_PRIVATE);
        SharedPreferences userPrefs = getSharedPreferences("USER", MODE_PRIVATE);

        boolean isFirstLaunch = appPrefs.getBoolean("isFirstLaunch", true);
        boolean isLoggedIn = userPrefs.getBoolean(KeyUtils.KEY_IS_LOGIN, false);
        String firebaseUid = userPrefs.getString("firebase_uid", null);
        boolean hasFirebaseUser = (firebaseUid != null && !firebaseUid.isEmpty());

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (isLoggedIn && hasFirebaseUser) {
                intent = new Intent(SplashActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            } else if (isFirstLaunch) {
                intent = new Intent(SplashActivity.this, OnboardingActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, PhoneAuthActivity.class);
            }
            startActivity(intent);
            finish();
        }, 2000);
    }
}