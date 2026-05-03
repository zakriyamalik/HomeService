package com.example.homeservice.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeservice.R;
import com.example.homeservice.models.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private List<Category> categories;
    private OnCategoryClickListener listener;
    private int selectedCategoryId;
    private int layoutResId;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category, int position);
    }

    public CategoryAdapter(Context context, List<Category> categories, OnCategoryClickListener listener, int selectedCategoryId) {
        this(context, categories, listener, selectedCategoryId, R.layout.item_category);
    }

    public CategoryAdapter(Context context, List<Category> categories, OnCategoryClickListener listener, int selectedCategoryId, int layoutResId) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
        this.selectedCategoryId = selectedCategoryId;
        this.layoutResId = layoutResId;
    }

    public void setSelectedCategoryId(int selectedCategoryId) {
        this.selectedCategoryId = selectedCategoryId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(layoutResId, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.tvName.setText(category.getName());
        holder.ivIcon.setImageResource(getCategoryIcon(category.getName()));

        boolean isSelected = category.getId() == selectedCategoryId;
        if (isSelected) {
            holder.cvCategory.setCardBackgroundColor(ContextCompat.getColor(context, R.color.color_primary));
            holder.ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.primary_white));
            holder.tvName.setTextColor(ContextCompat.getColor(context, R.color.color_primary));
        } else {
            holder.cvCategory.setCardBackgroundColor(ContextCompat.getColor(context, R.color.color_card_background));
            holder.ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.color_primary));
            holder.tvName.setTextColor(ContextCompat.getColor(context, R.color.color_text_primary));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivIcon;
        CardView cvCategory;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            ivIcon = itemView.findViewById(R.id.ivCategoryIcon);
            cvCategory = itemView.findViewById(R.id.cvCategory);
        }
    }

    private int getCategoryIcon(String categoryName) {
        if (categoryName == null) return R.drawable.ic_category_all;
        switch (categoryName) {
            case "Cleaning": return R.drawable.ic_category_cleaning;
            case "Plumbing": return R.drawable.ic_category_plumbing;
            case "Electrician": return R.drawable.ic_category_electrician;
            case "Painting": return R.drawable.ic_category_painting;
            case "AC Repair": return R.drawable.ic_category_ac;
            case "All": return R.drawable.ic_category_all;
            default: return R.drawable.ic_category_all;
        }
    }
}