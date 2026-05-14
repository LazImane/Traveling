package com.example.traveling;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;

public class NotificationsFragment extends Fragment {
    View view;
    LinearLayout notifContainer;

    MainActivity activity;

    public NotificationsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_notifications, container, false);
        init();
        setListeners();
        createNotifs();
        return view;
    }
    private void init() {
        activity = (MainActivity) getActivity();
        notifContainer = view.findViewById(R.id.notifContainer);
    }

    private void setListeners(){

    }

    private void createNotifs() {
        activity.db.collection("groups_to_users_link")
                .whereEqualTo("user_id", activity.user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {

                        String groupId = doc.getString("group_id");

                        if (groupId != null) {
                            check_group_notifs(groupId);
                        }
                    }

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(activity, "Failed to load groups", Toast.LENGTH_SHORT).show();
                });
    }

    private void check_group_notifs(String groupId){
        activity.db.collection("notifications")
                .whereEqualTo("group_id", groupId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {

                        String notifType = doc.getString("type");

                        if (notifType != null) {
                            createNotif(groupId, notifType);
                        }
                    }
                });
    }

    private void createNotif(String groupId, String notifType){
        if(!isAdded()) return;
        LinearLayout newNotif = (LinearLayout)getLayoutInflater().inflate(R.layout.view_group, notifContainer, false);
        LinearLayout group_infos = newNotif.findViewById(R.id.group_infos);
        group_infos.setOnClickListener(v -> activity.fn_home());
        View button = newNotif.findViewById(R.id.options);
        button.setOnClickListener(this::openPopupWindow);

        TextView groupName = group_infos.findViewById(R.id.groupName);
        TextView groupCount = group_infos.findViewById(R.id.groupCount);
        TextView groupNotification = group_infos.findViewById(R.id.groupNotification);
        group_infos.removeView(groupCount);
        activity.db.collection("groups").document(groupId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        groupName.setText(doc.getString("name"));
                        groupNotification.setText(notifType);
                    }
                });
        notifContainer.addView(newNotif);

    }

//    private void createNotif(){
//        LinearLayout newNotif = (LinearLayout)getLayoutInflater().inflate(R.layout.view_group, notifContainer, false);
//        View group_button = newNotif.findViewById(R.id.group_button);
//        group_button.setOnClickListener(v -> activity.fn_home());
//        View button = newNotif.findViewById(R.id.options);
//        button.setOnClickListener(this::openPopupWindow);
//        notifContainer.addView(newNotif);
//    }

    private void openPopupWindow(View v) {
        PopupMenu menu = new PopupMenu(requireContext(), v);
        menu.getMenuInflater().inflate(R.menu.menu_delete, menu.getMenu());

        menu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete) {
                View notif = (View) v.getParent();
                notifContainer.removeView(notif);
                return true;
            }
            return false;
        });

        menu.show();
    }
}