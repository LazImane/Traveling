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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class GroupActivity extends AppCompatActivity {

    ImageButton btnBack, btnEditP;
    EditText etGroupN;
    AppCompatButton btnSave, btnSignOut;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseUser user;
    Uri selectedImageUri = null;

    ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_creator);
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        btnEditP.setImageURI(uri);
                    }
                });
        init();
        setListeners();
    }

    private void init() {
        btnBack    = findViewById(R.id.btnBack);
        btnEditP   = findViewById(R.id.btnEditPicture);
        etGroupN   = findViewById(R.id.etGroupName);
        btnSave    = findViewById(R.id.btnSave);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();
        user  = mAuth.getCurrentUser();

    }
    private void setListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnEditP.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnSave.setOnClickListener(v -> saveGroup());
    }
    private void saveGroup() {
        if (user == null) return;

        String groupName = etGroupN.getText().toString().trim();
        if (TextUtils.isEmpty(groupName)) {
            Toast.makeText(this, getString(R.string.userNH), Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);

        DocumentReference groupRef = db.collection("groups").document();

        if (selectedImageUri != null) {
            // 1. Upload image to Firebase Storage
            String fileName = user.getUid() + "_" + System.currentTimeMillis() + ".jpg";
            StorageReference imageRef = FirebaseStorage.getInstance()
                    .getReference("group_images/" + groupRef.getId() + "/" + fileName);

            imageRef.putFile(selectedImageUri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) throw task.getException();
                        // 2. Get the public download URL
                        return imageRef.getDownloadUrl();
                    })
                    .addOnSuccessListener(downloadUri -> {
                        // 3. Save group with real image URL
                        saveGroupToFirestore(groupRef, groupName, downloadUri.toString());
                    })
                    .addOnFailureListener(e -> {
                        btnSave.setEnabled(true);
                        Toast.makeText(this, "Image upload failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        } else {
            // No image selected — save without one
            saveGroupToFirestore(groupRef, groupName, null);
        }
    }

    private void saveGroupToFirestore(DocumentReference groupRef,
                                      String groupName,
                                      String photoUrl) {
        Map<String, Object> groupData = new HashMap<>();
        groupData.put("name",     groupName);
        groupData.put("photoUrl", photoUrl != null ? photoUrl : "");

        groupRef.set(groupData)
                .addOnSuccessListener(unused -> {
                    // 4. Only link the user AFTER the group doc exists
                    linkUserToGroup(groupRef.getId());
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Failed to create group: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void linkUserToGroup(String groupId) {
        Map<String, Object> groupLinker = new HashMap<>();
        groupLinker.put("group_id", groupId);
        groupLinker.put("user_id",  user.getUid());

        db.collection("groups_to_users_link").document()
                .set(groupLinker)
                .addOnSuccessListener(unused -> finish())
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Failed to join group: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}