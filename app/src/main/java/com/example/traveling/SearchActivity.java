package com.example.traveling;

import static java.lang.Double.max;

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

import org.w3c.dom.Text;

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

        for(int i = 0; i < random.nextInt(3)+3; i ++){
            List<Integer> passage = createRandomizedList((int)(duration/70));
            display_path(passage);
        }
    }

    private void loadAllCategories() {

        ExecutorService exec = Executors.newFixedThreadPool(4);

        Future<List<SearchInfo>> restaurantsFuture;
        if(activity_type.contains(1)){
            restaurantsFuture = exec.submit(() ->
                    MVPMapSearch.search_near_coordinates_thread(
                            this, 1, 800, start_lat, start_lon
                    )
            );
        } else {
            restaurantsFuture = null;
        }

        Future<List<SearchInfo>> cultureFuture;
        if(activity_type.contains(2)) {
            cultureFuture = exec.submit(() ->
                    MVPMapSearch.search_near_coordinates_thread(
                            this, 2, 800, start_lat, start_lon
                    )
            );
        } else {
            cultureFuture = null;
        }

        Future<List<SearchInfo>> discoveryFuture;
        if(activity_type.contains(3)) {
            discoveryFuture = exec.submit(() ->
                    MVPMapSearch.search_near_coordinates_thread(
                            this, 3, 800, start_lat, start_lon
                    )
            );
        } else {
            discoveryFuture = null;
        }

        Future<List<SearchInfo>> activitiesFuture;
        if(activity_type.contains(4)){
            activitiesFuture = exec.submit(() ->
                    MVPMapSearch.search_near_coordinates_thread(
                            this, 4, 800, start_lat, start_lon
                    )
            );
        } else {
            activitiesFuture = null;
        }

        executor.execute(() -> {
            try {

                if(restaurantsFuture!=null)restaurants = restaurantsFuture.get();
                if(cultureFuture!=null)culture = cultureFuture.get();
                if(discoveryFuture!=null)discovery = discoveryFuture.get();
                if(activitiesFuture!=null)activities = activitiesFuture.get();

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

    private String doubleToTime(Double d){
        double totalMinutes = d;
        int hours = (int) totalMinutes / 60;
        int minutes = (int) totalMinutes % 60;
        if(hours > 0) return hours + "h "+minutes;
        return minutes + "min";
    }

    private void display_path(List<Integer> path_composition){
        List<SearchInfo> path = generate_path(path_composition);
        path = orderShortestPath(path);
        double walkTime = 0;
        double activityTime = 0;
        double price = 0;
        for(int i = 0; i < path.size(); i ++){
            System.out.println(path.get(i).lat + " " + path.get(i).lon + " "+ path.get(i).loc_name);
            if(i > 0) walkTime += path.get(i-1).walkingTimeMinutes(path.get(i));
            activityTime += 40+random.nextDouble()*40;
            int step_type = path.get(i).type;
            switch(step_type){
                case 1: price += 10 + random.nextDouble()*20;break;
                case 2: price += max(0,-5 + random.nextDouble()*15);break;
                case 3: price += 0;break;
                case 4: price += 15 + random.nextDouble()*25; break;
            }
        }
        LinearLayout newVisit = (LinearLayout)getLayoutInflater().inflate(R.layout.travel_result, layout_results, false);
        TextView txt_time = newVisit.findViewById(R.id.txt_time);
        TextView txt_budget = newVisit.findViewById(R.id.txt_budget);
        TextView txt_walk = newVisit.findViewById(R.id.txt_walk);
        txt_time.setText("Time :\n"+doubleToTime(activityTime+walkTime));
        txt_budget.setText("Budget :\n"+(int)price+"€");
        txt_walk.setText("Walk :\n"+doubleToTime(walkTime));
        List<SearchInfo> finalPath = path;
        newVisit.setOnClickListener(v -> open_path(finalPath));
        layout_results.addView(newVisit);

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
            layout_results.removeAllViews();
            for(int i = 0; i < random.nextInt(3)+3; i ++){
                List<Integer> passage = createRandomizedList((int)(duration/70));
                display_path(passage);
            }
        }

        private void back() {
            finish();
        }

    private void open_path(List<SearchInfo> path){
        Intent intent = new Intent(this, PathActivity.class);
        intent.putExtra("path", new ArrayList<>(path));
        startActivity(intent);
    }

    private List<SearchInfo> generate_path(List<Integer> types) {

        List<SearchInfo> result = new ArrayList<>();

        List<SearchInfo> available1 = new ArrayList<>(restaurants);
        List<SearchInfo> available2 = new ArrayList<>(culture);
        List<SearchInfo> available3 = new ArrayList<>(discovery);
        List<SearchInfo> available4 = new ArrayList<>(activities);

        for (int type : types) {

            List<SearchInfo> available;
            switch (type) {
                case 1:
                    available = available1;
                    break;

                case 2:
                    available = available2;
                    break;

                case 3:
                    available = available3;
                    break;

                case 4:
                    available = available4;
                    break;

                default:
                    throw new IllegalArgumentException("Invalid type: " + type);
            }
            if (available.isEmpty()) {
                continue;
            }
            int randomPos = random.nextInt(available.size());
            SearchInfo chosen = available.remove(randomPos);
            chosen.type = type;
            result.add(chosen);
        }

        return result;
    }


    public static List<SearchInfo> orderShortestPath(List<SearchInfo> input) {

        if (input.isEmpty()) return new ArrayList<>();

        List<SearchInfo> remaining = new ArrayList<>(input);
        List<SearchInfo> ordered = new ArrayList<>();

        // start from first element (or you can randomize start)
        SearchInfo current = remaining.remove(0);
        ordered.add(current);

        while (!remaining.isEmpty()) {

            SearchInfo nearest = null;
            double bestTime = Double.MAX_VALUE;

            for (SearchInfo candidate : remaining) {

                double time = current.walkingTimeMinutes(candidate);

                if (time < bestTime) {
                    bestTime = time;
                    nearest = candidate;
                }
            }

            ordered.add(nearest);
            remaining.remove(nearest);
            current = nearest;
        }

        return ordered;
    }
}