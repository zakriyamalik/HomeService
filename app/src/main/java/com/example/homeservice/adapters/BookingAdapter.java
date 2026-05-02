package com.example.homeservice.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeservice.R;
import com.example.homeservice.models.Booking;

import java.util.ArrayList;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private Context context;
    private List<Booking> bookings;
    private OnCancelClickListener cancelListener;
    private OnRateClickListener rateListener;

    public interface OnCancelClickListener {
        void onCancelClick(Booking booking);
    }

    public interface OnRateClickListener {
        void onRateClick(Booking booking);
    }

    public BookingAdapter(Context context, List<Booking> bookings,
                          OnCancelClickListener cancelListener,
                          OnRateClickListener rateListener) {
        this.context = context;
        this.bookings = new ArrayList<>(bookings);
        this.cancelListener = cancelListener;
        this.rateListener = rateListener;
    }

    public void updateBookings(List<Booking> newBookings) {
        this.bookings.clear();
        this.bookings.addAll(newBookings);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.tvServiceName.setText(booking.getServiceName());
        holder.tvDateTime.setText(booking.getDate() + " at " + booking.getTime());
        holder.tvPrice.setText("$" + booking.getPrice());
        holder.tvStatus.setText(booking.getStatus());

        // Show rating (0 = not rated, show empty stars)
        holder.rbRating.setRating(booking.getRating());

        // Cancel button only for Upcoming
        if ("Upcoming".equals(booking.getStatus()) && cancelListener != null) {
            holder.btnCancel.setVisibility(View.VISIBLE);
            holder.btnCancel.setOnClickListener(v -> cancelListener.onCancelClick(booking));
        } else {
            holder.btnCancel.setVisibility(View.GONE);
        }

        // Rate button for all non-cancelled bookings
        if (!"Cancelled".equals(booking.getStatus()) && rateListener != null) {
            holder.btnRate.setVisibility(View.VISIBLE);
            holder.btnRate.setOnClickListener(v -> rateListener.onRateClick(booking));
        } else {
            holder.btnRate.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName, tvDateTime, tvPrice, tvStatus;
        RatingBar rbRating;
        Button btnCancel, btnRate;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvBookingServiceName);
            tvDateTime = itemView.findViewById(R.id.tvBookingDateTime);
            tvPrice = itemView.findViewById(R.id.tvBookingPrice);
            tvStatus = itemView.findViewById(R.id.tvBookingStatus);
            rbRating = itemView.findViewById(R.id.rbBookingRating);
            btnCancel = itemView.findViewById(R.id.btnCancelBooking);
            btnRate = itemView.findViewById(R.id.btnRateBooking);
        }
    }
}