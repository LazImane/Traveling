package com.example.traveling;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    ImageButton btnProfile, btnMenu;
    ImageButton navHome, navTravelPath, navPost, navGroups, navNotifications;

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
        replaceFragment(new HomeFragments());
        fn_group_modified(navHome);
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
            fn_home();
        });
        navTravelPath.setOnClickListener(v -> {
            fn_group_modified(navTravelPath);
            replaceFragment(new PathFragment());
        });
        navPost.setOnClickListener(v -> {
            fn_group_modified(navPost);
            replaceFragment(new PostFragment());
        });
        navGroups.setOnClickListener(v -> {
            fn_group_modified(navGroups);
            replaceFragment(new GroupsFragment());
        });
        navNotifications.setOnClickListener(v -> {
            fn_notif();
        });

        //TODO: Filter button
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

    public void fn_home(){
        fn_group_modified(navHome);
        replaceFragment(new HomeFragments());
    }
    public void fn_notif(){
        fn_group_modified(navNotifications);
        replaceFragment(new NotificationsFragment());
    }

    private void fn_group_modified(View v) {
        navHome.setSelected(false);
        navTravelPath.setSelected(false);
        navPost.setSelected(false);
        navGroups.setSelected(false);
        navNotifications.setSelected(false);

        v.setSelected(true);
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentLayout, fragment);
        fragmentTransaction.commit();
    }
}