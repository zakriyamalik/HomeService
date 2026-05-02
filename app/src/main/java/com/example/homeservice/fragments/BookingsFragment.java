package com.example.homeservice.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeservice.MyApplication;
import com.example.homeservice.R;
import com.example.homeservice.adapters.BookingAdapter;
import com.example.homeservice.database.LocalRepository;
import com.example.homeservice.models.Booking;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookingsFragment extends Fragment
        implements BookingAdapter.OnCancelClickListener,
        BookingAdapter.OnRateClickListener {

    private RecyclerView rvBookings;
    private TextView tvEmpty;
    private BookingAdapter adapter;
    private LocalRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private AlertDialog activeDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        repository = new LocalRepository(MyApplication.getDatabaseHelper());
        loadBookings();
    }

    private void loadBookings() {
        String userId = requireActivity().getSharedPreferences("USER", Context.MODE_PRIVATE)
                .getString("firebase_uid", "temp_user");

        executor.execute(() -> {
            List<Booking> bookings = repository.getAllActiveBookings(userId);
            mainHandler.post(() -> {
                if (!isAdded() || getContext() == null) return;

                if (adapter == null) {
                    adapter = new BookingAdapter(requireContext(), new ArrayList<>(bookings),
                            this, this);
                    rvBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
                    rvBookings.setAdapter(adapter);
                } else {
                    adapter.updateBookings(bookings);
                }
                updateEmptyState(bookings.isEmpty());
            });
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        rvBookings.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    // ---------- CANCELLATION (from Step 5) ----------
    @Override
    public void onCancelClick(Booking booking) {
        if (!isAdded() || getContext() == null) return;

        activeDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel this booking?")
                .setPositiveButton("Yes", (dialog, which) -> cancelBooking(booking))
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelBooking(Booking booking) {
        executor.execute(() -> {
            int result = repository.updateBookingStatus(booking.getId(), "Cancelled");
            mainHandler.post(() -> {
                if (!isAdded() || getContext() == null) return;
                if (result > 0) {
                    Toast.makeText(getContext(), "Booking cancelled", Toast.LENGTH_SHORT).show();
                    loadBookings();
                } else {
                    Toast.makeText(getContext(), "Failed to cancel", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // ---------- RATING (Step 6) ----------
    @Override
    public void onRateClick(Booking booking) {
        if (!isAdded() || getContext() == null) return;

        // Inflate dialog layout with RatingBar
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rate_booking, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.dialogRatingBar);
        ratingBar.setRating(booking.getRating());

        activeDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Rate " + booking.getServiceName())
                .setView(dialogView)
                .setPositiveButton("Submit", (dialog, which) -> {
                    int newRating = (int) ratingBar.getRating();
                    if (newRating > 0) {
                        submitRating(booking, newRating);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void submitRating(Booking booking, int rating) {
        executor.execute(() -> {
            int result = repository.updateBookingRating(booking.getId(), rating);
            mainHandler.post(() -> {
                if (!isAdded() || getContext() == null) return;
                if (result > 0) {
                    Toast.makeText(getContext(), "Rated " + rating + " stars", Toast.LENGTH_SHORT).show();
                    loadBookings(); // Refresh to show new rating
                } else {
                    Toast.makeText(getContext(), "Failed to save rating", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (repository != null) loadBookings();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (activeDialog != null && activeDialog.isShowing()) {
            activeDialog.dismiss();
        }
        executor.shutdown();
    }

    private void init(View view) {
        rvBookings = view.findViewById(R.id.rvBookings);
        tvEmpty = view.findViewById(R.id.tvEmpty);
    }
}