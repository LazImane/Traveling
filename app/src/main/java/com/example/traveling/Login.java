package com.example.traveling;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Login extends AppCompatActivity {

    EditText etEmail;
    Button btnContinue, btnGuest;
    LinearLayout btnGoogle, btnApple;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        init();
        setListeners();
    }

    private void init() {
        etEmail     = findViewById(R.id.etEmail);
        btnContinue = findViewById(R.id.btnContinue);
        btnGuest    = findViewById(R.id.btnGuest);
        btnGoogle   = findViewById(R.id.btnGoogle);
        btnApple    = findViewById(R.id.btnApple);
        mAuth       = FirebaseAuth.getInstance();
        db          = FirebaseFirestore.getInstance();
    }

    private void setListeners() {
        btnContinue.setOnClickListener(v -> handleEmailContinue());
        btnGuest.setOnClickListener(v -> handleGuest());
        btnGoogle.setOnClickListener(v -> handleGoogle());
        btnApple.setOnClickListener(v -> handleApple());
    }
    private void handleEmailContinue() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, getString(R.string.emailEnter), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isValidEmail(email)) {
            Toast.makeText(this, getString(R.string.emailValid), Toast.LENGTH_SHORT).show();
            return;
        }

        goToPassword(email);
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void goToPassword(String email) {
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                        Intent intent = new Intent(this, Password.class);
                        intent.putExtra("email", email);
                        intent.putExtra("isNewUser", isNewUser);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, getString(R.string.emailError), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void handleGuest() {
        // Disable immediately to prevent double-tap
        btnGuest.setEnabled(false);

        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user == null) {
                            btnGuest.setEnabled(true);
                            return;
                        }
                        createGuestInFirestore(user);
                    } else {
                        btnGuest.setEnabled(true);
                        Toast.makeText(this, getString(R.string.signInFailG), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createGuestInFirestore(FirebaseUser user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid",       user.getUid());
        userData.put("email",     "guest");
        userData.put("username",  generateUsername());
        userData.put("photoUrl",  "");
        userData.put("createdAt", com.google.firebase.Timestamp.now());
        userData.put("isGuest",   true);

        db.collection("users")
                .document(user.getUid())
                .set(userData)
                .addOnSuccessListener(unused -> {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("emailVerified", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    btnGuest.setEnabled(true);
                    Toast.makeText(this, getString(R.string.profile_create_failed), Toast.LENGTH_SHORT).show();
                });
    }

    /** Generates a random username like "Traveler_4821" */
    private String generateUsername() {
        int number = new Random().nextInt(9000) + 1000;
        return "Traveler_" + number;
    }

    private void handleGoogle() {
        // TODO: Google Sign-In logic
    }

    private void handleApple() {
        // TODO: Apple Sign-In logic
    }
}