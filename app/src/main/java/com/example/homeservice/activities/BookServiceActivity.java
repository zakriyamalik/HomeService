package com.example.homeservice.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.homeservice.R;
import com.example.homeservice.utils.KeyUtils;

public class BookServiceActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_service);

        String serviceName = getIntent().getStringExtra(KeyUtils.KEY_SERVICE_NAME);
        double servicePrice = getIntent().getDoubleExtra(KeyUtils.KEY_SERVICE_PRICE, 0);

        TextView tvInfo = findViewById(R.id.tvServiceInfo);
        tvInfo.setText("Selected: " + serviceName + "\nPrice: $" + servicePrice + "\n\nBooking flow will be added in Module 8");

        Toast.makeText(this, "Service clicked: " + serviceName, Toast.LENGTH_SHORT).show();
    }
}