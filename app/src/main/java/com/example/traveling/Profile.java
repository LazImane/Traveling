package com.example.traveling;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class Profile extends AppCompatActivity {

    ImageButton btnBack, btnEditP;
    ImageView ivPfp;
    EditText etUserN;
    AppCompatButton btnSave, btnSignOut;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseUser user;
    FirebaseStorage storage;
    Uri selectedImageUri = null;

    ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        Glide.with(Profile.this)
                                .load(uri)
                                .placeholder(R.drawable.post_frame)
                                .into(ivPfp);
                    }
                });
        init();
        loadUserData();
        setListeners();
    }

    private void init() {
        btnBack    = findViewById(R.id.btnBack);
        btnEditP   = findViewById(R.id.btnEditPicture);
        ivPfp      = findViewById(R.id.ivProfilePic);
        etUserN    = findViewById(R.id.etUsername);
        btnSave    = findViewById(R.id.btnSave);
        btnSignOut = findViewById(R.id.btnSignOut);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();
        user  = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
    }
    private void loadUserData() {
        if (user == null) return;

        //load username from Firestore
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String username = doc.getString("username");
                        if (!TextUtils.isEmpty(username)) {
                            etUserN.setText(username);
                        }
                        String photoUrl = doc.getString("photoUrl");
                        if (!TextUtils.isEmpty(photoUrl) && selectedImageUri == null) {

                            Glide.with(Profile.this)
                                    .load(photoUrl)
                                    .placeholder(R.drawable.post_frame)
                                    .error(R.drawable.post_frame)
                                    .into(ivPfp);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, getString(R.string.load_profile_failed), Toast.LENGTH_SHORT).show()
                );
    }
    private void setListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnEditP.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnSave.setOnClickListener(v -> saveProfile());
        btnSignOut.setOnClickListener(v -> signOut());
    }
    private void saveProfile() {
        if (user == null) return;

        String username = etUserN.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, getString(R.string.userNH), Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", username);

        if (selectedImageUri != null) {

            StorageReference storageRef = storage.getReference()
                    .child("profile_pictures")
                    .child(user.getUid() + ".jpg");

            storageRef.putFile(selectedImageUri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return storageRef.getDownloadUrl();
                    })
                    .addOnSuccessListener(uri -> {

                        updates.put("photoUrl", uri.toString());

                        DocumentReference userRef =
                                db.collection("users").document(user.getUid());

                        userRef.update(updates)
                                .addOnSuccessListener(unused -> {
                                    btnSave.setEnabled(true);
                                    Toast.makeText(this,
                                            getString(R.string.saveChanges),
                                            Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    btnSave.setEnabled(true);
                                    Toast.makeText(this,
                                            getString(R.string.saveFail),
                                            Toast.LENGTH_SHORT).show();
                                });

                    })
                    .addOnFailureListener(e -> {
                        btnSave.setEnabled(true);
                        Toast.makeText(this,
                                "Image upload failed",
                                Toast.LENGTH_SHORT).show();
                    });

            return;
        }

        DocumentReference userRef = db.collection("users").document(user.getUid());
        userRef.update(updates)
                .addOnSuccessListener(unused -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, getString(R.string.saveChanges), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, getString(R.string.saveFail), Toast.LENGTH_SHORT).show();
                });
    }
    private void signOut() {
        mAuth.signOut();
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}