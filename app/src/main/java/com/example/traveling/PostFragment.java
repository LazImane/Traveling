package com.example.traveling;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class PostFragment extends Fragment {

    //views
    ImageView ivPostIm;
    EditText etTitle, etDescription, etTag, etAddress;
    AutoCompleteTextView etGroup;
    ImageButton ibVoiceDesc, ibAiTag;
    TextView tvAddTag;
    ChipGroup chipGroupTags;
    CheckBox cbPublic, cbPrivate;
    AppCompatButton btnPost;
    View view;

    MainActivity mainActivity;

    //State
    Uri selectedImageUri = null;
    List<String> tags = new ArrayList<>();


    // image picker
    ActivityResultLauncher<Intent> imagePickerLauncher;


    public PostFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity)getContext();
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
        loadGroupSuggestions();
        return view;
    }


    private void init() {
        ivPostIm        = view.findViewById(R.id.ivPostImage);
        etTitle         = view.findViewById(R.id.etTitle);
        etDescription   = view.findViewById(R.id.etDescription);
        etTag           = view.findViewById(R.id.etTag);
        etAddress       = view.findViewById(R.id.etAddress);
        etGroup         = view.findViewById(R.id.etGroup);
        ibVoiceDesc     = view.findViewById(R.id.btnVoiceDesc);
        ibAiTag         = view.findViewById(R.id.btnAiTag);
        tvAddTag        = view.findViewById(R.id.tvAddTag);
        chipGroupTags   = view.findViewById(R.id.chipGroupTags);
        cbPublic        = view.findViewById(R.id.cbPublic);
        cbPrivate       = view.findViewById(R.id.cbPrivate);
        btnPost         = view.findViewById(R.id.btnPost);

    }
    private void setListeners() {
        ivPostIm.setOnClickListener(v -> openImagePicker());

        tvAddTag.setOnClickListener(v -> addTagFromInput());
        etTag.setOnEditorActionListener((v, actionId, event) -> {
            addTagFromInput();
            return true;
        });

        ibAiTag.setOnClickListener(v ->
                Toast.makeText(getContext(), getString(R.string.ai_soon), Toast.LENGTH_SHORT).show());
        ibVoiceDesc.setOnClickListener(v ->
                Toast.makeText(getContext(), getString(R.string.voice_soon) , Toast.LENGTH_SHORT).show());

        cbPublic.setOnClickListener(v -> {
            cbPublic.setChecked(true);
            cbPrivate.setChecked(false);
        });

        cbPrivate.setOnClickListener(v -> {

            // cannot make private without group
            if (TextUtils.isEmpty(etGroup.getText().toString().trim())) {

                Toast.makeText(
                        getContext(),
                        getString(R.string.private_post_group),
                        Toast.LENGTH_SHORT
                ).show();

                cbPrivate.setChecked(false);
                cbPublic.setChecked(true);

                return;
            }

            cbPrivate.setChecked(true);
            cbPublic.setChecked(false);
        });

//        etGroup.setOnFocusChangeListener((v, hasFocus) -> {
//
//            if (!hasFocus) {
//
//                String group = etGroup.getText().toString().trim();
//
//                if (!TextUtils.isEmpty(group)) {
//                    cbPrivate.setChecked(true);
//                    cbPublic.setChecked(false);
//                }
//            }
//        });

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
        FirebaseUser user = mainActivity.mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), getString(R.string.login_required), Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), getString(R.string.select_image), Toast.LENGTH_SHORT).show();
            return;
        }

        btnPost.setEnabled(false);

        // 1. Build the Storage path: post_images/<userId>/<timestamp>.jpg
        String fileName = user.getUid() + "_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = FirebaseStorage.getInstance()
                .getReference("post_images/" + user.getUid() + "/" + fileName);

        // 2. Upload the image bytes to Firebase Storage
        imageRef.putFile(selectedImageUri)
                .addOnFailureListener(e -> {
                    btnPost.setEnabled(true);
                    Toast.makeText(getContext(),
                            getString(R.string.group_image_upload_failed) + e.getMessage(), Toast.LENGTH_LONG).show();
                })
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return imageRef.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> {
                    savePostToFirestore(user, downloadUri.toString());
                })
                .addOnFailureListener(e -> {
                    btnPost.setEnabled(true);
                    Toast.makeText(getContext(),
                            getString(R.string.url_cant) + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void savePostToFirestore(FirebaseUser user, String imageUrl) {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String address     = etAddress.getText().toString().trim();
        String groupName = etGroup.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            btnPost.setEnabled(true);
            Toast.makeText(getContext(), getString(R.string.title_required), Toast.LENGTH_SHORT).show();
            return;
        }

        // If no group -> public post
        if (TextUtils.isEmpty(groupName)) {
            createPost(user, imageUrl, null, null,true);
            return;
        }

        // Group exists -> fetch its ID
        mainActivity.db.collection("groups")
                .whereEqualTo("name", groupName)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (queryDocumentSnapshots.isEmpty()) {

                        btnPost.setEnabled(true);

                        Toast.makeText(
                                getContext(),
                                getString(R.string.group_not_exist),
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    String groupId = queryDocumentSnapshots
                            .getDocuments()
                            .get(0)
                            .getId();
                    boolean isPublic = cbPublic.isChecked();
                    createPost(user, imageUrl, groupId, groupName,isPublic);
                })
                .addOnFailureListener(e -> {

                    btnPost.setEnabled(true);

                    Toast.makeText(
                            getContext(),
                            getString(R.string.group_validate_failed),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void createPost(
            FirebaseUser user,
            String imageUrl,
            String groupId,
            String groupName,
            boolean isPublic
    ) {

        String title       = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String address     = etAddress.getText().toString().trim();

        Map<String, Object> post = new HashMap<>();

        post.put("authorId", user.getUid());
        post.put("title", title);
        post.put("description", description);
        post.put("tags", new ArrayList<>(tags));
        post.put("address", address);

        // null if public post
        post.put("groupId", groupId);
        post.put("groupName", groupName);

        post.put("isPublic", isPublic);
        post.put("isAnonymous", user.isAnonymous());
        post.put("likes", 0);
        post.put("imageUrl", imageUrl);
        post.put("timestamp", Timestamp.now());

        if (!address.isEmpty()) {
            new Thread(() -> {
                try {
                    Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                    List<Address> results = geocoder.getFromLocationName(address, 1);
                    if (results != null && !results.isEmpty()) {
                        post.put("latitude",  results.get(0).getLatitude());
                        post.put("longitude", results.get(0).getLongitude());
                    }
                } catch (Exception ignored) {
                    // Geocoding failed
                }
                requireActivity().runOnUiThread(() -> saveToFirestore(post, groupId, user));
            }).start();
        } else {
            saveToFirestore(post, groupId, user);
        }
    }

    private void saveToFirestore(Map<String, Object> post, String groupId, FirebaseUser user) {
        mainActivity.db.collection("posts")
                .add(post)
                .addOnSuccessListener(docRef -> {
                    if (groupId != null && !groupId.isEmpty()) {
                        notifyGroupMembers(docRef.getId(),
                                (String) post.get("description"), groupId, user.getUid());
                    }
                    Toast.makeText(getContext(),
                            getString(R.string.post_success), Toast.LENGTH_SHORT).show();
                    resetForm();
                })
                .addOnFailureListener(e -> {
                    btnPost.setEnabled(true);
                    Toast.makeText(getContext(),
                            getString(R.string.post_failed) + ": " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void notifyGroupMembers(String postId, String description,
                                    String groupId, String authorId) {
        mainActivity.db.collection("groups_to_users_link")
                .whereEqualTo("group_id", groupId)
                .get()
                .addOnSuccessListener(qs -> {
                    for (DocumentSnapshot link : qs) {
                        String targetUid = link.getString("user_id");
                        // Don't notify the author
                        if (targetUid != null && !targetUid.equals(authorId)) {
                            Map<String, Object> notif = new HashMap<>();
                            notif.put("userId",    targetUid);
                            notif.put("type",      "new_post_group");
                            notif.put("message",   description != null && !description.isEmpty()
                                    ? description
                                    : getString(R.string.new_post_in_your_group));
                            notif.put("postId",    postId);
                            notif.put("groupId",   groupId);
                            notif.put("read",      false);
                            notif.put("timestamp", com.google.firebase.Timestamp.now());
                            mainActivity.db.collection("notifications")
                                    .document()
                                    .set(notif);
                        }
                    }
                });
    }

    /*================AUTOCOMPLETE=======================================*/

    private void loadGroupSuggestions() {
        mainActivity.db.collection("groups")
                .get()
                .addOnSuccessListener(qs -> {
                    List<String> groupNames = new ArrayList<>();
                    for (DocumentSnapshot doc : qs) {
                        String name = doc.getString("name");
                        if (name != null) groupNames.add(name);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            groupNames
                    ) {
                        @Override
                        public android.widget.Filter getFilter() {
                            return new android.widget.Filter() {
                                @Override
                                protected FilterResults performFiltering(CharSequence constraint) {
                                    FilterResults results = new FilterResults();
                                    if (constraint == null || constraint.length() == 0) {
                                        results.values = groupNames;
                                        results.count  = groupNames.size();
                                    } else {
                                        String query = constraint.toString().toLowerCase();
                                        List<String> filtered = new ArrayList<>();
                                        for (String name : groupNames) {
                                            if (name.toLowerCase().contains(query)) {
                                                filtered.add(name);
                                            }
                                        }
                                        results.values = filtered;
                                        results.count  = filtered.size();
                                    }
                                    return results;
                                }

                                @Override
                                protected void publishResults(CharSequence constraint,
                                                              FilterResults results) {
                                    clear();
                                    //noinspection unchecked
                                    addAll((List<String>) results.values);
                                    if (results.count > 0) notifyDataSetChanged();
                                    else notifyDataSetInvalidated();
                                }

                                @Override
                                public CharSequence convertResultToString(Object resultValue) {
                                    return (String) resultValue;
                                }
                            };
                        }
                    };

                    etGroup.setAdapter(adapter);
                })
                .addOnFailureListener(e ->
                        android.util.Log.e("PostFragment", getString(R.string.group_load_failed) + e.getMessage()));
    }

    /*============================RESET=================================*/

    private void resetForm() {
        selectedImageUri = null;
        ivPostIm.setImageResource(R.drawable.post_frame);
        etTitle.setText("");
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