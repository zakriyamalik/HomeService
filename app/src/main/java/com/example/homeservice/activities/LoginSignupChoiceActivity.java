package com.example.homeservice.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.homeservice.R;

public class LoginSignupChoiceActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup_choice);

        TextView tvMessage = findViewById(R.id.tvMessage);
        tvMessage.setText("Login/Signup Choice Screen - Coming in Module 3");
    }
}