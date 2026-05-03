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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeservice.MyApplication;
import com.example.homeservice.R;
import com.example.homeservice.activities.BookServiceActivity;
import com.example.homeservice.adapters.CategoryAdapter;
import com.example.homeservice.adapters.ServiceAdapter;
import com.example.homeservice.database.LocalRepository;
import com.example.homeservice.models.Category;
import com.example.homeservice.models.Service;
import com.example.homeservice.utils.KeyUtils;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener, ServiceAdapter.OnServiceClickListener {

    private RecyclerView rvCategories, rvServices;
    private CategoryAdapter categoryAdapter;
    private LocalRepository repository;
    private List<Service> allServices = new ArrayList<>();
    private int selectedCategoryId = -1;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        repository = new LocalRepository(MyApplication.getDatabaseHelper());
        loadData();
    }

    private void loadData() {
        executor.execute(() -> {
            List<Category> categories = repository.getAllCategories();
            List<Service> services = repository.getAllServices();

            if (services != null) {
                allServices.clear();
                allServices.addAll(services);
            }

            mainHandler.post(() -> {
                if (!isAdded() || getContext() == null) return;

                // Setup categories (use database list only — no duplicate "All")
                categoryAdapter = new CategoryAdapter(requireContext(), categories, this, selectedCategoryId);
                LinearLayoutManager catLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
                rvCategories.setLayoutManager(catLayoutManager);
                rvCategories.setAdapter(categoryAdapter);

                // Prevent ViewPager2 from stealing horizontal scrolls
                rvCategories.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                    @Override
                    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull android.view.MotionEvent e) {
                        rv.getParent().requestDisallowInterceptTouchEvent(true);
                        return false;
                    }
                    @Override
                    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull android.view.MotionEvent e) {}
                    @Override
                    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
                });

                // Setup services
                ServiceAdapter serviceAdapter = new ServiceAdapter(requireContext(), new ArrayList<>(allServices), this);
                rvServices.setLayoutManager(new LinearLayoutManager(requireContext()));
                rvServices.setAdapter(serviceAdapter);
            });
        });
    }

    @Override
    public void onCategoryClick(Category category, int position) {
        selectedCategoryId = category.getId();
        categoryAdapter.setSelectedCategoryId(selectedCategoryId);

        List<Service> filtered = new ArrayList<>();
        if (category.getId() == 0 || "All".equalsIgnoreCase(category.getName())) {
            filtered.addAll(allServices);
        } else {
            for (Service service : allServices) {
                if (service.getCategoryId() == category.getId()) {
                    filtered.add(service);
                }
            }
        }

        // Recreate adapter with filtered list to force refresh
        ServiceAdapter newAdapter = new ServiceAdapter(requireContext(), filtered, this);
        rvServices.setAdapter(newAdapter);

        if (category.getId() != 0 && !"All".equalsIgnoreCase(category.getName())) {
            showSnack("Showing: " + category.getName());
        }
    }

    @Override
    public void onServiceClick(Service service) {
        if (!isAdded() || getContext() == null) return;
        Intent intent = new Intent(requireContext(), BookServiceActivity.class);
        intent.putExtra(KeyUtils.KEY_SERVICE_ID, service.getId());
        intent.putExtra(KeyUtils.KEY_SERVICE_NAME, service.getName());
        intent.putExtra(KeyUtils.KEY_SERVICE_PRICE, service.getPrice());
        startActivity(intent);
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
        rvCategories = view.findViewById(R.id.rvCategories);
        rvServices = view.findViewById(R.id.rvServices);
    }
}