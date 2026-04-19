package com.example.homeservice.fragments;

import android.os.Bundle;
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

public class BookingsFragment extends Fragment {

    private RecyclerView rvBookings;
    private TextView tvEmpty;
    private BookingAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);

        adapter = new BookingAdapter(requireContext(), MyApplication.bookings);
        rvBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBookings.setAdapter(adapter);

        updateEmptyState();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh when returning from booking flow
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            updateEmptyState();
        }
    }

    private void updateEmptyState() {
        if (MyApplication.bookings.isEmpty()) {
            rvBookings.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            rvBookings.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void init(View view) {
        rvBookings = view.findViewById(R.id.rvBookings);
        tvEmpty = view.findViewById(R.id.tvEmpty);
    }
}