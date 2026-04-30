package com.example.traveling;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

public class GroupsFragment extends Fragment {
    View view;
    LinearLayout groupsContainer;

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
        createGroup();
        createGroup();
        createGroup();
        return view;
    }
    private void init() {
        activity = (MainActivity) getActivity();
        groupsContainer = view.findViewById(R.id.groupsContainer);
    }
    private void setListeners() {

    }

    private void createGroup(){
        LinearLayout newGroup = (LinearLayout)getLayoutInflater().inflate(R.layout.view_group, groupsContainer, false);
        View group_button = newGroup.findViewById(R.id.group_button);
        group_button.setOnClickListener(v -> activity.fn_home());
        View button = newGroup.findViewById(R.id.options);
        button.setOnClickListener(this::openPopupWindow);
        groupsContainer.addView(newGroup);
    }

    private void openPopupWindow(View v) {
        PopupMenu menu = new PopupMenu(requireContext(), v);
        menu.getMenuInflater().inflate(R.menu.menu_delete, menu.getMenu());

        menu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete) {
                View group = (View) v.getParent();
                groupsContainer.removeView(group);
                return true;
            }
            return false;
        });

        menu.show();
    }
}