package com.example.traveling;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Password extends AppCompatActivity {

    EditText etPassword, etConfirmPassword;
    Button btnContinue, btnBack;
    TextView tvTitle, tvSubtitle;

    String email;
    boolean isNewUser;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    private static final String TAG = "Password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password);
        getIntentData();
        init();
        setupUI();
        setListeners();
    }

    private void getIntentData() {
        email     = getIntent().getStringExtra("email");
        isNewUser = getIntent().getBooleanExtra("isNewUser", false);
    }

    private void init() {
        etPassword        = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnContinue       = findViewById(R.id.btnContinue);
        btnBack           = findViewById(R.id.btnBack);
        tvTitle           = findViewById(R.id.tvTitle);
        tvSubtitle        = findViewById(R.id.tvSubtitle);
        mAuth             = FirebaseAuth.getInstance();
        db                = FirebaseFirestore.getInstance();
    }

    private void setupUI() {
        if (isNewUser) {
            tvTitle.setText(R.string.create_pswd);
            tvSubtitle.setText(R.string.choose_password);
            etConfirmPassword.setVisibility(View.VISIBLE);
        } else {
            tvTitle.setText(R.string.welcome_back);
            tvSubtitle.setText(R.string.enter_password);
            etConfirmPassword.setVisibility(View.GONE);
        }
    }

    private void setListeners() {
        btnContinue.setOnClickListener(v -> handleContinue());
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    public void onStart() {
        super.onStart();
        //go to home if not anonymous
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && !currentUser.isAnonymous()) {
            goToHome();
        }
    }

    private void handleContinue() {
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, getString(R.string.enter_password), Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, getString(R.string.pwd6carMin), Toast.LENGTH_SHORT).show();
            return;
        }

        if (isNewUser) {
            handleSignUp(password);
        } else {
            handleSignIn(password);
        }
    }

    private void handleSignUp(String password) {
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, getString(R.string.confirm_password_hint), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, getString(R.string.password_mismatch), Toast.LENGTH_SHORT).show();
            return;
        }

        btnContinue.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user == null) return;
                        createUserInFirestore(user, false);
                    } else {
                        btnContinue.setEnabled(true);
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void handleSignIn(String password) {
        btnContinue.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        goToHome();
                    } else {
                        btnContinue.setEnabled(true);
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void createUserInFirestore(FirebaseUser user, boolean isGuest) {
        String autoUsername = generateUsername();

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid",       user.getUid());
        userData.put("email",     isGuest ? "guest" : user.getEmail());
        userData.put("username",  autoUsername);
        userData.put("photoUrl",  "");
        userData.put("createdAt", com.google.firebase.Timestamp.now());
        userData.put("isGuest",   isGuest);

        db.collection("users")
                .document(user.getUid())
                .set(userData)
                .addOnSuccessListener(unused -> goToHome())
                .addOnFailureListener(e -> {
                    btnContinue.setEnabled(true);
                    Toast.makeText(this, getString(R.string.profile_create_failed), Toast.LENGTH_SHORT).show();
                });
    }

    /** Generates a random username like "Traveler_4821" */
    private String generateUsername() {
        int number = new Random().nextInt(9000) + 1000;
        return "Traveler_" + number;
    }

    private void goToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("emailVerified", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}