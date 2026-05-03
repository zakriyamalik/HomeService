package com.example.homeservice.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.homeservice.R;

public class LoginSignupChoiceActivity extends AppCompatActivity {
    private Button btnLogin, btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup_choice);
        init();

        Button btnEmailAuth = findViewById(R.id.btnEmailAuth);
        btnEmailAuth.setOnClickListener(v -> startAnimated(EmailLoginActivity.class));

        btnLogin.setOnClickListener(v -> startAnimated(PhoneAuthActivity.class));
        btnSignup.setOnClickListener(v -> startAnimated(PhoneAuthActivity.class));
    }

    private void startAnimated(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void init() {
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);
    }
}