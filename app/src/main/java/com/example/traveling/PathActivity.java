package com.example.traveling;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.util.GeoPoint;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import java.util.ArrayList;
import java.util.List;

public class PathActivity extends AppCompatActivity {
    ImageView btn_back, btn_export;
    LinearLayout layout_results;

    List<SearchInfo> path;

    MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path   );
        init();
        getExtras();
        setListeners();
        initMap();
    }

    private void getExtras() {
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            path = (ArrayList<SearchInfo>)getIntent().getSerializableExtra("path");
        }
    }private void initMap() {
        // Required osmdroid config
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // Draw the polyline through all points
        List<GeoPoint> points = new ArrayList<>();
        for (SearchInfo s : path) {
            points.add(new GeoPoint(s.lat, s.lon));
        }

        Polyline line = new Polyline();
        line.setPoints(points);
        line.setColor(Color.BLUE);
        line.setWidth(10f);
        mapView.getOverlays().add(line);

        // Add a marker for each stop
        for (int i = 0; i < path.size(); i++) {
            SearchInfo s = path.get(i);
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(s.lat, s.lon));
            marker.setTitle((i + 1) + ". " + s.loc_name);  // "1. Louvre", "2. ..."
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(marker);
            //add_entry(s.loc_name + " " + s.address);
        }

        // Center and zoom the map to fit all points
        if (!points.isEmpty()) {
            BoundingBox box = BoundingBox.fromGeoPoints(points);
            mapView.post(() -> mapView.zoomToBoundingBox(box, true, 100));
        }

        mapView.invalidate();
    }
    private void init() {
        btn_back          = findViewById(R.id.btn_back);
        btn_export        = findViewById(R.id.btn_export);
        layout_results    = findViewById(R.id.layout_results);
        mapView           = findViewById(R.id.mapView);
    }

    private void add_entry(String s){
        TextView v = new TextView(this);
        v.setText(s);
        layout_results.addView(v);
    }
    private void setListeners() {
        btn_back.setOnClickListener(v -> back());
        //layout_results.setOnClickListener(v -> export());
    }


    private void export() {

    }

    private void back() {
        finish();
    }
}