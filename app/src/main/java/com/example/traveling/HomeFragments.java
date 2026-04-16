package com.example.traveling;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class HomeFragments extends Fragment {
    EditText etSearch;
    Button filterNature, filterCity, filterMuseums, filterShops, filterAround;
    View view;

    public HomeFragments() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        init();
        setListeners();
        return view;
    }
    private void init() {

        etSearch            = view.findViewById(R.id.etSearch);
        filterNature        = view.findViewById(R.id.filterNature);
        filterCity          = view.findViewById(R.id.filterCity);
        filterMuseums       = view.findViewById(R.id.filterMuseums);
        filterShops         = view.findViewById(R.id.filterShops);
        filterAround        = view.findViewById(R.id.filterAround);
    }
    private void setListeners() {

        filterNature.setOnClickListener(v -> handleFilter("nature"));
        filterCity.setOnClickListener(v -> handleFilter("city"));
        filterMuseums.setOnClickListener(v -> handleFilter("museums"));
        filterShops.setOnClickListener(v -> handleFilter("shops"));
        filterAround.setOnClickListener(v -> handleFilter("around"));
    }

    private void handleFilter(String filter) {
        //TODO
    }
}