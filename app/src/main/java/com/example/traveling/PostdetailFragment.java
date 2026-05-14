package com.example.traveling;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Full-screen post detail view.
 * Pass a PostItem via newInstance() — it is serialised as individual Bundle extras.
 */
public class PostdetailFragment extends Fragment {

    // Bundle keys
    private static final String KEY_ID          = "firestoreId";
    private static final String KEY_DESC        = "description";
    private static final String KEY_ADDRESS     = "address";
    private static final String KEY_GROUP       = "group";
    private static final String KEY_LIKES       = "likes";
    private static final String KEY_IMAGE_URI   = "imageUri";
    private static final String KEY_TIMESTAMP   = "timestamp";
    private static final String KEY_ANONYMOUS   = "isAnonymous";

    // Views
    private ImageView  ivDetailImage;
    private TextView   tvDetailAddress;
    private TextView   tvDetailDescription;
    private TextView   tvDetailGroup;
    private TextView   tvDetailDate;
    private ChipGroup  chipGroupDetailTags;
    private ImageView  btnDetailLike;
    private TextView   tvDetailLikeCount;
    private ImageView  btnBack;

    // State
    private PostItem post;
    private FirebaseFirestore db;
    private FirebaseAuth      mAuth;

    // ── Factory ───────────────────────────────────────────────────────────────

    public static PostdetailFragment newInstance(PostItem post) {
        PostdetailFragment f = new PostdetailFragment();
        Bundle args = new Bundle();
        args.putString(KEY_ID,        post.getFirestoreId());
        args.putString(KEY_DESC,      post.getDescription());
        args.putString(KEY_ADDRESS,   post.getAddress());
        args.putString(KEY_GROUP,     post.getGroup());
        args.putLong  (KEY_LIKES,     post.getLikes());
        args.putString(KEY_IMAGE_URI, post.getImageUri());
        args.putLong  (KEY_TIMESTAMP, post.getTimestampMillis());
        args.putBoolean(KEY_ANONYMOUS, post.isAnonymous());
        if (post.getTags() != null) {
            args.putStringArrayList("tags", new java.util.ArrayList<>(post.getTags()));
        }
        f.setArguments(args);
        return f;
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db    = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Rebuild the PostItem from bundle
        post  = new PostItem();
        Bundle args = getArguments();
        if (args != null) {
            post.setFirestoreId(args.getString(KEY_ID));
            post.setDescription(args.getString(KEY_DESC));
            post.setAddress(args.getString(KEY_ADDRESS));
            post.setGroup(args.getString(KEY_GROUP));
            post.setLikes(args.getLong(KEY_LIKES));
            post.setImageUri(args.getString(KEY_IMAGE_URI));
            post.setTimestampMillis(args.getLong(KEY_TIMESTAMP));
            post.setAnonymous(args.getBoolean(KEY_ANONYMOUS));
            post.setTags(args.getStringArrayList("tags"));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_detail, container, false);
        initViews(view);
        bindData();
        setListeners();
        return view;
    }

    // ── Init & bind ───────────────────────────────────────────────────────────

    private void initViews(View view) {
        ivDetailImage       = view.findViewById(R.id.ivDetailImage);
        tvDetailAddress     = view.findViewById(R.id.tvDetailAddress);
        tvDetailDescription = view.findViewById(R.id.tvDetailDescription);
        tvDetailGroup       = view.findViewById(R.id.tvDetailGroup);
        tvDetailDate        = view.findViewById(R.id.tvDetailDate);
        chipGroupDetailTags = view.findViewById(R.id.chipGroupDetailTags);
        //btnDetailLike       = view.findViewById(R.id.btnDetailLike);
        //tvDetailLikeCount   = view.findViewById(R.id.tvDetailLikeCount);
        btnBack             = view.findViewById(R.id.btnBack);
    }

    private void bindData() {
        // Image
        if (post.getImageUri() != null) {
            ivDetailImage.setImageURI(Uri.parse(post.getImageUri()));
        } else {
            ivDetailImage.setImageResource(R.drawable.post_frame);
        }

        // Address
        String address = post.getAddress();
        if (address != null && !address.isEmpty()) {
            tvDetailAddress.setVisibility(View.VISIBLE);
            tvDetailAddress.setText(address);
        } else {
            tvDetailAddress.setVisibility(View.GONE);
        }

        // Description
        String desc = post.getDescription();
        tvDetailDescription.setText((desc != null && !desc.isEmpty()) ? desc : "No description");

        // Group
        String group = post.getGroup();
        if (group != null && !group.isEmpty()) {
            tvDetailGroup.setVisibility(View.VISIBLE);
            tvDetailGroup.setText("Posted on " + group);
        } else {
            tvDetailGroup.setVisibility(View.GONE);
        }

        // Date
        if (post.getTimestampMillis() > 0) {
            String formatted = new SimpleDateFormat("MMMM dd, yyyy", Locale.US)
                    .format(new Date(post.getTimestampMillis()));
            tvDetailDate.setText(formatted);
        }

        // Likes
        tvDetailLikeCount.setText(formatCount(post.getLikes()));

        // Tags
        chipGroupDetailTags.removeAllViews();
        if (post.getTags() != null) {
            for (String tag : post.getTags()) {
                Chip chip = new Chip(chipGroupDetailTags.getContext());
                chip.setText("#" + tag);
                chip.setClickable(false);
                chip.setCheckable(false);
                chipGroupDetailTags.addView(chip);
            }
        }
    }

    private void setListeners() {
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        btnDetailLike.setOnClickListener(v -> handleLike());
    }

    // ── Like ──────────────────────────────────────────────────────────────────

    private void handleLike() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Sign in to like posts", Toast.LENGTH_SHORT).show();
            return;
        }

        long newLikes = post.getLikes() + 1;
        post.setLikes(newLikes);
        tvDetailLikeCount.setText(formatCount(newLikes));

        db.collection("posts")
                .document(post.getFirestoreId())
                .update("likes", FieldValue.increment(1))
                .addOnFailureListener(e -> {
                    post.setLikes(newLikes - 1);
                    tvDetailLikeCount.setText(formatCount(newLikes - 1));
                    Toast.makeText(getContext(), "Could not update like", Toast.LENGTH_SHORT).show();
                });
    }

    private String formatCount(long n) {
        if (n >= 1_000_000) return String.format(Locale.US, "%.1fM", n / 1_000_000.0);
        if (n >= 1_000)     return String.format(Locale.US, "%.1fk", n / 1_000.0);
        return String.valueOf(n);
    }
}