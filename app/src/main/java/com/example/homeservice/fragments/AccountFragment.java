package com.example.homeservice.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.homeservice.MyApplication;
import com.example.homeservice.R;
import com.example.homeservice.activities.LoginSignupChoiceActivity;
import com.example.homeservice.database.LocalRepository;
import com.example.homeservice.models.User;
import com.example.homeservice.utils.KeyUtils;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class AccountFragment extends Fragment {

    private static final int PICK_IMAGE = 100;
    private ImageView ivProfilePic;
    private Button btnUploadPic, btnLogout, btnCall, btnMap, btnWebsite, btnAddPhone;
    private TextView tvUsername, tvEmail, tvUserPhone;
    private SharedPreferences userPrefs;
    private LocalRepository repository;
    private String userId;
    private String userPhoneNumber = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);

        repository = new LocalRepository(MyApplication.getDatabaseHelper());

        SwitchMaterial switchDarkMode = view.findViewById(R.id.switchDarkMode);
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        switchDarkMode.setChecked(currentMode == AppCompatDelegate.MODE_NIGHT_YES);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            requireActivity().getSharedPreferences("APP", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("dark_mode", isChecked)
                    .apply();
        });

        userPrefs = requireActivity().getSharedPreferences("USER", Context.MODE_PRIVATE);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(), "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            logout();
            return;
        }
        userId = currentUser.getUid();

        loadUserInfo();
        loadProfilePic();

        ivProfilePic.setOnClickListener(v -> pickImage());
        btnUploadPic.setOnClickListener(v -> pickImage());
        btnLogout.setOnClickListener(v -> logout());
        btnCall.setOnClickListener(v -> makeCall());
        btnMap.setOnClickListener(v -> openMap());
        btnWebsite.setOnClickListener(v -> openWebsite());
        btnAddPhone.setOnClickListener(v -> showAddPhoneDialog());
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == getActivity().RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                ivProfilePic.setImageBitmap(bitmap);
                saveImageLocally(bitmap);
                Toast.makeText(getContext(), "Photo saved", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveImageLocally(Bitmap bitmap) {
        try {
            File file = new File(getActivity().getFilesDir(), "profile_pic_" + userId + ".jpg");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProfilePic() {
        File file = new File(getActivity().getFilesDir(), "profile_pic_" + userId + ".jpg");
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            ivProfilePic.setImageBitmap(bitmap);
        }
    }

    private void loadUserInfo() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String name = userPrefs.getString(KeyUtils.KEY_NAME, "User");
        String email = userPrefs.getString(KeyUtils.KEY_EMAIL, "");

        if (firebaseUser != null && firebaseUser.getPhoneNumber() != null && !firebaseUser.getPhoneNumber().isEmpty()) {
            userPhoneNumber = firebaseUser.getPhoneNumber();
        } else {
            userPhoneNumber = userPrefs.getString("user_phone", "");
        }

        if (userPhoneNumber.isEmpty()) {
            User dbUser = repository.getUserByUid(userId);
            if (dbUser != null) {
                userPhoneNumber = dbUser.getPhone();
                if (name.equals("User") && !dbUser.getName().isEmpty()) name = dbUser.getName();
                if (email.isEmpty() && !dbUser.getEmail().isEmpty()) email = dbUser.getEmail();
            }
        }

        tvUsername.setText(name);
        tvEmail.setText(email.isEmpty() ? "No email" : email);

        if (userPhoneNumber.isEmpty() || userPhoneNumber.equals("Unknown")) {
            tvUserPhone.setText("No phone number");
            btnAddPhone.setVisibility(View.VISIBLE);
            btnCall.setEnabled(false);
            btnCall.setAlpha(0.5f);
        } else {
            tvUserPhone.setText(userPhoneNumber);
            btnAddPhone.setVisibility(View.GONE);
            btnCall.setEnabled(true);
            btnCall.setAlpha(1.0f);
        }
    }

    private void showAddPhoneDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_phone, null);
        TextInputEditText etPhone = dialogView.findViewById(R.id.etPhone);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Phone Number")
                .setMessage("Enter your phone number")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String phone = etPhone.getText().toString().trim();
                    if (!phone.isEmpty()) {
                        repository.updateUserPhone(userId, phone);
                        userPrefs.edit().putString("user_phone", phone).apply();
                        userPhoneNumber = phone;
                        loadUserInfo();
                        Toast.makeText(getContext(), "Phone number added", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logout() {
        userPrefs.edit().putBoolean(KeyUtils.KEY_IS_LOGIN, false).apply();
        Intent intent = new Intent(requireActivity(), LoginSignupChoiceActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void makeCall() {
        if (userPhoneNumber.isEmpty()) {
            Toast.makeText(getContext(), "No phone number available", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + userPhoneNumber));
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
        ivProfilePic = view.findViewById(R.id.ivProfilePic);
        btnUploadPic = view.findViewById(R.id.btnUploadPic);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvUserPhone = view.findViewById(R.id.tvUserPhone);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnCall = view.findViewById(R.id.btnCall);
        btnMap = view.findViewById(R.id.btnMap);
        btnWebsite = view.findViewById(R.id.btnWebsite);
        btnAddPhone = view.findViewById(R.id.btnAddPhone);
    }
}