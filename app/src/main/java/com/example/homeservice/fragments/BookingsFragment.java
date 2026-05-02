package com.example.homeservice.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

public class BookingsFragment extends Fragment {

    private RecyclerView rvBookings;
    private TextView tvEmpty;
    private BookingAdapter adapter;
    private LocalRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

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
        String userId = requireActivity().getSharedPreferences("USER", MODE_PRIVATE)
                .getString("firebase_uid", "temp_user");
        executor.execute(() -> {
            List<Booking> bookings = repository.getAllBookings(userId);
            mainHandler.post(() -> {
                if (adapter == null) {
                    adapter = new BookingAdapter(requireContext(), new ArrayList<>(bookings));
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
        if (isEmpty) {
            rvBookings.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            rvBookings.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (repository != null) {
            loadBookings();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void init(View view) {
        rvBookings = view.findViewById(R.id.rvBookings);
        tvEmpty = view.findViewById(R.id.tvEmpty);
    }
}