package com.example.traveling;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PathFragment extends Fragment {
    public PathFragment() {
        // Required empty public constructor
    }


    Button btn_family, btn_disabled, btn_dynamic, btn_heat, btn_cold, btn_rain;
    LinearLayout btn_food, btn_culture, btn_discovery, btn_activities, btn_search, visitLayout;
    EditText et_mandatory_visit, et_budget, et_duration;
    View addVisit;
    View view;

    int effortLevel = 1;
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
        addVisit            = view.findViewById(R.id.addVisit);
        visitLayout         = view.findViewById(R.id.visitLayout);
        et_mandatory_visit  = view.findViewById(R.id.et_mandatory_visit);
        et_budget           = view.findViewById(R.id.et_budget);
        et_duration         = view.findViewById(R.id.et_duration);
        btn_search          = view.findViewById(R.id.btn_search);
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
        addVisit.setOnClickListener(v       -> fn_add_visit());
        btn_search.setOnClickListener(v     -> fn_search());
    }

    private void fn_inverse(View v){
        v.setSelected(!v.isSelected());
    }

    private void fn_group_modified(View v) {
        btn_dynamic.setSelected(false);
        btn_family.setSelected(false);
        btn_disabled.setSelected(false);

        if(v == btn_disabled) effortLevel = 0;
        if(v == btn_family) effortLevel = 1;
        if(v == btn_dynamic) effortLevel = 2;
        v.setSelected(true);
    }

    private void rm_view(View v){
        ((ViewGroup)v.getParent()).removeView(v);
    }

    private void fn_search(){
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        intent.putExtra("food", btn_food.isSelected());
        intent.putExtra("culture", btn_culture.isSelected());
        intent.putExtra("discovery", btn_discovery.isSelected());
        intent.putExtra("activities", btn_activities.isSelected());
        if(!et_budget.getText().toString().isEmpty()){
            intent.putExtra("budget", Float.parseFloat(et_budget.getText().toString()));
        }else{
            intent.putExtra("budget", -1);
        }
        if(!et_duration.getText().toString().isEmpty()) {
            intent.putExtra("duration", Float.parseFloat(et_duration.getText().toString()));
        }
        else{
            intent.putExtra("duration", -1);
        }
        intent.putExtra("effort", effortLevel);
        intent.putExtra("cold", btn_cold.isSelected());
        intent.putExtra("heat", btn_heat.isSelected());
        intent.putExtra("rain", btn_rain.isSelected());
        startActivity(intent);
    }

    private void fn_add_visit(){
        if(!et_mandatory_visit.getText().toString().isEmpty()){
            LinearLayout newVisit = (LinearLayout)getLayoutInflater().inflate(R.layout.added_visit, visitLayout, false);
            TextView tv_new_visit = newVisit.findViewById(R.id.tv_new_visit);
            View btn_remove = newVisit.findViewById(R.id.btn_remove);
            btn_remove.setOnClickListener(v -> rm_view(newVisit));
            tv_new_visit.setText(et_mandatory_visit.getText());
            et_mandatory_visit.setText("");
            visitLayout.addView(newVisit);
        }
    }
}