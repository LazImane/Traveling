package com.example.traveling;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    ImageButton btnProfile, btnMenu;
    ImageButton navHome, navTravelPath, navPost, navGroups, navNotifications;
    EditText etSearch;
    Button filterNature, filterCity, filterMuseums, filterShops, filterAround;

    FirebaseAuth mAuth;
    boolean emailVerified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getIntentData();
        init();
        setupUI();
        setListeners();
    }

    private void getIntentData() {
        emailVerified = getIntent().getBooleanExtra("emailVerified", false);
    }

    private void init() {
        btnProfile          = findViewById(R.id.btnProfile);
        navHome             = findViewById(R.id.navHome);
        navTravelPath       = findViewById(R.id.navTravelPath);
        navPost             = findViewById(R.id.navPost);
        navGroups           = findViewById(R.id.navGroups);
        navNotifications    = findViewById(R.id.navNotifications);
        etSearch            = findViewById(R.id.etSearch);
        filterNature        = findViewById(R.id.filterNature);
        filterCity          = findViewById(R.id.filterCity);
        filterMuseums       = findViewById(R.id.filterMuseums);
        filterShops         = findViewById(R.id.filterShops);
        filterAround        = findViewById(R.id.filterAround);
        mAuth               = FirebaseAuth.getInstance();
    }

    private void setupUI() {
        if (!emailVerified) {
            Toast.makeText(this, getString(R.string.emailVerify), Toast.LENGTH_LONG).show();
        }
    }

    private void setListeners() {
        btnProfile.setOnClickListener(v -> handleProfile());

        //the footer
        navHome.setOnClickListener(v -> {
            //do nothing as i am on home
        });
        navTravelPath.setOnClickListener(v -> {
            //TODO
        });
        navPost.setOnClickListener(v -> {
            //TODO
        });
        navGroups.setOnClickListener(v -> {
            //TODO
        });
        navNotifications.setOnClickListener(v -> {
            //TODO
        });

        //TODO: Filter button
        filterNature.setOnClickListener(v -> handleFilter("nature"));
        filterCity.setOnClickListener(v -> handleFilter("city"));
        filterMuseums.setOnClickListener(v -> handleFilter("museums"));
        filterShops.setOnClickListener(v -> handleFilter("shops"));
        filterAround.setOnClickListener(v -> handleFilter("around"));
    }

    private void handleProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.isAnonymous()) {
            //we start as anonymous so profile takes you to login/signup
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, Profile.class);
            startActivity(intent);
        }
    }

    private void handleFilter(String filter) {
        //TODO
    }
}