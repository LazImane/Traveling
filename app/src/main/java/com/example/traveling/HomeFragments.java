package com.example.traveling;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**TODO : IMPLEMENT GPS FOR FILTER AROUND ME*/

public class HomeFragments extends Fragment {
    EditText etSearch;
    Button filterNature, filterCity, filterMuseums, filterShops, filterAround;

    //adding recycler View to show posts
    RecyclerView recyclerView;
    ProgressBar progressBar;

    //access to db through mainactivity
    MainActivity mainActivity;

    View view;

    //data related to showing posts
    private final List<PostItem> allPosts = new ArrayList<>();
    private final List<PostItem> displayedPosts = new ArrayList<>();
    private PostAdapter  adapter;
    private String activeFilter = null;
    private String activeGroupId = null;
    private String activeGroupName = null;

    public HomeFragments() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity)getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        mainActivity = (MainActivity) getContext();
        if (getArguments() != null) {
            activeGroupId   = getArguments().getString("groupId");
            activeGroupName = getArguments().getString("groupName");
        }
        init();
        setListeners();
        loadPosts();
        return view;
    }
    private void init() {

        etSearch            = view.findViewById(R.id.etSearch);
        filterNature        = view.findViewById(R.id.filterNature);
        filterCity          = view.findViewById(R.id.filterCity);
        filterMuseums       = view.findViewById(R.id.filterMuseums);
        filterShops         = view.findViewById(R.id.filterShops);
        filterAround        = view.findViewById(R.id.filterAround);
        //related to showing posts
        recyclerView  = view.findViewById(R.id.rvPosts);
        progressBar   = view.findViewById(R.id.progressBar);

        adapter = new PostAdapter(displayedPosts, new PostAdapter.OnPostClickListener() {
            @Override
            public void onPostClick(PostItem post) {
                openPostDetail(post);
            }
            @Override
            public void onLikeClick(PostItem post, int position) {
                handleLike(post, position);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

    }
    private void setListeners() {

        filterNature.setOnClickListener(v -> handleFilter("nature"));
        filterCity.setOnClickListener(v -> handleFilter("city"));
        filterMuseums.setOnClickListener(v -> handleFilter("museums"));
        filterShops.setOnClickListener(v -> handleFilter("shops"));
        filterAround.setOnClickListener(v -> handleFilter("around"));
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearchAndFilter(s.toString().trim(), activeFilter);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

    }

    private void handleFilter(String filter) {
        if (filter.equals(activeFilter)) {
            // Tap same filter → clear it
            activeFilter = null;
            resetFilterButtonStyles();
        } else {
            activeFilter = filter;
            highlightActiveFilter(filter);
        }
        applySearchAndFilter(etSearch.getText().toString().trim(), activeFilter);
    }

    private void applySearchAndFilter(String query, String filter) {
        displayedPosts.clear();
        for (PostItem post : allPosts) {
            if (!matchesFilter(post, filter)) continue;
            if (!matchesSearch(post, query))  continue;
            displayedPosts.add(post);
        }
        adapter.notifyDataSetChanged();
    }

    private boolean matchesFilter(PostItem post, String filter) {
        if (filter == null || filter.isEmpty()) return true;
        if (filter.equals("around"))            return true; // location-aware: implement with GPS later

        // Check if any tag contains the filter keyword
        if (post.getTags() != null) {
            for (String tag : post.getTags()) {
                if (tag.toLowerCase().contains(filter.toLowerCase())) return true;
            }
        }
        // Also check description and address and the title
        String desc = post.getDescription();
        if (desc != null && desc.toLowerCase().contains(filter.toLowerCase())) return true;
        String addr = post.getAddress();
        if (addr != null && addr.toLowerCase().contains(filter.toLowerCase())) return true;
        String title = post.getTitle();

        if (title != null &&
                title.toLowerCase().contains(filter.toLowerCase())) {
            return true;
        }

        return false;
    }

    private boolean matchesSearch(PostItem post, String query) {
        if (query.isEmpty()) return true;
        String q = query.toLowerCase();
        if (post.getTitle() != null &&
                post.getTitle().toLowerCase().contains(q)) {
            return true;
        }
        if (post.getGroupName() != null &&
                post.getGroupName().toLowerCase().contains(q)) {
            return true;
        }
        if (post.getDescription() != null && post.getDescription().toLowerCase().contains(q)) return true;
        if (post.getAddress()     != null && post.getAddress().toLowerCase().contains(q))     return true;
        if (post.getTags() != null) {
            for (String tag : post.getTags()) {
                if (tag.toLowerCase().contains(q)) return true;
            }
        }
        return false;
    }


    //load posts from firestore
    private void loadPosts() {
        progressBar.setVisibility(View.VISIBLE);

        Log.d("HomeFragments", "Starting to load posts...");

        com.google.firebase.firestore.CollectionReference postsRef =
                mainActivity.db.collection("posts");

        com.google.firebase.firestore.Query query;
        if (activeGroupId != null) {
            query = postsRef.whereEqualTo("group", activeGroupId).limit(50);
        } else {
            query = postsRef.whereEqualTo("isPublic", true).limit(50);
        }

        query.get()
                .addOnSuccessListener(querySnapshot -> {
                    allPosts.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        PostItem item = buildPostItem(doc);
                        allPosts.add(item);
                    }
                    applySearchAndFilter(etSearch.getText().toString().trim(), activeFilter);
                    progressBar.setVisibility(View.GONE);

                    if (allPosts.isEmpty()) {
                        Toast.makeText(getContext(),
                                activeGroupId != null
                                        ? getString(R.string.no_posts_group)
                                        : getString(R.string.no_posts_found),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(),
                            getString(R.string.failed_load_posts) + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });

        mainActivity.db.collection("posts")
                .whereEqualTo("isPublic", true)
                //.orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("HomeFragments", "Success! Found " + querySnapshot.size() + " posts");
                    allPosts.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        PostItem item = buildPostItem(doc);
                        allPosts.add(item);
                        Log.d("HomeFragments", "Post: " + item.getDescription());
                    }
                    applySearchAndFilter(etSearch.getText().toString().trim(), activeFilter);
                    progressBar.setVisibility(View.GONE);

                    if (allPosts.isEmpty()) {
                        Toast.makeText(getContext(), getString(R.string.no_posts_database), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeFragments", "Failed to load posts", e);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(),
                            "Failed to load posts: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });

        //TODO: add posts from groups too
    }

    //maps firestore storage to PostItem
    private PostItem buildPostItem(DocumentSnapshot doc) {
        PostItem item = new PostItem();
        item.setFirestoreId(doc.getId());
        item.setAuthorId(doc.getString("authorId"));
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

        // Image URL from Firebase Storage
        item.setImageUri(doc.getString("imageUrl"));

        // Check if current user already liked this post
        String uid = mainActivity.mAuth.getCurrentUser() != null
                ? mainActivity.mAuth.getCurrentUser().getUid() : null;
        if (uid != null) {
            mainActivity.db.collection("liked_by")
                    .whereEqualTo("postId", doc.getId())
                    .whereEqualTo("userId", uid)
                    .get()
                    .addOnSuccessListener(qs -> {
                        item.setLikedByMe(!qs.isEmpty());
                        // Notify adapter to refresh just this item's button state
                        int pos = allPosts.indexOf(item);
                        if (pos >= 0) adapter.notifyItemChanged(pos, "likes");
                    });
        }


        return item;
    }

    //like button logic
    private void handleLike(PostItem post, int position) {
        String uid = mainActivity.mAuth.getCurrentUser() != null
                ? mainActivity.mAuth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Toast.makeText(getContext(), getString(R.string.sign_in_like), Toast.LENGTH_SHORT).show();
            return;
        }

        if (post.isLikedByMe()) {
            // --- UNLIKE ---
            post.setLikedByMe(false);
            post.setLikes(post.getLikes() - 1);
            adapter.updateLikes(position, post.getLikes());
            adapter.notifyItemChanged(position);

            // Remove liked_by document
            mainActivity.db.collection("liked_by")
                    .whereEqualTo("postId", post.getFirestoreId())
                    .whereEqualTo("userId", uid)
                    .get()
                    .addOnSuccessListener(qs -> {
                        for (DocumentSnapshot doc : qs) doc.getReference().delete();
                    })
                    .addOnFailureListener(e -> {
                        // Roll back
                        post.setLikedByMe(true);
                        post.setLikes(post.getLikes() + 1);
                        adapter.updateLikes(position, post.getLikes());
                    });

            // Decrement counter on the post
            mainActivity.db.collection("posts")
                    .document(post.getFirestoreId())
                    .update("likes", FieldValue.increment(-1));

        } else {
            // --- LIKE ---
            post.setLikedByMe(true);
            post.setLikes(post.getLikes() + 1);
            adapter.updateLikes(position, post.getLikes());
            adapter.notifyItemChanged(position);

            // Write liked_by document
            Map<String, Object> likeDoc = new HashMap<>();
            likeDoc.put("postId", post.getFirestoreId());
            likeDoc.put("userId", uid);

            mainActivity.db.collection("liked_by").document()
                    .set(likeDoc)
                    .addOnFailureListener(e -> {
                        // Roll back
                        post.setLikedByMe(false);
                        post.setLikes(post.getLikes() - 1);
                        adapter.updateLikes(position, post.getLikes());
                    });

            // Increment counter on the post
            mainActivity.db.collection("posts")
                    .document(post.getFirestoreId())
                    .update("likes", FieldValue.increment(1));
        }
    }

    private void openPostDetail(PostItem post) {
        PostdetailFragment detail = PostdetailFragment.newInstance(post);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right)
                .replace(R.id.fragmentLayout, detail)
                .addToBackStack(null)
                .commit();
    }


    //filter button styles
    private void resetFilterButtonStyles() {
        filterNature.setSelected(false);
        filterCity.setSelected(false);
        filterMuseums.setSelected(false);
        filterShops.setSelected(false);
        filterAround.setSelected(false);
    }

    private void highlightActiveFilter(String filter) {
        resetFilterButtonStyles();
        switch (filter) {
            case "nature":   filterNature.setSelected(true);   break;
            case "city":     filterCity.setSelected(true);     break;
            case "museums":  filterMuseums.setSelected(true);  break;
            case "shops":    filterShops.setSelected(true);    break;
            case "around":   filterAround.setSelected(true);   break;
        }
    }

    public static HomeFragments newInstanceWithGroup(String groupId, String groupName) {
        HomeFragments f = new HomeFragments();
        Bundle args = new Bundle();
        args.putString("groupId", groupId);
        args.putString("groupName", groupName);
        f.setArguments(args);
        return f;
    }

}