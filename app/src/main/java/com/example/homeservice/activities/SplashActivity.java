package com.example.homeservice.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.homeservice.R;
import com.example.homeservice.utils.KeyUtils;

public class SplashActivity extends AppCompatActivity {

    private ImageView ivLogo;
    private TextView tvAppName;
    private final Handler splashHandler = new Handler(Looper.getMainLooper());
    private Animation logoAnim;
    private Animation textAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isDarkMode = getSharedPreferences("APP", MODE_PRIVATE)
                .getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        );
        setContentView(R.layout.activity_splash);

        ivLogo = findViewById(R.id.ivLogo);
        tvAppName = findViewById(R.id.tvAppName);

        // Load separate animation instances (don't share state between views)
        logoAnim = AnimationUtils.loadAnimation(this, R.anim.splash_animation);
        textAnim = AnimationUtils.loadAnimation(this, R.anim.splash_animation);

        if (ivLogo != null) {
            ivLogo.startAnimation(logoAnim);
        }
        if (tvAppName != null) {
            tvAppName.startAnimation(textAnim);
        }

        SharedPreferences appPrefs = getSharedPreferences("APP", MODE_PRIVATE);
        SharedPreferences userPrefs = getSharedPreferences("USER", MODE_PRIVATE);

        boolean isFirstLaunch = appPrefs.getBoolean("isFirstLaunch", true);
        boolean isLoggedIn = userPrefs.getBoolean(KeyUtils.KEY_IS_LOGIN, false);
        String firebaseUid = userPrefs.getString("firebase_uid", null);
        boolean hasFirebaseUser = (firebaseUid != null && !firebaseUid.isEmpty());

        splashHandler.postDelayed(() -> {
            if (isFinishing()) return; // Don't route if user pressed back

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove pending handler callback to prevent crash on back press
        splashHandler.removeCallbacksAndMessages(null);

        // Cancel running animations to prevent leak
        if (logoAnim != null) {
            logoAnim.cancel();
        }
        if (textAnim != null) {
            textAnim.cancel();
        }
        if (ivLogo != null) {
            ivLogo.clearAnimation();
        }
        if (tvAppName != null) {
            tvAppName.clearAnimation();
        }
    }
}