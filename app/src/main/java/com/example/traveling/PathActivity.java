package com.example.traveling;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.MapView;

public class PathActivity extends AppCompatActivity {
    ImageView btn_back, btn_export;
    LinearLayout layout_results;

    MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path   );
        init();
        getExtras();
        setListeners();
    }

    private void getExtras() {
        Bundle extras = getIntent().getExtras();
        if(extras != null){

        }
    }


    private void init() {
        btn_back          = findViewById(R.id.btn_back);
        btn_export        = findViewById(R.id.btn_export);
        layout_results    = findViewById(R.id.layout_results);
        mapView           = findViewById(R.id.mapView);
    }


    private void setListeners() {
        btn_back.setOnClickListener(v -> back());
        layout_results.setOnClickListener(v -> export());
    }


    private void export() {

    }

    private void back() {
        finish();
    }
}