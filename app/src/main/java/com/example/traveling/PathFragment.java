package com.example.traveling;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class PathFragment extends Fragment {
    public PathFragment() {
        // Required empty public constructor
    }


    Button btn_family, btn_disabled, btn_dynamic, btn_heat, btn_cold, btn_rain;
    View view;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_path, container, false);
        init();
        return view;
    }
    private void init() {
        btn_disabled        = view.findViewById(R.id.btn_disabled);
        btn_family          = view.findViewById(R.id.btn_family);
        btn_dynamic         = view.findViewById(R.id.btn_dynamic);
        btn_heat            = view.findViewById(R.id.btn_heat);
        btn_cold            = view.findViewById(R.id.btn_cold);
        btn_rain            = view.findViewById(R.id.btn_rain);
        btn_family.setSelected(true);
    }
}