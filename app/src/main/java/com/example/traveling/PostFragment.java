package com.example.traveling;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostFragment extends Fragment {

    //views
    ImageView ivPostIm;
    EditText etDescription, etTag, etAddress, etGroup;
    ImageButton ibVoiceDesc, ibAiTag;
    TextView tvAddTag;
    ChipGroup chipGroupTags;
    CheckBox cbPublic, cbPrivate;
    AppCompatButton btnPost;
    View view;

    //State
    Uri selectedImageUri = null;
    List<String> tags = new ArrayList<>();

    //Firebase
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    //local database storage
    DataBaseHelper dbHelper;

    // image picker
    ActivityResultLauncher<Intent> imagePickerLauncher;


    public PostFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //launcher to open gallery
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        ivPostIm.setImageURI(selectedImageUri);
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_post, container, false);
        init();
        setListeners();
        return view;
    }


    private void init() {
        ivPostIm   = view.findViewById(R.id.ivPostImage);
        etDescription = view.findViewById(R.id.etDescription);
        etTag         = view.findViewById(R.id.etTag);
        etAddress     = view.findViewById(R.id.etAddress);
        etGroup       = view.findViewById(R.id.etGroup);
        ibVoiceDesc  = view.findViewById(R.id.btnVoiceDesc);
        ibAiTag      = view.findViewById(R.id.btnAiTag);
        tvAddTag      = view.findViewById(R.id.tvAddTag);
        chipGroupTags = view.findViewById(R.id.chipGroupTags);
        cbPublic      = view.findViewById(R.id.cbPublic);
        cbPrivate     = view.findViewById(R.id.cbPrivate);
        btnPost       = view.findViewById(R.id.btnPost);

        mAuth   = FirebaseAuth.getInstance();
        db      = FirebaseFirestore.getInstance();

        dbHelper = new DataBaseHelper(getContext());
    }
    private void setListeners() {
        ivPostIm.setOnClickListener(v -> openImagePicker());

        tvAddTag.setOnClickListener(v -> addTagFromInput());
        etTag.setOnEditorActionListener((v, actionId, event) -> {
            addTagFromInput();
            return true;
        });

        ibAiTag.setOnClickListener(v ->
                Toast.makeText(getContext(), "AI tag suggestion coming soon", Toast.LENGTH_SHORT).show());
        ibVoiceDesc.setOnClickListener(v ->
                Toast.makeText(getContext(), "Voice input coming soon", Toast.LENGTH_SHORT).show());

        cbPublic.setOnCheckedChangeListener((btn, checked) -> { if (checked) cbPrivate.setChecked(false); });
        cbPrivate.setOnCheckedChangeListener((btn, checked) -> { if (checked) cbPublic.setChecked(false); });

        btnPost.setOnClickListener(v -> handlePost());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }


    /*============================TAG MANAGEMENT=================================*/
    private void addTagFromInput() {
        String tag = etTag.getText().toString().trim();
        if (TextUtils.isEmpty(tag)) return;
        if (tags.contains(tag)) {
            etTag.setText("");
            return;
        }
        tags.add(tag);
        addChip(tag);
        etTag.setText("");
    }

    private void addChip(String label) {
        Chip chip = new Chip(requireContext());
        chip.setText(label);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            tags.remove(label);
            chipGroupTags.removeView(chip);
        });
        chipGroupTags.addView(chip);
    }

    /*============================POST MANAGEMENT=================================*/

    private void handlePost() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), getString(R.string.login_required), Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), getString(R.string.select_image), Toast.LENGTH_SHORT).show();
            return;
        }

        btnPost.setEnabled(false);

        String description = etDescription.getText().toString().trim();
        String address     = etAddress.getText().toString().trim();
        String group       = etGroup.getText().toString().trim();
        boolean isPublic   = cbPublic.isChecked();

        //everything except the image goes to Firestore
        Map<String, Object> post = new HashMap<>();
        post.put("authorId",            user.getUid());
        post.put("description",         description);
        post.put("tags",                new ArrayList<>(tags));
        post.put("address",             address);
        post.put("group",               TextUtils.isEmpty(group) ? null : group);
        post.put("isPublic",            isPublic);
        post.put("isAnonymous",         user.isAnonymous());
        post.put("likes",               0);
        post.put("imageStoredLocally",  true); // image in SQLite
        post.put("timestamp",           Timestamp.now());

        db.collection("posts")
                .add(post)
                .addOnSuccessListener(docRef -> {
                    //save image URI locally linked to the Firestore document
                    dbHelper.insertPost(docRef.getId(), selectedImageUri.toString());
                    Toast.makeText(getContext(), getString(R.string.post_success), Toast.LENGTH_SHORT).show();
                    resetForm();
                })
                .addOnFailureListener(e -> {
                    btnPost.setEnabled(true);
                    Toast.makeText(getContext(),
                            getString(R.string.post_failed) + ": " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    /*============================RESET=================================*/

    private void resetForm() {
        selectedImageUri = null;
        ivPostIm.setImageResource(R.drawable.post_frame);
        etDescription.setText("");
        etTag.setText("");
        etAddress.setText("");
        etGroup.setText("");
        tags.clear();
        chipGroupTags.removeAllViews();
        cbPublic.setChecked(true);
        cbPrivate.setChecked(false);
        btnPost.setEnabled(true);
    }

}