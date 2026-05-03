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
    private TextView tvTagline;
    private final Handler splashHandler = new Handler(Looper.getMainLooper());
    private Animation logoAnim;
    private Animation textAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Dark mode setup
        boolean isDarkMode = getSharedPreferences("APP", MODE_PRIVATE)
                .getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        );

        setContentView(R.layout.activity_splash);

        ivLogo = findViewById(R.id.ivLogo);
        tvAppName = findViewById(R.id.tvAppName);
        tvTagline = findViewById(R.id.tvTagline);

        // Load NEW separate animations from res/anim/
        logoAnim = AnimationUtils.loadAnimation(this, R.anim.scale_up);   // Logo scales up
        textAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in);    // Text fades in

        // Apply animations
        if (ivLogo != null) {
            ivLogo.startAnimation(logoAnim);
        }
        if (tvAppName != null) {
            tvAppName.startAnimation(textAnim);
        }
        if (tvTagline != null) {
            // Delay tagline fade slightly for staggered effect
            tvTagline.postDelayed(() -> tvTagline.startAnimation(textAnim), 300);
        }

        // Check auth state
        SharedPreferences appPrefs = getSharedPreferences("APP", MODE_PRIVATE);
        SharedPreferences userPrefs = getSharedPreferences("USER", MODE_PRIVATE);

        boolean isFirstLaunch = appPrefs.getBoolean("isFirstLaunch", true);
        boolean isLoggedIn = userPrefs.getBoolean(KeyUtils.KEY_IS_LOGIN, false);
        String firebaseUid = userPrefs.getString("firebase_uid", null);
        boolean hasFirebaseUser = (firebaseUid != null && !firebaseUid.isEmpty());

        // Navigate after delay with FADE transition
        splashHandler.postDelayed(() -> {
            if (isFinishing()) return;

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
            overridePendingTransition(R.anim.fade_in, android.R.anim.fade_out);  // NEW: Premium fade
            finish();
        }, 2500); // Slightly longer for animation to breathe
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        splashHandler.removeCallbacksAndMessages(null);

        if (logoAnim != null) logoAnim.cancel();
        if (textAnim != null) textAnim.cancel();
        if (ivLogo != null) ivLogo.clearAnimation();
        if (tvAppName != null) tvAppName.clearAnimation();
        if (tvTagline != null) tvTagline.clearAnimation();
    }
}