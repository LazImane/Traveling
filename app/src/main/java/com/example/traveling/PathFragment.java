package com.example.traveling;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

public class PathFragment extends Fragment {
    public PathFragment() {
        // Required empty public constructor
    }


    Button btn_family, btn_disabled, btn_dynamic, btn_heat, btn_cold, btn_rain;
    LinearLayout btn_food, btn_culture, btn_discovery, btn_activities;
    View view;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_path, container, false);
        init();
        setListeners();
        btn_family.setSelected(true);
        btn_food.setSelected(true);
        return view;
    }
    private void init() {
        btn_disabled        = view.findViewById(R.id.btn_disabled);
        btn_family          = view.findViewById(R.id.btn_family);
        btn_dynamic         = view.findViewById(R.id.btn_dynamic);
        btn_heat            = view.findViewById(R.id.btn_heat);
        btn_cold            = view.findViewById(R.id.btn_cold);
        btn_rain            = view.findViewById(R.id.btn_rain);
        btn_food            = view.findViewById(R.id.Restaurants);
        btn_culture         = view.findViewById(R.id.Culture);
        btn_discovery       = view.findViewById(R.id.Discovery);
        btn_activities      = view.findViewById(R.id.Activities);
    }

    private void setListeners() {
        btn_disabled.setOnClickListener(v   -> fn_group_modified(btn_disabled));
        btn_family.setOnClickListener(v     -> fn_group_modified(btn_family));
        btn_dynamic.setOnClickListener(v    -> fn_group_modified(btn_dynamic));
        btn_heat.setOnClickListener(v       -> fn_inverse(btn_heat));
        btn_cold.setOnClickListener(v       -> fn_inverse(btn_cold));
        btn_rain.setOnClickListener(v       -> fn_inverse(btn_rain));
        btn_food.setOnClickListener(v       -> fn_inverse(btn_food));
        btn_culture.setOnClickListener(v    -> fn_inverse(btn_culture));
        btn_discovery.setOnClickListener(v  -> fn_inverse(btn_discovery));
        btn_activities.setOnClickListener(v -> fn_inverse(btn_activities));
    }

    private void fn_inverse(View v){
        v.setSelected(!v.isSelected());
    }

    private void fn_group_modified(View v) {
        btn_dynamic.setSelected(false);
        btn_family.setSelected(false);
        btn_disabled.setSelected(false);

        v.setSelected(true);
    }
}