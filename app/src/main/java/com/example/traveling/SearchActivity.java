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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SearchActivity extends AppCompatActivity {
    ImageView btn_back, btn_reload;
    LinearLayout layout_results;
    boolean cold, heat, rain;
    List<Integer> activity_type;
    int effort;
    float budget, duration;
    Double start_lat, start_lon;
    Random random = new Random();
    List<SearchInfo> restaurants = new ArrayList<>();
    List<SearchInfo> culture = new ArrayList<>();
    List<SearchInfo> discovery = new ArrayList<>();
    List<SearchInfo> activities = new ArrayList<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        init();
        getExtras();
        setListeners();
        //createTravels();
        loadAllCategories();
    }


    private void getExtras() {
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            start_lat = extras.getDouble("start_lat");
            start_lon = extras.getDouble("start_lon");
            activity_type = new ArrayList<>();
            if(extras.getBoolean("food")) activity_type.add(1);
            if(extras.getBoolean("culture")) activity_type.add(2);
            if(extras.getBoolean("discovery")) activity_type.add(3);
            if(extras.getBoolean("activities")) activity_type.add(4);
            budget = extras.getFloat("budget");
            duration = extras.getFloat("duration");
            effort = extras.getInt("effort");
            cold = extras.getBoolean("cold");
            heat = extras.getBoolean("heat");
            rain = extras.getBoolean("rain");
        }
    }

    private void onAllDataLoaded() {
        // called when everything is ready
        Toast.makeText(this, "All data loaded", Toast.LENGTH_SHORT).show();
        System.out.println("travels created");
        System.out.println(restaurants.size());
        System.out.println(culture.size());
        System.out.println(discovery.size());
        System.out.println(activities.size());
//        createTravels(); // or whatever you want to trigger next
    }

    private void loadAllCategories() {

        ExecutorService exec = Executors.newFixedThreadPool(4);

        Future<List<SearchInfo>> restaurantsFuture = exec.submit(() ->
                MVPMapSearch.search_near_coordinates_thread(
                        this, 1, 800, start_lat, start_lon
                )
        );

        Future<List<SearchInfo>> cultureFuture = exec.submit(() ->
                MVPMapSearch.search_near_coordinates_thread(
                        this, 2, 800, start_lat, start_lon
                )
        );

        Future<List<SearchInfo>> discoveryFuture = exec.submit(() ->
                MVPMapSearch.search_near_coordinates_thread(
                        this, 3, 800, start_lat, start_lon
                )
        );

        Future<List<SearchInfo>> activitiesFuture = exec.submit(() ->
                MVPMapSearch.search_near_coordinates_thread(
                        this, 4, 800, start_lat, start_lon
                )
        );

        executor.execute(() -> {
            try {

                restaurants = restaurantsFuture.get();
                culture = cultureFuture.get();
                discovery = discoveryFuture.get();
                activities = activitiesFuture.get();

                runOnUiThread(this::onAllDataLoaded);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                exec.shutdown();
            }
        });
    }

    public List<Integer> createRandomizedList(int m) {
        boolean containsOne = activity_type.contains(1);
        if (containsOne && m < 1) {
            throw new IllegalArgumentException("m must be at least 1");
        }
        Random random = new Random();
        List<Integer> result = new ArrayList<>();
        List<Integer> withoutOne = new ArrayList<>(activity_type);
        withoutOne.remove(Integer.valueOf(1));
        for (int i = 0; i < m; i++) {
            if (containsOne && result.size() == m - 1) {
                result.add(1);
                break;
            }
            int value;
            if (withoutOne.isEmpty()) {
                value = 1;
            } else {
                value = withoutOne.get(random.nextInt(withoutOne.size()));
            }
            result.add(value);
        }
        Collections.shuffle(result);
        return result;
    }

    private Future<List<SearchInfo>> createTravelAsync(List<Integer> passage, SearchInfo startingLocation) {
        return executor.submit(() -> createTravel(passage, startingLocation));
    }private List<SearchInfo> createTravel(List<Integer> passage, SearchInfo startingLocation) {
        if (passage.isEmpty()) return new ArrayList<>();

        List<SearchInfo> results =
                MVPMapSearch.search_near_coordinates_thread(
                        this,
                        passage.get(0),
                        800,
                        startingLocation.lat,
                        startingLocation.lon
                );
        if (results.isEmpty()) {
            return new ArrayList<>();
        }
        SearchInfo next_travel = results.get(random.nextInt(results.size()));
        System.out.println(next_travel.lat + " " + next_travel.lon + " " + next_travel.loc_name);
        List<SearchInfo> travel =
                createTravel(
                        passage.subList(1, passage.size()),
                        next_travel
                );

        travel.add(0, next_travel);

        return travel;
    }

    private void createTravels() {
        ///List<Integer> passage = createRandomizedList(3);
        List<Integer> passage = List.of(1, 2, 3, 4);
        Future<List<SearchInfo>> future = createTravelAsync(passage, new SearchInfo(start_lat, start_lon, ""));

        executor.execute(() -> {
            try {

                List<SearchInfo> result = future.get();

                runOnUiThread(() -> {
                    System.out.println(result.get(0).lat + " " + result.get(0).lon);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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