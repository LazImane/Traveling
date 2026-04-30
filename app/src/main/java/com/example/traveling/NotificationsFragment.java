package com.example.traveling;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

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
        createNotif();
        createNotif();
        return view;
    }
    private void init() {
        activity = (MainActivity) getActivity();
        notifContainer = view.findViewById(R.id.notifContainer);
    }
    private void setListeners() {

    }

    private void createNotif(){
        LinearLayout newNotif = (LinearLayout)getLayoutInflater().inflate(R.layout.view_group, notifContainer, false);
        View group_button = newNotif.findViewById(R.id.group_button);
        group_button.setOnClickListener(v -> activity.fn_home());
        View button = newNotif.findViewById(R.id.options);
        button.setOnClickListener(this::openPopupWindow);
        notifContainer.addView(newNotif);
    }

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