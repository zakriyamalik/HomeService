package com.example.homeservice.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeservice.R;
import com.example.homeservice.models.Booking;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private Context context;
    private List<Booking> bookings;

    public BookingAdapter(Context context, List<Booking> bookings) {
        this.context = context;
        this.bookings = bookings;
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
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName, tvDateTime, tvPrice, tvStatus;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvBookingServiceName);
            tvDateTime = itemView.findViewById(R.id.tvBookingDateTime);
            tvPrice = itemView.findViewById(R.id.tvBookingPrice);
            tvStatus = itemView.findViewById(R.id.tvBookingStatus);
        }
    }
}