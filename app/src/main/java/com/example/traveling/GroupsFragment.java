package com.example.traveling;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

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
    }
    private void setListeners() {
        addGroup.setOnClickListener(v -> createNewGroup());
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
        group_button.setOnClickListener(v -> activity.fn_home());
        View button = newGroup.findViewById(R.id.options);
        button.setOnClickListener(this::openPopupWindow);

        TextView groupName = group_button.findViewById(R.id.groupName);
        TextView groupCount = group_button.findViewById(R.id.groupCount);
        TextView groupNotification = group_button.findViewById(R.id.groupNotification);

        activity.db.collection("groups").document(groupId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        groupName.setText(doc.getString("name"));

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
        groupsContainer.addView(newGroup);
    }

    private void openPopupWindow(View v) {
        PopupMenu menu = new PopupMenu(requireContext(), v);
        menu.getMenuInflater().inflate(R.menu.menu_delete, menu.getMenu());

        menu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete) {
                View group = (View) v.getParent();
                leaveGroup(group);
                return true;
            }
            return false;
        });

        menu.show();
    }

    private void leaveGroup(View group){
        groupsContainer.removeView(group);
        String groupId = Objects.requireNonNull(groups.get(group));
        activity.db.collection("groups").document(groupId).delete();
        activity.db.collection("groups_to_users_link")
                .whereEqualTo("group_id", groupId)
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query) {
                        doc.getReference().delete();
                    }
                });
    }
}