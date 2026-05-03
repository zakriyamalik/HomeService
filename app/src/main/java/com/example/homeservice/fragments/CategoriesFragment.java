package com.example.homeservice.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeservice.MyApplication;
import com.example.homeservice.R;
import com.example.homeservice.adapters.CategoryAdapter;
import com.example.homeservice.database.LocalRepository;
import com.example.homeservice.models.Category;
import com.example.homeservice.models.Service;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoriesFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener {

    private RecyclerView rvCategories;
    private CategoryAdapter adapter;
    private LocalRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        repository = new LocalRepository(MyApplication.getDatabaseHelper());
        loadCategories();
    }

    private void loadCategories() {
        executor.execute(() -> {
            List<Category> categories = repository.getAllCategories();
            mainHandler.post(() -> {
                if (!isAdded() || getContext() == null) return;

                adapter = new CategoryAdapter(requireContext(), categories, this, -1, R.layout.item_category_grid);
                rvCategories.setLayoutManager(new GridLayoutManager(requireContext(), 2));
                rvCategories.setAdapter(adapter);
            });
        });
    }

    @Override
    public void onCategoryClick(Category category, int position) {
        if (category.getId() == 0) {
            showSnack("Select a specific category");
            return;
        }

        executor.execute(() -> {
            List<Service> services = repository.getServicesByCategory(category.getId());
            mainHandler.post(() -> {
                if (!isAdded() || getContext() == null) return;

                if (services.isEmpty()) {
                    showSnack("No services in " + category.getName());
                    return;
                }

                showServicesDialog(category.getName(), services);
            });
        });
    }

    private void showServicesDialog(String categoryName, List<Service> services) {
        String[] items = new String[services.size()];
        for (int i = 0; i < services.size(); i++) {
            items[i] = services.get(i).getName() + " - $" + services.get(i).getPrice();
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(categoryName + " Services")
                .setItems(items, (dialog, which) -> {
                    Service s = services.get(which);
                    Intent intent = new Intent(requireContext(), com.example.homeservice.activities.BookServiceActivity.class);
                    intent.putExtra("service_id", s.getId());
                    intent.putExtra("service_name", s.getName());
                    intent.putExtra("service_price", s.getPrice());
                    startActivity(intent);
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void showSnack(String message) {
        if (getView() == null) return;
        Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(requireContext().getColor(R.color.color_surface))
                .setTextColor(requireContext().getColor(R.color.color_text_primary))
                .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void init(View view) {
        rvCategories = view.findViewById(R.id.rvCategoriesGrid);
    }
}