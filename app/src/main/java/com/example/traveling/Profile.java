package com.example.traveling;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class Profile extends AppCompatActivity {

    ImageButton btnBack, btnEditP;
    ImageView ivPfp;
    EditText etUserN, etEmail;
    AppCompatButton btnSave, btnSignOut;

    FirebaseAuth mAuth;
    FirebaseUser user;
    Uri selectedImageUri = null;

    ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivPfp.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        init();
        loadUserData();
        setListeners();
    }

    private void init() {
        //access to ui design
        btnBack        = findViewById(R.id.btnBack);
        btnEditP = findViewById(R.id.btnEditPicture);
        ivPfp   = findViewById(R.id.ivProfilePic);
        etUserN    = findViewById(R.id.etUsername);
        etEmail        = findViewById(R.id.etEmail);
        btnSave        = findViewById(R.id.btnSave);
        btnSignOut     = findViewById(R.id.btnSignOut);
        //authentification
        mAuth          = FirebaseAuth.getInstance();
        user           = mAuth.getCurrentUser();
    }

    private void loadUserData() {
        if (user == null) return;
        etEmail.setText(user.getEmail());
        if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            etUserN.setText(user.getDisplayName());
        }
        if (user.getPhotoUrl() != null) {
            ivPfp.setImageURI(user.getPhotoUrl());
        }
    }

    private void setListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnEditP.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnSave.setOnClickListener(v -> saveProfile());
        btnSignOut.setOnClickListener(v -> signOut());
    }

    private void saveProfile() {
        String username = etUserN.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, getString(R.string.userNH), Toast.LENGTH_SHORT).show();
            return;
        }

        UserProfileChangeRequest.Builder profileUpdate = new UserProfileChangeRequest.Builder()
                .setDisplayName(username);

        if (selectedImageUri != null) {
            profileUpdate.setPhotoUri(selectedImageUri);
        }

        user.updateProfile(profileUpdate.build())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, getString(R.string.saveChanges), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, getString(R.string.saveFail), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signOut() {
        mAuth.signOut();
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}