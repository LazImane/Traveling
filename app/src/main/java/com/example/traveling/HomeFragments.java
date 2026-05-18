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

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

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

    //around me filter button
    private static final double RADIUS_KM = 10.0;

    private FusedLocationProviderClient fusedLocation;
    private double userLat = Double.NaN;
    private double userLng = Double.NaN;

    // Permission launcher — replaces the old requestPermissions() API
    private final ActivityResultLauncher<String[]> locationPermLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        boolean granted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION))
                                || Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                        if (granted) {
                            fetchLocationThenFilter();
                        } else {
                            Toast.makeText(getContext(),
                                    "Location permission denied — can't filter nearby posts.",
                                    Toast.LENGTH_SHORT).show();
                            // Snap the filter back off
                            activeFilter = null;
                            resetFilterButtonStyles();
                        }
                    });

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
        fusedLocation = LocationServices.getFusedLocationProviderClient(requireActivity());
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
            activeFilter = null;
            userLat = Double.NaN;
            userLng = Double.NaN;
            resetFilterButtonStyles();
            applySearchAndFilter(etSearch.getText().toString().trim(), null);
            return;
        }

        activeFilter = filter;
        highlightActiveFilter(filter);

        if ("around".equals(filter)) {
            requestLocationAndFilter();
        } else {
            applySearchAndFilter(etSearch.getText().toString().trim(), filter);
        }
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

        if ("around".equals(filter)) {
            // If we don't have a fix yet, show nothing until the callback fires
            if (Double.isNaN(userLat) || Double.isNaN(userLng)) return false;
            // Posts without stored coordinates are excluded
            if (post.getLatitude() == 0.0 && post.getLongitude() == 0.0) return false;
            return distanceKm(userLat, userLng,
                    post.getLatitude(), post.getLongitude()) <= RADIUS_KM;
        }

        // existing tag / description / address / title checks
        if (post.getTags() != null) {
            for (String tag : post.getTags())
                if (tag.toLowerCase().contains(filter.toLowerCase())) return true;
        }
        String desc = post.getDescription();
        if (desc != null && desc.toLowerCase().contains(filter.toLowerCase())) return true;
        String addr = post.getAddress();
        if (addr != null && addr.toLowerCase().contains(filter.toLowerCase())) return true;
        String title = post.getTitle();
        if (title != null && title.toLowerCase().contains(filter.toLowerCase())) return true;

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

    // load posts from firestore
    private void loadPosts() {
        android.util.Log.d("HomeFragments", "loadPosts: activeGroupId=" + activeGroupId + " activeGroupName=" + activeGroupName);
        progressBar.setVisibility(View.VISIBLE);
        Log.d("HomeFragments", "Starting to load posts...");

        com.google.firebase.firestore.Query query;

        if (activeGroupId != null) {
            boolean isAnonymous = mainActivity.mAuth.getCurrentUser() == null || mainActivity.mAuth.getCurrentUser().isAnonymous();
            if (isAnonymous) {
                // guest users only see public posts in the group
                query = mainActivity.db.collection("posts")
                        .whereEqualTo("groupId", activeGroupId)
                        .whereEqualTo("isPublic", true)
                        .limit(50);
            } else {
                // members see all posts in the group
                query = mainActivity.db.collection("posts")
                        .whereEqualTo("groupId", activeGroupId)
                        .limit(50);
            }
        } else {
            query = mainActivity.db.collection("posts")
                    .whereEqualTo("isPublic", true)
                    .limit(50);
        }

        query.get()
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
                        Toast.makeText(getContext(),
                                activeGroupId != null
                                        ? getString(R.string.no_posts_group)
                                        : getString(R.string.no_posts_database),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeFragments", "Failed to load posts", e);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(),
                            "Failed to load posts: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    //maps firestore storage to PostItem
    private PostItem buildPostItem(DocumentSnapshot doc) {
        PostItem item = new PostItem();
        item.setFirestoreId(doc.getId());
        item.setAuthorId(doc.getString("authorId"));
        item.setTitle(doc.getString("title"));
        item.setDescription(doc.getString("description"));
        item.setAddress(doc.getString("address"));
        //group
        item.setGroupId(doc.getString("groupId"));
        item.setGroupName(doc.getString("groupName"));
        Boolean pub = doc.getBoolean("isPublic");
        //guest
        item.setPublic(pub != null && pub);
        Boolean anon = doc.getBoolean("isAnonymous");
        item.setAnonymous(anon != null && anon);
        //likes
        Long likes = doc.getLong("likes");
        item.setLikes(likes != null ? likes : 0);
        //pos
        Double lat = doc.getDouble("latitude");
        Double lng = doc.getDouble("longitude");
        item.setLatitude(lat  != null ? lat  : 0.0);
        item.setLongitude(lng != null ? lng : 0.0);

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

    private void requestLocationAndFilter() {
        boolean fine   = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (fine || coarse) {
            fetchLocationThenFilter();
        } else {
            locationPermLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    @SuppressWarnings("MissingPermission")
    private void fetchLocationThenFilter() {
        // getCurrentLocation gives a fresh fix (not a stale lastKnown)
        fusedLocation.getCurrentLocation(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        new CancellationTokenSource().getToken())
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        Toast.makeText(getContext(),
                                "Couldn't get your location. Try again.",
                                Toast.LENGTH_SHORT).show();
                        activeFilter = null;
                        resetFilterButtonStyles();
                        return;
                    }
                    userLat = location.getLatitude();
                    userLng = location.getLongitude();
                    applySearchAndFilter(
                            etSearch.getText().toString().trim(), "around");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                            "Location error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    activeFilter = null;
                    resetFilterButtonStyles();
                });
    }

    private static double distanceKm(double lat1, double lon1,
                                     double lat2, double lon2) {
        final double R = 6371.0; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }


}