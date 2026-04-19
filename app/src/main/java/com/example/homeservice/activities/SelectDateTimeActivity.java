package com.example.homeservice.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.homeservice.R;
import com.example.homeservice.utils.KeyUtils;

import java.util.Calendar;

public class SelectDateTimeActivity extends AppCompatActivity {

    private Button btnPickDate, btnPickTime, btnConfirm;
    private TextView tvSelectedDateTime;
    private String selectedDate = "";
    private String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_datetime);
        init();

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnPickTime.setOnClickListener(v -> showTimePicker());

        btnConfirm.setOnClickListener(v -> {
            if (!selectedDate.isEmpty() && !selectedTime.isEmpty()) {
                Intent result = new Intent();
                result.putExtra(KeyUtils.KEY_BOOKING_DATE, selectedDate);
                result.putExtra(KeyUtils.KEY_BOOKING_TIME, selectedTime);
                setResult(RESULT_OK, result);
                finish();
            } else {
                Toast.makeText(this, "Please select both date and time", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                    updateDisplay();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedTime = String.format("%02d:%02d", hourOfDay, minute);
                    updateDisplay();
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
        );
        dialog.show();
    }

    private void updateDisplay() {
        if (!selectedDate.isEmpty() && !selectedTime.isEmpty()) {
            tvSelectedDateTime.setText("Date: " + selectedDate + " | Time: " + selectedTime);
            btnConfirm.setEnabled(true);
        } else if (!selectedDate.isEmpty()) {
            tvSelectedDateTime.setText("Date: " + selectedDate + " | Time: not selected");
        } else if (!selectedTime.isEmpty()) {
            tvSelectedDateTime.setText("Date: not selected | Time: " + selectedTime);
        }
    }

    private void init() {
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnConfirm = findViewById(R.id.btnConfirm);
        tvSelectedDateTime = findViewById(R.id.tvSelectedDateTime);
    }
}