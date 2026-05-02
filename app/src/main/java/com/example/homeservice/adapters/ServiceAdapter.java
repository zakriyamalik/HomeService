package com.example.homeservice.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeservice.R;
import com.example.homeservice.models.Service;

import java.util.ArrayList;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private Context context;
    private List<Service> displayList;   // What RecyclerView shows
    private List<Service> originalList;  // Master copy from MyApplication
    private OnServiceClickListener listener;

    public interface OnServiceClickListener {
        void onServiceClick(Service service);
    }

    public ServiceAdapter(Context context, List<Service> services, OnServiceClickListener listener) {
        this.context = context;
        this.originalList = new ArrayList<>(services);
        this.displayList = new ArrayList<>(services);
        this.listener = listener;
    }

    public void filterByCategory(int categoryId) {
        displayList.clear();
        for (Service service : originalList) {
            if (service.getCategoryId() == categoryId) {
                displayList.add(service);
            }
        }
        notifyDataSetChanged();
    }

    public void resetFilter() {
        displayList.clear();
        displayList.addAll(originalList);
        notifyDataSetChanged();
    }

    // ✅ NEW METHOD for SearchFragment (efficient filtering)
    public void updateList(List<Service> newList) {
        this.displayList.clear();
        this.displayList.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = displayList.get(position);
        holder.tvName.setText(service.getName());
        holder.tvDesc.setText(service.getDescription());
        holder.tvPrice.setText("$" + service.getPrice());
        holder.itemView.setOnClickListener(v -> listener.onServiceClick(service));
    }


    @Override
    public int getItemCount() {
        return displayList.size();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvPrice;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvServiceName);
            tvDesc = itemView.findViewById(R.id.tvServiceDesc);
            tvPrice = itemView.findViewById(R.id.tvServicePrice);
        }
    }
}