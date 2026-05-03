package com.example.homeservice.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeservice.MyApplication;
import com.example.homeservice.R;
import com.example.homeservice.activities.HomeActivity;
import com.example.homeservice.adapters.BookingAdapter;
import com.example.homeservice.database.LocalRepository;
import com.example.homeservice.models.Booking;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookingsFragment extends Fragment
        implements BookingAdapter.OnCancelClickListener,
        BookingAdapter.OnRateClickListener,
        BookingAdapter.OnCompleteClickListener {

    private RecyclerView rvBookings;
    private View emptyState;
    private ChipGroup chipGroupFilter;
    private Button btnClearAll;
    private BookingAdapter adapter;
    private LocalRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private AlertDialog activeDialog;
    private String currentUserId;
    private String currentFilter = "All";
    private List<Booking> allBookings = new ArrayList<>();
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault());

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

        Button btnBrowse = emptyState.findViewById(R.id.btnBrowseServices);
        if (btnBrowse != null) {
            btnBrowse.setOnClickListener(v -> {
                if (getActivity() instanceof HomeActivity) {
                    ((HomeActivity) getActivity()).switchToTab(0);
                }
            });
        }

        btnClearAll.setOnClickListener(v -> showClearAllDialog());

        chipGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAll) currentFilter = "All";
            else if (checkedId == R.id.chipUpcoming) currentFilter = Booking.STATUS_UPCOMING;
            else if (checkedId == R.id.chipCompleted) currentFilter = Booking.STATUS_COMPLETED;
            else if (checkedId == R.id.chipMissed) currentFilter = Booking.STATUS_MISSED;
            else if (checkedId == R.id.chipCancelled) currentFilter = Booking.STATUS_CANCELLED;
            else if (checkedId == R.id.chipRated) currentFilter = Booking.STATUS_RATED;
            applyFilter();
        });

        loadBookings();
    }

    private void loadBookings() {
        currentUserId = requireActivity().getSharedPreferences("USER", Context.MODE_PRIVATE)
                .getString("firebase_uid", "temp_user");

        executor.execute(() -> {
            List<Booking> bookings = repository.getAllBookings(currentUserId);

            boolean anyUpdated = false;
            for (Booking booking : bookings) {
                if (Booking.STATUS_UPCOMING.equals(booking.getStatus()) && isDateTimePassed(booking)) {
                    repository.updateBookingStatus(booking.getId(), Booking.STATUS_MISSED, currentUserId);
                    booking.setStatus(Booking.STATUS_MISSED);
                    anyUpdated = true;
                }
            }

            if (anyUpdated) {
                bookings = repository.getAllBookings(currentUserId);
            }

            allBookings.clear();
            allBookings.addAll(bookings);

            mainHandler.post(() -> {
                if (!isAdded() || getContext() == null) return;
                applyFilter();
            });
        });
    }

    private boolean isDateTimePassed(Booking booking) {
        try {
            Date bookingDate = dateTimeFormat.parse(booking.getDate() + " " + booking.getTime());
            return bookingDate != null && bookingDate.before(new Date());
        } catch (ParseException e) {
            return false;
        }
    }

    private void applyFilter() {
        List<Booking> filtered = new ArrayList<>();
        for (Booking booking : allBookings) {
            if ("All".equals(currentFilter) || currentFilter.equals(booking.getStatus())) {
                filtered.add(booking);
            }
        }

        if (adapter == null) {
            adapter = new BookingAdapter(requireContext(), filtered, this, this, this);
            rvBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvBookings.setAdapter(adapter);
        } else {
            adapter.updateBookings(filtered);
        }

        updateEmptyState(filtered.isEmpty());
    }

    private void updateEmptyState(boolean isEmpty) {
        rvBookings.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        if (emptyState != null) {
            emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onCompleteClick(Booking booking) {
        executor.execute(() -> {
            int result = repository.updateBookingStatus(booking.getId(), Booking.STATUS_COMPLETED, currentUserId);
            mainHandler.post(() -> {
                if (!isAdded() || getContext() == null) return;
                if (result > 0) {
                    showSnack("Booking completed");
                    loadBookings();
                } else {
                    showSnack("Failed to complete");
                }
            });
        });
    }

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
            int result = repository.updateBookingStatus(booking.getId(), Booking.STATUS_CANCELLED, currentUserId);
            mainHandler.post(() -> {
                if (!isAdded() || getContext() == null) return;
                if (result > 0) {
                    showSnack("Booking cancelled");
                    loadBookings();
                } else {
                    showSnack("Failed to cancel");
                }
            });
        });
    }

    @Override
    public void onRateClick(Booking booking) {
        if (!isAdded() || getContext() == null) return;

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
            int result = repository.updateBookingRating(booking.getId(), rating, currentUserId);
            mainHandler.post(() -> {
                if (!isAdded() || getContext() == null) return;
                if (result > 0) {
                    showSnack("Rated " + rating + " stars");
                    loadBookings();
                } else {
                    showSnack("Failed to save rating");
                }
            });
        });
    }

    private void showClearAllDialog() {
        activeDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Clear All Bookings")
                .setMessage("Are you sure you want to remove all bookings? This cannot be undone.")
                .setPositiveButton("Clear All", (dialog, which) -> clearAllBookings())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearAllBookings() {
        executor.execute(() -> {
            int result = repository.deleteAllBookings(currentUserId);
            mainHandler.post(() -> {
                if (!isAdded() || getContext() == null) return;
                if (result > 0) {
                    showSnack("All bookings cleared");
                } else {
                    showSnack("No bookings to clear");
                }
                loadBookings();
            });
        });
    }

    private void showSnack(String message) {
        if (getView() == null) return;
        Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(requireContext().getColor(R.color.color_surface))
                .setTextColor(requireContext().getColor(R.color.color_text_primary))
                .show();
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
        emptyState = view.findViewById(R.id.emptyState);
        chipGroupFilter = view.findViewById(R.id.chipGroupFilter);
        btnClearAll = view.findViewById(R.id.btnClearAll);
    }
}