package com.example.homeservice.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.homeservice.MyApplication;
import com.example.homeservice.R;
import com.example.homeservice.database.LocalRepository;
import com.example.homeservice.models.Booking;
import com.example.homeservice.utils.KeyUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.content.SharedPreferences;

public class BookServiceActivity extends AppCompatActivity {

    private TextView tvServiceName, tvServicePrice, tvSelectedDateTime;
    private Button btnSelectDateTime, btnConfirmBooking;
    private ActivityResultLauncher<Intent> dateTimeLauncher;
    private LocalRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private int serviceId;
    private String serviceName;
    private double servicePrice;
    private String selectedDate = "";
    private String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_service);
        init();

        repository = new LocalRepository(MyApplication.getDatabaseHelper());

        serviceId = getIntent().getIntExtra(KeyUtils.KEY_SERVICE_ID, -1);
        serviceName = getIntent().getStringExtra(KeyUtils.KEY_SERVICE_NAME);
        servicePrice = getIntent().getDoubleExtra(KeyUtils.KEY_SERVICE_PRICE, 0);

        tvServiceName.setText(serviceName);
        tvServicePrice.setText("$" + servicePrice);

        dateTimeLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedDate = result.getData().getStringExtra(KeyUtils.KEY_BOOKING_DATE);
                        selectedTime = result.getData().getStringExtra(KeyUtils.KEY_BOOKING_TIME);
                        tvSelectedDateTime.setText("Date: " + selectedDate + " | Time: " + selectedTime);
                        btnConfirmBooking.setEnabled(true);
                    }
                }
        );

        btnSelectDateTime.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectDateTimeActivity.class);
            dateTimeLauncher.launch(intent);
        });

        btnConfirmBooking.setOnClickListener(v -> {
            if (!selectedDate.isEmpty() && !selectedTime.isEmpty()) {
                saveBookingToDatabase();
            }
        });
    }

    private void saveBookingToDatabase() {
        SharedPreferences prefs = getSharedPreferences("USER", MODE_PRIVATE);
        String userId = prefs.getString("firebase_uid", "temp_user");
        Booking booking = new Booking(0, serviceId, serviceName, selectedDate, selectedTime,
                servicePrice, "Upcoming", 0, userId);

        btnConfirmBooking.setEnabled(false);
        btnConfirmBooking.setText("Saving...");

        executor.execute(() -> {
            long result = repository.insertBooking(booking);
            mainHandler.post(() -> {
                btnConfirmBooking.setEnabled(true);
                btnConfirmBooking.setText("Confirm Booking");

                if (result != -1) {
                    Toast.makeText(this, "Booking confirmed for " + serviceName, Toast.LENGTH_LONG).show();
                    Intent homeIntent = new Intent(this, HomeActivity.class);
                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(homeIntent);
                    finish();
                } else {
                    Toast.makeText(this, "Failed to save booking", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void init() {
        tvServiceName = findViewById(R.id.tvServiceName);
        tvServicePrice = findViewById(R.id.tvServicePrice);
        tvSelectedDateTime = findViewById(R.id.tvSelectedDateTime);
        btnSelectDateTime = findViewById(R.id.btnSelectDateTime);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
    }
}