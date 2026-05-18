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
import com.google.firebase.firestore.Query;

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
        if (activity.mAuth.getCurrentUser() == null
                || activity.mAuth.getCurrentUser().isAnonymous()) {
            // Show a friendly message instead of crashing
            View emptyState = getLayoutInflater().inflate(
                    R.layout.view_notification, notifContainer, false);
            TextView tvMessage = emptyState.findViewById(R.id.tvNotifMessage);
            TextView tvDate    = emptyState.findViewById(R.id.tvNotifDate);
            View btnDismiss    = emptyState.findViewById(R.id.btnDismissNotif);
            tvMessage.setText("Sign in to receive notifications");
            tvDate.setVisibility(View.GONE);
            btnDismiss.setVisibility(View.GONE);
            notifContainer.addView(emptyState);
            return;
        }

        String uid = activity.user.getUid();

        activity.db.collection("notifications")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(qs -> {
                    notifContainer.removeAllViews();
                    if (qs.isEmpty()) {
                        Toast.makeText(activity, "Aucune notification",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    for (DocumentSnapshot doc : qs) {
                        String message = doc.getString("message");
                        String postId  = doc.getString("postId");
                        String type    = doc.getString("type");
                        Boolean read   = doc.getBoolean("read");
                        com.google.firebase.Timestamp ts = doc.getTimestamp("timestamp");

                        createNotif(doc.getId(), message, postId, type,
                                Boolean.TRUE.equals(read), ts);
                    }

                    activity.updateNotifBadge();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(activity, "Erreur: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

//    private void check_group_notifs(String groupId){
//        activity.db.collection("notifications")
//                .whereEqualTo("group_id", groupId)
//                .get()
//                .addOnSuccessListener(querySnapshot -> {
//                    for (DocumentSnapshot doc : querySnapshot) {
//
//                        String notifType = doc.getString("type");
//
//                        if (notifType != null) {
//                            createNotif(groupId, notifType);
//                        }
//                    }
//                });
//    }

    private void createNotif(String notifId, String message, String postId,
                             String type, boolean read,
                             com.google.firebase.Timestamp ts) {
        if (!isAdded()) return;
        View card = getLayoutInflater().inflate(R.layout.view_notification,
                notifContainer, false);

        TextView tvMessage = card.findViewById(R.id.tvNotifMessage);
        TextView tvDate    = card.findViewById(R.id.tvNotifDate);
        View btnDismiss    = card.findViewById(R.id.btnDismissNotif);

        tvMessage.setText(message);

        if (ts != null) {
            String date = new java.text.SimpleDateFormat("MMM dd, HH:mm",
                    java.util.Locale.getDefault())
                    .format(ts.toDate());
            tvDate.setText(date);
        }

        // Dim if already read
        card.setAlpha(read ? 0.6f : 1.0f);

        // Tap → open the post and mark as read
        card.setOnClickListener(v -> {
            markAsRead(notifId);
            card.setAlpha(0.6f);
            if (postId != null) {
                PostItem stub = new PostItem();
                stub.setFirestoreId(postId);
                PostdetailFragment detail = PostdetailFragment.newInstance(stub);
                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentLayout, detail)
                        .addToBackStack(null)
                        .commit();
            }
        });

        // Dismiss (delete)
        btnDismiss.setOnClickListener(v -> {
            notifContainer.removeView(card);
            activity.db.collection("notifications").document(notifId).delete();
        });

        notifContainer.addView(card);
    }

//    private void createNotif(){
//        LinearLayout newNotif = (LinearLayout)getLayoutInflater().inflate(R.layout.view_group, notifContainer, false);
//        View group_button = newNotif.findViewById(R.id.group_button);
//        group_button.setOnClickListener(v -> activity.fn_home());
//        View button = newNotif.findViewById(R.id.options);
//        button.setOnClickListener(this::openPopupWindow);
//        notifContainer.addView(newNotif);
//    }

//    private void openPopupWindow(View v) {
//        PopupMenu menu = new PopupMenu(requireContext(), v);
//        menu.getMenuInflater().inflate(R.menu.menu_delete, menu.getMenu());
//
//        menu.setOnMenuItemClickListener(item -> {
//            if (item.getItemId() == R.id.action_delete) {
//                View notif = (View) v.getParent();
//                notifContainer.removeView(notif);
//                return true;
//            }
//            return false;
//        });
//
//        menu.show();
//    }

    private void markAsRead(String notifId) {
        activity.db.collection("notifications").document(notifId)
                .update("read", true);
    }
}