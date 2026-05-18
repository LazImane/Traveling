package com.example.traveling;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
            add_entry(s.loc_name + " " + s.address, s);
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

    private void add_entry(String s, SearchInfo si){
        TextView view = new TextView(this);
        view.setText(s);
        view.setPadding(10, 10, 10, 10);
        view.setOnClickListener(v->openInGoogleMaps(si.lat, si.lon));
        layout_results.addView(view);
    }

    private void openInGoogleMaps(double lat, double lon) {
        Uri uri = Uri.parse("geo:" + lat + "," + lon + "?q=" + lat + "," + lon);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // Google Maps not installed — open in browser instead
            Uri browserUri = Uri.parse("https://www.google.com/maps?q=" + lat + "," + lon);
            startActivity(new Intent(Intent.ACTION_VIEW, browserUri));
        }
    }

    private void setListeners() {
        btn_back.setOnClickListener(v -> back());
        btn_export.setOnClickListener(v -> export());
    }


    private void export() {
            // 1 — Capture the root view as a Bitmap
            View rootView = getWindow().getDecorView().getRootView();
            rootView.setDrawingCacheEnabled(true);
            Bitmap screenshot = Bitmap.createBitmap(rootView.getDrawingCache());
            rootView.setDrawingCacheEnabled(false);

            // 2 — Set up the PDF document
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                    screenshot.getWidth(),
                    screenshot.getHeight(),
                    1
            ).create();

            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            canvas.drawBitmap(screenshot, 0, 0, null);
            document.finishPage(page);

            // 3 — Write to Downloads folder
            String fileName = "export_" + System.currentTimeMillis() + ".pdf";
            File file = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    fileName
            );

            try (FileOutputStream fos = new FileOutputStream(file)) {
                document.writeTo(fos);
                document.close();
                Toast.makeText(this, "PDF saved to Downloads: " + fileName, Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                document.close();
                Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
    }

    private void back() {
        finish();
    }
}