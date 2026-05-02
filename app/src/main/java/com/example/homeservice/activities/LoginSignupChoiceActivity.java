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
        btnEmailAuth.setOnClickListener(v -> {
            Intent intent = new Intent(LoginSignupChoiceActivity.this, EmailLoginActivity.class);
            startActivity(intent);
        });
        btnLogin.setOnClickListener(v -> startActivity(new Intent(this, PhoneAuthActivity.class)));
        btnSignup.setOnClickListener(v -> startActivity(new Intent(this, PhoneAuthActivity.class)));
    }

    private void init() {
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);
    }
}