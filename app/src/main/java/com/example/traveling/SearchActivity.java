package com.example.traveling;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class SearchActivity extends AppCompatActivity {
    ImageView btn_back, btn_reload;
    LinearLayout layout_results;
    boolean food, culture, discovery, activities, cold, heat, rain;
    int effort;
    float budget, duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        init();
        getExtras();
        setListeners();
        createTravels();
    }

    private void getExtras() {
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            food = extras.getBoolean("food");
            culture = extras.getBoolean("culture");
            discovery = extras.getBoolean("discovery");
            activities = extras.getBoolean("activities");
            budget = extras.getFloat("budget");
            duration = extras.getFloat("duration");
            effort = extras.getInt("effort");
            cold = extras.getBoolean("cold");
            heat = extras.getBoolean("heat");
            rain = extras.getBoolean("rain");
        }
    }

    private void createTravels() {
        for(int i = 0; i < 3; i ++){
            LinearLayout newVisit = (LinearLayout)getLayoutInflater().inflate(R.layout.travel_result, layout_results, false);
            newVisit.setOnClickListener(v -> display_path());
            layout_results.addView(newVisit);
        }
    }


    private void init() {
        btn_back          = findViewById(R.id.btn_back);
        btn_reload        = findViewById(R.id.btn_reload);
        layout_results    = findViewById(R.id.layout_results);
    }


    private void setListeners() {
        btn_back.setOnClickListener(v -> back());
        btn_reload.setOnClickListener(v -> reload());
    }

        private void reload() {

        }

        private void back() {
            finish();
        }

    private void display_path(){
        Intent intent = new Intent(this, PathActivity.class);
        startActivity(intent);
    }
}