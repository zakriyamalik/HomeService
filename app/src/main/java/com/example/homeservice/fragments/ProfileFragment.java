package com.example.homeservice.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.homeservice.R;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE = 100;
    private ImageView ivProfilePic;
    private Button btnUploadPic;
    private TextView tvUserName, tvUserPhone;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadProfilePic();
        loadUserInfo();

        btnUploadPic.setOnClickListener(v -> pickImage());
        ivProfilePic.setOnClickListener(v -> pickImage());
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
            File file = new File(getActivity().getFilesDir(), "profile_pic.jpg");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProfilePic() {
        File file = new File(getActivity().getFilesDir(), "profile_pic.jpg");
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            ivProfilePic.setImageBitmap(bitmap);
        }
    }

    private void loadUserInfo() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("USER", android.content.Context.MODE_PRIVATE);
        String phone = prefs.getString("user_phone", "Unknown");
        tvUserPhone.setText(phone);
        tvUserName.setText("User " + userId.substring(0, 6));
    }

    private void init(View view) {
        ivProfilePic = view.findViewById(R.id.ivProfilePic);
        btnUploadPic = view.findViewById(R.id.btnUploadPic);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserPhone = view.findViewById(R.id.tvUserPhone);
    }
}