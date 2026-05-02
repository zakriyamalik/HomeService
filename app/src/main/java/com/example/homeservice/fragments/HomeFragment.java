package com.example.homeservice.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener, ServiceAdapter.OnServiceClickListener {

    private RecyclerView rvCategories, rvServices;
    private CategoryAdapter categoryAdapter;
    private ServiceAdapter serviceAdapter;
    private LocalRepository repository;
    private List<Service> allServices = new ArrayList<>();
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
        loadCategoriesAndServices();
    }

    private void loadCategoriesAndServices() {
        executor.execute(() -> {
            List<Category> categories = repository.getAllCategories();
            List<Service> services = repository.getAllServices();
            allServices.clear();
            allServices.addAll(services);

            mainHandler.post(() -> {
                if (!isAdded() || getContext() == null) return;

                categoryAdapter = new CategoryAdapter(requireContext(), categories, this);
                rvCategories.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
                rvCategories.setAdapter(categoryAdapter);

                // ✅ Prevent ViewPager2 from stealing horizontal scrolls
                rvCategories.setOnTouchListener((v, event) -> {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_MOVE:
                            v.getParent().requestDisallowInterceptTouchEvent(true);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                    return false;
                });

                serviceAdapter = new ServiceAdapter(requireContext(), services, this);
                rvServices.setLayoutManager(new LinearLayoutManager(requireContext()));
                rvServices.setAdapter(serviceAdapter);
            });
        });
    }

    @Override
    public void onCategoryClick(Category category) {
        if (serviceAdapter == null) return;

        executor.execute(() -> {
            List<Service> filtered;
            if (category.getId() == 0) {
                filtered = new ArrayList<>(allServices);
            } else {
                filtered = repository.getServicesByCategory(category.getId());
            }
            mainHandler.post(() -> {
                if (!isAdded() || getContext() == null) return;
                serviceAdapter.updateList(filtered);
                Toast.makeText(getContext(), "Showing: " + category.getName(), Toast.LENGTH_SHORT).show();
            });
        });
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