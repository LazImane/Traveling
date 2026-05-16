package com.example.traveling;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GroupsFragment extends Fragment {
    View view;
    LinearLayout groupsContainer;
    LinearLayout searchResultsContainer;
    View searchResultsScroll;
    EditText etSearch;
    Map<View, String> groups = new HashMap<>();
    View addGroup;

    MainActivity activity;

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_groups, container, false);
        init();
        setListeners();
        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        createGroups();
    }
    private void init() {
        activity = (MainActivity) getActivity();
        groupsContainer = view.findViewById(R.id.groupsContainer);
        addGroup = view.findViewById(R.id.addGroup);
        searchResultsContainer = view.findViewById(R.id.searchResultsContainer);
        searchResultsScroll    = view.findViewById(R.id.searchResultsScroll);
        etSearch               = view.findViewById(R.id.etSearch);
    }
    private void setListeners() {

        addGroup.setOnClickListener(v -> createNewGroup());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    searchResultsScroll.setVisibility(View.GONE);
                    searchResultsContainer.removeAllViews();
                } else {
                    searchGroups(query);
                }
            }
        });
    }

    private void createNewGroup(){
        Intent intent = new Intent(activity, GroupActivity.class);
        startActivity(intent);
    }

    private void createGroups(){
        groupsContainer.removeAllViews();
        activity.db.collection("groups_to_users_link")
            .whereEqualTo("user_id", activity.user.getUid())
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {

                for (DocumentSnapshot doc : queryDocumentSnapshots) {

                    String groupId = doc.getString("group_id");

                    if (groupId != null) {
                        createGroup(groupId);
                    }
                }

            })
            .addOnFailureListener(e -> {
                Toast.makeText(activity, "Failed to load groups", Toast.LENGTH_SHORT).show();
            });
    }

    private void createGroup(String groupId){
        if(!isAdded()) return;
        View newGroup = getLayoutInflater().inflate(R.layout.view_group, groupsContainer, false);
        groups.put(newGroup, groupId);

        View group_button = newGroup.findViewById(R.id.group_button);
        group_button.setOnClickListener(v ->
                activity.db.collection("groups").document(groupId).get()
                        .addOnSuccessListener(doc -> {
                            String name = doc.exists() ? doc.getString("name") : groupId;
                            openGroupFeed(groupId, name);
                        })
                        .addOnFailureListener(e -> openGroupFeed(groupId, groupId)));

        ImageView ivGroupPhoto = newGroup.findViewById(R.id.ivGroupPhoto);

        View button = newGroup.findViewById(R.id.options);

        TextView groupName = group_button.findViewById(R.id.groupName);
        TextView groupCount = group_button.findViewById(R.id.groupCount);
        TextView groupNotification = group_button.findViewById(R.id.groupNotification);

        // Check if user is already in this group (for proper menu display)
        activity.db.collection("groups_to_users_link")
                .whereEqualTo("group_id", groupId)
                .whereEqualTo("user_id", activity.user.getUid())
                .get()
                .addOnSuccessListener(qs -> {
                    boolean isMember = !qs.isEmpty();
                    button.setOnClickListener(v -> openPopupWindow(v, groupId, isMember));
                });

        activity.db.collection("groups").document(groupId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        groupName.setText(doc.getString("name"));
                        loadGroupPhoto(doc.getString("photoUrl"), ivGroupPhoto);

                    }
                });
        activity.db.collection("groups_to_users_link")
                .whereEqualTo("group_id", groupId)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    int memberCount = querySnapshot.size();

                    groupCount.setText(String.valueOf(memberCount)+ " members");
                });
        activity.db.collection("notifications")
                .whereEqualTo("group_id", groupId)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    int notifCount = querySnapshot.size();

                    groupNotification.setText(String.valueOf(notifCount)+ " notifications");
                });

        button.setOnClickListener(v -> {
            // Check if user is a member to show correct menu
            activity.db.collection("groups_to_users_link")
                    .whereEqualTo("group_id", groupId)
                    .whereEqualTo("user_id", activity.user.getUid())
                    .get()
                    .addOnSuccessListener(qs -> {
                        boolean isMember = !qs.isEmpty();
                        openPopupWindow(v, groupId, isMember);
                    })
                    .addOnFailureListener(e -> {
                        openPopupWindow(v, groupId, false);
                    });
        });

        groupsContainer.addView(newGroup);
    }

    private void openPopupWindow(View v, String groupId, boolean isMember) {
        PopupMenu menu = new PopupMenu(requireContext(), v);
        menu.getMenuInflater().inflate(R.menu.menu_delete, menu.getMenu());

        if (isMember) {
            menu.getMenu().findItem(R.id.action_delete).setVisible(true);
            menu.getMenu().findItem(R.id.action_join).setVisible(false);
        } else {
            menu.getMenu().findItem(R.id.action_delete).setVisible(false);
            menu.getMenu().findItem(R.id.action_join).setVisible(true);
        }

        menu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete) {
                View group = (View) v.getParent();
                leaveGroup(group);
                return true;
            }  else if (item.getItemId() == R.id.action_join) {
                joinGroupFromMenu(groupId);
                return true;
            }
            return false;
        });

        menu.show();
    }

    private void joinGroup(String groupId, String groupName) {
        String uid = activity.user.getUid();

        // Check if already a member first
        activity.db.collection("groups_to_users_link")
                .whereEqualTo("group_id", groupId)
                .whereEqualTo("user_id", uid)
                .get()
                .addOnSuccessListener(qs -> {
                    if (!qs.isEmpty()) {
                        Toast.makeText(activity,
                                "You're already in " + groupName,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> link = new HashMap<>();
                    link.put("group_id", groupId);
                    link.put("user_id",  uid);

                    activity.db.collection("groups_to_users_link").document()
                            .set(link)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(activity,
                                        "Joined " + groupName + "!",
                                        Toast.LENGTH_SHORT).show();
                                // Clear search and refresh my groups
                                etSearch.setText("");
                                searchResultsScroll.setVisibility(View.GONE);
                                searchResultsContainer.removeAllViews();
                                createGroups();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(activity,
                                            "Failed to join: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show());
                });
    }

    private void joinGroupFromMenu(String groupId) {
        String uid = activity.user.getUid();

        activity.db.collection("groups").document(groupId).get()
                .addOnSuccessListener(doc -> {
                    String groupName = doc.getString("name");

                    // Check if already a member
                    activity.db.collection("groups_to_users_link")
                            .whereEqualTo("group_id", groupId)
                            .whereEqualTo("user_id", uid)
                            .get()
                            .addOnSuccessListener(qs -> {
                                if (!qs.isEmpty()) {
                                    Toast.makeText(activity,
                                            "You're already in " + groupName,
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                Map<String, Object> link = new HashMap<>();
                                link.put("group_id", groupId);
                                link.put("user_id", uid);

                                activity.db.collection("groups_to_users_link").document()
                                        .set(link)
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(activity,
                                                    "Joined " + groupName + "!",
                                                    Toast.LENGTH_SHORT).show();
                                            createGroups();
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(activity,
                                                        "Failed to join: " + e.getMessage(),
                                                        Toast.LENGTH_SHORT).show());
                            });
                });
    }
    private void leaveGroup(View group){
        String groupId = Objects.requireNonNull(groups.get(group));
        activity.db.collection("groups_to_users_link")
                .whereEqualTo("group_id", groupId)
                .whereEqualTo("user_id", activity.user.getUid())
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query) {
                        doc.getReference().delete();
                    }
                    groupsContainer.removeView(group);
                    groups.remove(group);
                    Toast.makeText(activity, "Left group", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(activity, "Failed to leave group", Toast.LENGTH_SHORT).show());
    }

    /// SEARCH AND IT'S RESULTS
    private void searchGroups(String query) {
        activity.db.collection("groups")
                .get()
                .addOnSuccessListener(snapshot -> {
                    searchResultsContainer.removeAllViews();
                    boolean anyResult = false;

                    for (DocumentSnapshot doc : snapshot) {
                        String name = doc.getString("name");
                        if (name != null && name.toLowerCase()
                                .contains(query.toLowerCase())) {
                            addSearchResultCard(doc);
                            anyResult = true;
                        }
                    }

                    searchResultsScroll.setVisibility(
                            anyResult ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(activity, "Search failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void addSearchResultCard(DocumentSnapshot doc) {
        if (!isAdded()) return;
        View card = getLayoutInflater().inflate(R.layout.view_group,
                searchResultsContainer, false);

        ImageView ivGroupPhoto = card.findViewById(R.id.ivGroupPhoto);
        TextView tvName = card.findViewById(R.id.groupName);
        TextView tvCount = card.findViewById(R.id.groupCount);
        TextView tvNotif = card.findViewById(R.id.groupNotification);
        View optionsBtn = card.findViewById(R.id.options);

        optionsBtn.setVisibility(View.VISIBLE);
        tvNotif.setVisibility(View.GONE);

        tvName.setText(doc.getString("name"));
        loadGroupPhoto(doc.getString("photoUrl"), ivGroupPhoto);

        activity.db.collection("groups_to_users_link")
                .whereEqualTo("group_id", doc.getId()).get()
                .addOnSuccessListener(qs ->
                        tvCount.setText(qs.size() + " members"));

        optionsBtn.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(requireContext(), v);
            menu.getMenuInflater().inflate(R.menu.menu_delete, menu.getMenu());
            menu.getMenu().findItem(R.id.action_delete).setVisible(false);
            menu.getMenu().findItem(R.id.action_join).setVisible(true);

            menu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_join) {
                    joinGroup(doc.getId(), doc.getString("name"));
                    return true;
                }
                return false;
            });
            menu.show();
        });

        searchResultsContainer.addView(card);
    }


    //helper
    private void loadGroupPhoto(String photoUrl, ImageView target) {
        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(this)
                    .load(photoUrl)
                    .transform(new CircleCrop())
                    .placeholder(R.drawable.post_frame)
                    .into(target);
        } else {
            target.setImageResource(R.drawable.post_frame);
        }
    }

    private void openGroupFeed(String groupId, String groupName) {
        HomeFragments home = HomeFragments.newInstanceWithGroup(groupId, groupName);
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentLayout, home)
                .addToBackStack(null)
                .commit();
    }
}
