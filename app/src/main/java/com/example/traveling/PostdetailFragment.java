package com.example.traveling;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**TODO : COMMENTS + SIGNALER + FIX LIKES**/
public class PostdetailFragment extends Fragment {

    private static final String ARG_POST_ID = "post_id";

    private String postId;
    private MainActivity mainActivity;
    private PostItem post;

    // Keep a reference to the inflated view to avoid 'Cannot resolve symbol view'
    private View rootView;

    // Views
    private ImageView ivDetailImage, ivAuthorPhoto, ivGroupPhoto;
    private TextView tvDetailTitle, tvDetailAddress, tvDetailDate,
            tvDetailLocation, tvDetailAuthor, tvDetailGroup, tvDetailNarrative;
    private ChipGroup chipGroupDetailTags;
    private ImageView btnBack, btnLike;
    private TextView tvLikeCount;
    private LinearLayout rowLocation, rowGroup;

    public PostdetailFragment() {}

    public static PostdetailFragment newInstance(PostItem post) {
        PostdetailFragment f = new PostdetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_POST_ID, post.getFirestoreId());
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getContext();
        if (getArguments() != null) {
            postId = getArguments().getString(ARG_POST_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_post_detail, container, false);
        initViews();
        loadPost();
        return rootView;
    }

    private void initViews() {
        ivDetailImage       = rootView.findViewById(R.id.ivDetailImage);
        ivAuthorPhoto       = rootView.findViewById(R.id.ivAuthorPhoto);
        ivGroupPhoto        = rootView.findViewById(R.id.ivGroupPhoto);
        tvDetailTitle       = rootView.findViewById(R.id.tvDetailTitle);
        tvDetailAddress     = rootView.findViewById(R.id.tvDetailAddress);
        tvDetailDate        = rootView.findViewById(R.id.tvDetailDate);
        tvDetailLocation    = rootView.findViewById(R.id.tvDetailLocation);
        tvDetailAuthor      = rootView.findViewById(R.id.tvDetailAuthor);
        tvDetailGroup       = rootView.findViewById(R.id.tvDetailGroup);
        tvDetailNarrative   = rootView.findViewById(R.id.tvDetailNarrative);
        chipGroupDetailTags = rootView.findViewById(R.id.chipGroupDetailTags);
        btnBack             = rootView.findViewById(R.id.btnBack);
        btnLike             = rootView.findViewById(R.id.btnLike);
        tvLikeCount         = rootView.findViewById(R.id.tvLikeCount);
        rowLocation         = rootView.findViewById(R.id.rowLocation);
        rowGroup            = rootView.findViewById(R.id.rowGroup);

        btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());
    }

    private void loadPost() {
        mainActivity.db.collection("posts")
                .document(postId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        post = buildPostItem(doc);
                        bindViews();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Failed to load post: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private PostItem buildPostItem(DocumentSnapshot doc) {
        PostItem item = new PostItem();
        item.setFirestoreId(doc.getId());
        item.setAuthorId(doc.getString("authorId"));
        item.setTitle(doc.getString("title"));
        item.setDescription(doc.getString("description"));
        item.setAddress(doc.getString("address"));
        item.setGroupId(doc.getString("groupId"));
        item.setGroupName(doc.getString("groupName"));
        Boolean pub = doc.getBoolean("isPublic");
        item.setPublic(pub != null && pub);
        Boolean anon = doc.getBoolean("isAnonymous");
        item.setAnonymous(anon != null && anon);
        Long likes = doc.getLong("likes");
        item.setLikes(likes != null ? likes : 0);
        //noinspection unchecked
        List<String> tags = (List<String>) doc.get("tags");
        item.setTags(tags);
        com.google.firebase.Timestamp ts = doc.getTimestamp("timestamp");
        if (ts != null) item.setTimestampMillis(ts.toDate().getTime());
        item.setImageUri(doc.getString("imageUrl"));
        return item;
    }

    private void bindViews() {
        // Hero image
        Glide.with(this)
                .load(post.getImageUri())
                .placeholder(R.drawable.post_frame)
                .error(R.drawable.post_frame)
                .into(ivDetailImage);

        // Title (description shown as bold title over image)
        tvDetailTitle.setText( post.getTitle() != null ? post.getTitle() : "");

        // Narrative (same field for now — swap for a dedicated field if you add one later)
        tvDetailNarrative.setText(post.getDescription() != null ? post.getDescription() : "");

        // Address badge over image
        if (post.getAddress() != null && !post.getAddress().isEmpty()) {
            tvDetailAddress.setVisibility(View.VISIBLE);
            tvDetailAddress.setText(post.getAddress());
        } else {
            tvDetailAddress.setVisibility(View.GONE);
        }

        // Date
        if (post.getTimestampMillis() > 0) {
            String date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    .format(new Date(post.getTimestampMillis()));
            tvDetailDate.setText(date);
        }

        // Location row
        if (post.getAddress() != null && !post.getAddress().isEmpty()) {
            tvDetailLocation.setText(post.getAddress());
        } else {
            rowLocation.setVisibility(View.GONE);
        }

        // Author row
        if (post.isAnonymous()) {
            tvDetailAuthor.setText(R.string.anonymous);
            ivAuthorPhoto.setImageResource(R.drawable.post_frame); // default avatar
        } else {
            resolveAuthor(post.getAuthorId());
        }

        // Group row
        if (post.getGroupId() != null && !post.getGroupId().isEmpty()) {
            resolveGroup(post.getGroupId());
        } else {
            rowGroup.setVisibility(View.GONE);
        }

        // Likes
        tvLikeCount.setText(String.valueOf(post.getLikes()));
        btnLike.setOnClickListener(v -> handleLike());

        // Tags
        chipGroupDetailTags.removeAllViews();
        if (post.getTags() != null) {
            for (String tag : post.getTags()) {
                Chip chip = new Chip(requireContext());
                chip.setText("#" + tag);
                chip.setClickable(false);
                chip.setCheckable(false);
                chipGroupDetailTags.addView(chip);
            }
        }
    }

    /**
     * Fetches username and photoUrl from /users/{uid}.
     * photoUrl is stored by Login.java as "photoUrl" (empty string for guests).
     */
    private void resolveAuthor(String uid) {
        mainActivity.db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Username
                        String name = doc.getString("username");
                        tvDetailAuthor.setText(name != null ? name : uid.substring(0, 8) + "…");

                        // Profile picture — falls back to default if empty or missing
                        String photoUrl = doc.getString("photoUrl");
                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(photoUrl)
                                    .transform(new CircleCrop())
                                    .placeholder(R.drawable.post_frame)
                                    .into(ivAuthorPhoto);
                        } else {
                            ivAuthorPhoto.setImageResource(R.drawable.post_frame);
                        }
                    } else {
                        tvDetailAuthor.setText(uid.substring(0, 8) + "…");
                    }
                })
                .addOnFailureListener(e ->
                        tvDetailAuthor.setText(uid.substring(0, 8) + "…"));
    }

    /**
     * Fetches group name and photo from /groups/{groupId}.
     * Adjust field names to match what GroupActivity writes to Firestore.
     */
    private void resolveGroup(String groupId) {
        mainActivity.db.collection("groups").document(groupId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Group name
                        String name = doc.getString("name");
                        tvDetailGroup.setText(name != null ? name : groupId);

                        // Group photo — falls back to default if missing
                        String photoUrl = doc.getString("photoUrl");
                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(photoUrl)
                                    .transform(new CircleCrop())
                                    .placeholder(R.drawable.post_frame)
                                    .into(ivGroupPhoto);
                        } else {
                            ivGroupPhoto.setImageResource(R.drawable.post_frame);
                        }
                    } else {
                        rowGroup.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> rowGroup.setVisibility(View.GONE));
    }

    private void handleLike() {
        if (mainActivity.mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Sign in to like posts", Toast.LENGTH_SHORT).show();
            return;
        }

        long newLikes = post.getLikes() + 1;
        post.setLikes(newLikes);
        tvLikeCount.setText(String.valueOf(newLikes));

        mainActivity.db.collection("posts")
                .document(post.getFirestoreId())
                .update("likes", FieldValue.increment(1))
                .addOnFailureListener(e -> {
                    post.setLikes(newLikes - 1);
                    tvLikeCount.setText(String.valueOf(newLikes - 1));
                    Toast.makeText(getContext(), "Could not update like", Toast.LENGTH_SHORT).show();
                });
    }
}