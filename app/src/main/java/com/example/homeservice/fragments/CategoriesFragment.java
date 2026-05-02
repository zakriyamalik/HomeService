package com.example.homeservice.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeservice.MyApplication;
import com.example.homeservice.R;
import com.example.homeservice.adapters.CategoryAdapter;
import com.example.homeservice.database.LocalRepository;
import com.example.homeservice.models.Category;

import java.util.List;

public class CategoriesFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener {

    private RecyclerView rvCategories;
    private CategoryAdapter adapter;
    private LocalRepository repository;

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
        List<Category> categories = repository.getAllCategories();

        adapter = new CategoryAdapter(requireContext(), categories, this);
        rvCategories.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvCategories.setAdapter(adapter);
    }

    private void init(View view) {
        rvCategories = view.findViewById(R.id.rvCategoriesGrid);
    }

    @Override
    public void onCategoryClick(Category category) {
        Toast.makeText(requireContext(), "Category: " + category.getName(), Toast.LENGTH_SHORT).show();
    }
}