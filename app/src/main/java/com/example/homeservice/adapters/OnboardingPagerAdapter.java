package com.example.homeservice.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeservice.R;

public class OnboardingPagerAdapter extends RecyclerView.Adapter<OnboardingPagerAdapter.OnboardingViewHolder> {

    private Context context;
    private String[] titles = {"Welcome", "Find Services", "Book Easily"};
    private String[] descriptions = {
            "Browse hundreds of home services at your fingertips",
            "Select from cleaning, plumbing, electrician and more",
            "Choose date & time and confirm your booking instantly"
    };
    private int[] images = {
            android.R.drawable.ic_menu_edit,
            android.R.drawable.ic_menu_edit,
            android.R.drawable.ic_menu_edit
    };

    public OnboardingPagerAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_onboarding, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        holder.tvTitle.setText(titles[position]);
        holder.tvDesc.setText(descriptions[position]);
        holder.ivImage.setImageResource(images[position]);
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc;
        ImageView ivImage;

        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            ivImage = itemView.findViewById(R.id.ivImage);
        }
    }
}
