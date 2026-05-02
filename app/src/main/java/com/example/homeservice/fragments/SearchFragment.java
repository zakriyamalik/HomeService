package com.example.homeservice.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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
import com.example.homeservice.models.Service;
import com.example.homeservice.utils.KeyUtils;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements ServiceAdapter.OnServiceClickListener {

    private EditText etSearch;
    private RecyclerView rvResults;
    private ServiceAdapter adapter;
    private List<Service> allServices;
    private LocalRepository repository;

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
        loadAllServices();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });
    }

    private void loadAllServices() {
        allServices = repository.getAllServices();
        adapter = new ServiceAdapter(requireContext(), allServices, this);
        rvResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvResults.setAdapter(adapter);
    }

    private void filter(String query) {
        List<Service> filtered = new ArrayList<>();
        for (Service service : allServices) {
            if (service.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(service);
            }
        }
        adapter.updateList(filtered);
    }

    @Override
    public void onServiceClick(Service service) {
        Intent intent = new Intent(requireContext(), BookServiceActivity.class);
        intent.putExtra(KeyUtils.KEY_SERVICE_ID, service.getId());
        intent.putExtra(KeyUtils.KEY_SERVICE_NAME, service.getName());
        intent.putExtra(KeyUtils.KEY_SERVICE_PRICE, service.getPrice());
        startActivity(intent);
    }

    private void init(View view) {
        etSearch = view.findViewById(R.id.etSearch);
        rvResults = view.findViewById(R.id.rvSearchResults);
    }
}