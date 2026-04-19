package com.example.homeservice.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.example.homeservice.models.Category;
import com.example.homeservice.models.Service;
import com.example.homeservice.utils.KeyUtils;

public class HomeFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener, ServiceAdapter.OnServiceClickListener {

    private RecyclerView rvCategories, rvServices;
    private CategoryAdapter categoryAdapter;
    private ServiceAdapter serviceAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);

        // Categories - Horizontal
        categoryAdapter = new CategoryAdapter(requireContext(), MyApplication.categories, this);
        rvCategories.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        // Services - Vertical (fixed adapter uses copy of global list)
        serviceAdapter = new ServiceAdapter(requireContext(), MyApplication.services, this);
        rvServices.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvServices.setAdapter(serviceAdapter);
    }

    private void init(View view) {
        rvCategories = view.findViewById(R.id.rvCategories);
        rvServices = view.findViewById(R.id.rvServices);
    }

    @Override
    public void onCategoryClick(Category category) {
        if (category.getId() == 0) { // "All" category
            serviceAdapter.resetFilter();
        } else {
            serviceAdapter.filterByCategory(category.getId());
        }
        Toast.makeText(requireContext(), "Showing: " + category.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onServiceClick(Service service) {
        Intent intent = new Intent(requireContext(), BookServiceActivity.class);
        intent.putExtra(KeyUtils.KEY_SERVICE_ID, service.getId());
        intent.putExtra(KeyUtils.KEY_SERVICE_NAME, service.getName());
        intent.putExtra(KeyUtils.KEY_SERVICE_PRICE, service.getPrice());
        startActivity(intent);
    }
}