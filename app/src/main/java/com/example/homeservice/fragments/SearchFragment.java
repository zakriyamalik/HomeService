package com.example.homeservice.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeservice.MyApplication;
import com.example.homeservice.R;
import com.example.homeservice.activities.BookServiceActivity;
import com.example.homeservice.adapters.ServiceAdapter;
import com.example.homeservice.database.LocalRepository;
import com.example.homeservice.models.Category;
import com.example.homeservice.models.Service;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchFragment extends Fragment implements ServiceAdapter.OnServiceClickListener {

    private EditText etSearch;
    private RecyclerView rvResults;
    private ImageButton btnFilter;
    private ServiceAdapter adapter;
    private List<Service> allServices = new ArrayList<>();
    private LocalRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private BottomSheetDialog filterDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        repository = new LocalRepository(MyApplication.getDatabaseHelper());

        adapter = new ServiceAdapter(requireContext(), allServices, this);
        rvResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvResults.setAdapter(adapter);

        loadAllServices();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                filterSearch(s.toString());
            }
        });

        btnFilter.setOnClickListener(v -> showFilterBottomSheet());
    }

    private void checkEmptyState(List<Service> results) {
        View emptyState = getView().findViewById(R.id.emptyStateSearch);
        RecyclerView rvResults = getView().findViewById(R.id.rvSearchResults);

        if (emptyState != null && rvResults != null) {
            if (results == null || results.isEmpty()) {
                rvResults.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
            } else {
                rvResults.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
            }
        }
    }
    private void loadAllServices() {
        executor.execute(() -> {
            List<Service> services = repository.getAllServices();
            mainHandler.post(() -> {
                if (!isAdded() || getContext() == null) return;
                allServices.clear();
                allServices.addAll(services);
                adapter.updateList(allServices);
                checkEmptyState(allServices);
            });
        });
    }

    private void filterSearch(String query) {
        if (adapter == null) return;

        List<Service> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Service service : allServices) {
            String name = service.getName();
            if (name != null && name.toLowerCase().contains(lowerQuery)) {
                filtered.add(service);
            }
        }
        adapter.updateList(filtered);
        checkEmptyState(filtered);
    }

    private void showFilterBottomSheet() {
        if (!isAdded() || getContext() == null) return;

        View sheetView = getLayoutInflater().inflate(R.layout.filter_bottom_sheet, null);
        filterDialog = new BottomSheetDialog(requireContext());
        filterDialog.setContentView(sheetView);

        Spinner spinnerCategory = sheetView.findViewById(R.id.spinnerCategory);
        EditText etMinPrice = sheetView.findViewById(R.id.etMinPrice);
        EditText etMaxPrice = sheetView.findViewById(R.id.etMaxPrice);
        Button btnApply = sheetView.findViewById(R.id.btnApplyFilter);
        Button btnReset = sheetView.findViewById(R.id.btnResetFilter);

        executor.execute(() -> {
            List<Category> categories = repository.getAllCategories();
            if (categories == null) categories = new ArrayList<>();

            List<String> categoryNames = new ArrayList<>();
            List<Integer> categoryIds = new ArrayList<>();
            categoryNames.add("All Categories");
            categoryIds.add(0);
            for (Category cat : categories) {
                categoryNames.add(cat.getName());
                categoryIds.add(cat.getId());
            }

            mainHandler.post(() -> {
                if (!isAdded() || getContext() == null) return;
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, categoryNames);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(spinnerAdapter);
                spinnerCategory.setTag(categoryIds);
            });
        });

        btnApply.setOnClickListener(v -> {
            try {
                int selectedPos = spinnerCategory.getSelectedItemPosition();
                @SuppressWarnings("unchecked")
                List<Integer> ids = (List<Integer>) spinnerCategory.getTag();
                Integer selectedCategoryId = (ids != null && selectedPos >= 0 && selectedPos < ids.size())
                        ? ids.get(selectedPos) : null;

                // Parse prices into FINAL variables for lambda capture
                final Double finalMinPrice;
                final Double finalMaxPrice;
                String minStr = etMinPrice.getText().toString().trim();
                String maxStr = etMaxPrice.getText().toString().trim();

                if (!minStr.isEmpty()) {
                    finalMinPrice = Double.parseDouble(minStr);
                } else {
                    finalMinPrice = null;
                }

                if (!maxStr.isEmpty()) {
                    finalMaxPrice = Double.parseDouble(maxStr);
                } else {
                    finalMaxPrice = null;
                }

                if (finalMinPrice != null && finalMaxPrice != null && finalMinPrice > finalMaxPrice) {
                    Toast.makeText(getContext(), "Min price cannot exceed max price", Toast.LENGTH_SHORT).show();
                    return;
                }

                executor.execute(() -> {
                    List<Service> filteredServices = repository.filterServices(selectedCategoryId, finalMinPrice, finalMaxPrice);
                    mainHandler.post(() -> {
                        if (!isAdded() || getContext() == null) return;
                        allServices.clear();
                        allServices.addAll(filteredServices);
                        adapter.updateList(allServices);
                        etSearch.setText("");
                        if (filterDialog != null && filterDialog.isShowing()) {
                            filterDialog.dismiss();
                        }
                        Toast.makeText(getContext(), "Filter applied", Toast.LENGTH_SHORT).show();
                    });
                });
            } catch (NumberFormatException e) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Invalid price value", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnReset.setOnClickListener(v -> {
            loadAllServices();
            etSearch.setText("");
            if (filterDialog != null && filterDialog.isShowing()) {
                filterDialog.dismiss();
            }
        });

        filterDialog.show();
    }

    @Override
    public void onServiceClick(Service service) {
        Intent intent = new Intent(requireContext(), BookServiceActivity.class);
        intent.putExtra("service_id", service.getId());
        intent.putExtra("service_name", service.getName());
        intent.putExtra("service_price", service.getPrice());
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (filterDialog != null && filterDialog.isShowing()) {
            filterDialog.dismiss();
        }
        executor.shutdown();
    }

    private void init(View view) {
        etSearch = view.findViewById(R.id.etSearch);
        rvResults = view.findViewById(R.id.rvSearchResults);
        btnFilter = view.findViewById(R.id.btnFilter);
    }
}