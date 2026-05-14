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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

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
                //openPostDetail(post);
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
        // Also check description and address
        String desc = post.getDescription();
        if (desc != null && desc.toLowerCase().contains(filter.toLowerCase())) return true;
        String addr = post.getAddress();
        if (addr != null && addr.toLowerCase().contains(filter.toLowerCase())) return true;

        return false;
    }

    private boolean matchesSearch(PostItem post, String query) {
        if (query.isEmpty()) return true;
        String q = query.toLowerCase();
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

        // Query: public posts, ordered by newest first, limit 50 for now
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
                        Toast.makeText(getContext(), "No posts found in database", Toast.LENGTH_SHORT).show();
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
        item.setGroup(doc.getString("group"));
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

        // Image URL from Firebase Storage (works on any device)
        item.setImageUri(doc.getString("imageUrl"));

        return item;
    }

    //like button logic
    private void handleLike(PostItem post, int position) {
        FirebaseUser user = mainActivity.mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Sign in to like posts", Toast.LENGTH_SHORT).show();
            return;
        }

        // Optimistic UI update
        long newLikes = post.getLikes() + 1;
        post.setLikes(newLikes);
        adapter.updateLikes(position, newLikes);

        // Persist increment to Firestore
        mainActivity.db.collection("posts")
                .document(post.getFirestoreId())
                .update("likes", FieldValue.increment(1))
                .addOnFailureListener(e -> {
                    // Roll back on failure
                    post.setLikes(newLikes - 1);
                    adapter.updateLikes(position, newLikes - 1);
                    Toast.makeText(getContext(), "Could not update like", Toast.LENGTH_SHORT).show();
                });
    }

    //open post details TODO
//    private void openPostDetail(PostItem post) {
//        PostDetailFragment detail = PostDetailFragment.newInstance(post);
//        requireActivity().getSupportFragmentManager()
//                .beginTransaction()
//                .setCustomAnimations(
//                        R.anim.slide_in_right,
//                        R.anim.slide_out_left,
//                        R.anim.slide_in_left,
//                        R.anim.slide_out_right)
//                .replace(R.id.fragment_container, detail)   // adjust ID to match your activity
//                .addToBackStack(null)
//                .commit();
//    }


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

}