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
import com.example.homeservice.models.Category;

public class CategoriesFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener {

    private RecyclerView rvCategories;
    private CategoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);

        // GridLayoutManager with 2 columns (syllabus Lecture 7 pattern)
        adapter = new CategoryAdapter(requireContext(), MyApplication.categories, this);
        rvCategories.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvCategories.setAdapter(adapter);
    }

    private void init(View view) {
        rvCategories = view.findViewById(R.id.rvCategoriesGrid);
    }

    @Override
    public void onCategoryClick(Category category) {
        // For now, just show a Toast. Later we can open a filtered services screen.
        Toast.makeText(requireContext(), "Category: " + category.getName(), Toast.LENGTH_SHORT).show();
    }
}