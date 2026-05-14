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
        etGroupN    = findViewById(R.id.etGroupName);
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

        Map<String, Object> groupData = new HashMap<>();
        groupData.put("name", groupName);

        if (selectedImageUri != null) {
            groupData.put("picture_url", selectedImageUri.toString());
        }


        DocumentReference groupRef = db.collection("groups").document();

        groupRef.set(groupData)
                .addOnSuccessListener(unused -> {
                    btnSave.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                });

        Map<String, Object> groupLinker = new HashMap<>();
        groupLinker.put("group_id", groupRef.getId());
        groupLinker.put("user_id", user.getUid());

        DocumentReference groupLinkRef = db.collection("groups_to_users_link").document();

        groupLinkRef.set(groupLinker)
                .addOnSuccessListener(unused -> {
                    btnSave.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                });
        finish();
    }
}