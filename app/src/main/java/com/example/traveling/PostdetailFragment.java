package com.example.traveling;

import android.content.Intent;
import android.net.Uri;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**TODO : COMMENTS + SIGNALER + LIKES + Open MAP**/
public class PostdetailFragment extends Fragment {

    private static final String ARG_POST_ID = "post_id";

    private String postId;
    private MainActivity mainActivity;
    private PostItem post;
    private View rootView;

    // Views
    private ImageView ivDetailImage, ivAuthorPhoto, ivGroupPhoto;
    private TextView tvDetailTitle, tvDetailAddress, tvDetailDate,
            tvDetailLocation, tvDetailAuthor, tvDetailGroup, tvDetailNarrative;
    private ChipGroup chipGroupDetailTags;
    private ImageView btnBack, btnLike, btnComment, btnLocation;
    private TextView tvLikeCount, tvCommentCount;
    private LinearLayout rowLocation, rowGroup, btnSignalerContainer;

    //Like state
    private boolean likedByMe = false;
    private String currentUid;

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
        btnComment          = rootView.findViewById(R.id.btnComment);
        tvCommentCount      = rootView.findViewById(R.id.tvCommentCount);
        btnLocation         = rootView.findViewById(R.id.btnLocation);
        btnSignalerContainer = rootView.findViewById(R.id.btnSignalerContainer);
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
                                getString(R.string.failed_load_post) + e.getMessage(),
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
        currentUid = mainActivity.mAuth.getCurrentUser() != null
                ? mainActivity.mAuth.getCurrentUser().getUid() : null;

        // Hero image
        Glide.with(this)
                .load(post.getImageUri())
                .placeholder(R.drawable.post_frame)
                .error(R.drawable.post_frame)
                .into(ivDetailImage);

        // Title
        tvDetailTitle.setText( post.getTitle() != null ? post.getTitle() : "");

        // Narrative
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

        // Action bar
        tvLikeCount.setText(String.valueOf(post.getLikes()));
        checkIfLiked();
        btnLike.setOnClickListener(v -> handleLike());

        loadCommentCount();
        btnComment.setOnClickListener(v -> {
            CommentBottomSheet sheet = CommentBottomSheet.newInstance(post.getFirestoreId());
            sheet.setOnCommentPostedListener(newCount ->
                    tvCommentCount.setText(String.valueOf(newCount)));
            sheet.show(getChildFragmentManager(), "comments");
        });

        btnLocation.setOnClickListener(v -> openMaps());
        rootView.findViewById(R.id.tvLocation)
                .setOnClickListener(v -> openMaps());

        btnSignalerContainer.setOnClickListener(v -> submitReport());
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

    /** HANDLES LIKES */
    private void checkIfLiked() {
        if (currentUid == null) return;
        mainActivity.db.collection("liked_by")
                .whereEqualTo("postId", post.getFirestoreId())
                .whereEqualTo("userId", currentUid)
                .get()
                .addOnSuccessListener(qs -> {
                    likedByMe = !qs.isEmpty();
                    btnLike.setImageResource(likedByMe
                            ? R.drawable.ic_heart_fill
                            : R.drawable.ic_heart);
                });
    }

    private void handleLike() {
        if (currentUid == null) {
            Toast.makeText(getContext(), getString(R.string.sign_in_like), Toast.LENGTH_SHORT).show();
            return;
        }
        if (likedByMe) {
            // Unlike
            likedByMe = false;
            btnLike.setImageResource(R.drawable.ic_heart);
            post.setLikes(post.getLikes() - 1);
            tvLikeCount.setText(String.valueOf(post.getLikes()));

            mainActivity.db.collection("liked_by")
                    .whereEqualTo("postId", post.getFirestoreId())
                    .whereEqualTo("userId", currentUid)
                    .get()
                    .addOnSuccessListener(qs -> {
                        for (DocumentSnapshot d : qs) d.getReference().delete();
                    })
                    .addOnFailureListener(e -> {
                        likedByMe = true;
                        btnLike.setImageResource(R.drawable.ic_heart_fill);
                        post.setLikes(post.getLikes() + 1);
                        tvLikeCount.setText(String.valueOf(post.getLikes()));
                    });

            mainActivity.db.collection("posts").document(post.getFirestoreId())
                    .update("likes", FieldValue.increment(-1));
        } else {
            // Like
            likedByMe = true;
            btnLike.setImageResource(R.drawable.ic_heart_fill);
            post.setLikes(post.getLikes() + 1);
            tvLikeCount.setText(String.valueOf(post.getLikes()));

            Map<String, Object> likeDoc = new HashMap<>();
            likeDoc.put("postId", post.getFirestoreId());
            likeDoc.put("userId", currentUid);
            mainActivity.db.collection("liked_by").document()
                    .set(likeDoc)
                    .addOnFailureListener(e -> {
                        likedByMe = false;
                        btnLike.setImageResource(R.drawable.ic_heart);
                        post.setLikes(post.getLikes() - 1);
                        tvLikeCount.setText(String.valueOf(post.getLikes()));
                    });

            mainActivity.db.collection("posts").document(post.getFirestoreId())
                    .update("likes", FieldValue.increment(1));
        }
    }

    /** comment count */
    private void loadCommentCount() {
        mainActivity.db.collection("comments")
                .whereEqualTo("postId", post.getFirestoreId())
                .get()
                .addOnSuccessListener(qs ->
                        tvCommentCount.setText(String.valueOf(qs.size())));
    }

    private void openMaps() {
        String address = post.getAddress();
        if (address == null || address.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.no_address_available),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Intent osmAnd = new Intent(Intent.ACTION_VIEW,
                Uri.parse("osmand.api://navigate?dest_name=" + Uri.encode(address)
                        + "&dest_lat=0&dest_lon=0"));
        osmAnd.setPackage("net.osmand");

        Intent browser = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.openstreetmap.org/search?query="
                        + Uri.encode(address)));

        Intent geo = new Intent(Intent.ACTION_VIEW,
                Uri.parse("geo:0,0?q=" + Uri.encode(address)));

        if (osmAnd.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(osmAnd);
        } else if (geo.resolveActivity(requireActivity().getPackageManager()) != null) {
            // Shows a chooser: OSMAnd, Google Maps, Waze, whatever is installed
            startActivity(Intent.createChooser(geo, getString(R.string.open_with)));
        } else {
            // Guaranteed fallback — OSM in the browser
            startActivity(browser);
        }
    }

    private void submitReport() {
        if (currentUid == null) {
            Toast.makeText(getContext(), getString(R.string.connect_to_report),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Prevent duplicate reports from the same user on the same post
        mainActivity.db.collection("reports")
                .whereEqualTo("postId", post.getFirestoreId())
                .whereEqualTo("userId", currentUid)
                .get()
                .addOnSuccessListener(qs -> {
                    if (!qs.isEmpty()) {
                        Toast.makeText(getContext(),
                                getString(R.string.already_reported),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> report = new HashMap<>();
                    report.put("postId",    post.getFirestoreId());
                    report.put("userId",    currentUid);
                    report.put("timestamp", com.google.firebase.Timestamp.now());

                    mainActivity.db.collection("reports").document()
                            .set(report)
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(getContext(),
                                            getString(R.string.post_reported),
                                            Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(),
                                            getString(R.string.report_fail),
                                            Toast.LENGTH_SHORT).show());
                });
    }

}