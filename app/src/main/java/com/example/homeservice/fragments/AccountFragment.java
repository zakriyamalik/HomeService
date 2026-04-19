package com.example.homeservice.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.homeservice.R;
import com.example.homeservice.activities.LoginSignupChoiceActivity;
import com.example.homeservice.utils.KeyUtils;

public class AccountFragment extends Fragment {

    private TextView tvUsername, tvEmail;
    private Button btnLogout, btnCall, btnMap, btnWebsite;
    private SharedPreferences userPrefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);

        userPrefs = requireActivity().getSharedPreferences("USER", android.content.Context.MODE_PRIVATE);
        String name = userPrefs.getString(KeyUtils.KEY_NAME, "User");
        String email = userPrefs.getString(KeyUtils.KEY_EMAIL, name + "@example.com");

        tvUsername.setText(name);
        tvEmail.setText(email);

        btnLogout.setOnClickListener(v -> logout());
        btnCall.setOnClickListener(v -> makeCall());
        btnMap.setOnClickListener(v -> openMap());
        btnWebsite.setOnClickListener(v -> openWebsite());
    }

    private void logout() {
        userPrefs.edit().putBoolean(KeyUtils.KEY_IS_LOGIN, false).apply();
        Intent intent = new Intent(requireActivity(), LoginSignupChoiceActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void makeCall() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:+1234567890"));
        startActivity(intent);
    }

    private void openMap() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("google.navigation:q=Home+Service+Headquarters"));
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(requireContext(), "No map app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void openWebsite() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.homeservice.com"));
        startActivity(intent);
    }

    private void init(View view) {
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnCall = view.findViewById(R.id.btnCall);
        btnMap = view.findViewById(R.id.btnMap);
        btnWebsite = view.findViewById(R.id.btnWebsite);
    }
}