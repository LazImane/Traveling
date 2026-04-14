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

public class Password extends AppCompatActivity {

    EditText etPassword, etConfirmPassword;
    Button btnContinue, btnBack;
    TextView tvTitle, tvSubtitle;

    String email;
    boolean isNewUser;
    FirebaseAuth mAuth;
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
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
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
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
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
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            sendVerificationEmail(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(Password.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Verification email sent to " + email, Toast.LENGTH_LONG).show();
                        goToHome();//because I am too lazy to add another activity to stop them :(
                    } else {
                        Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleSignIn(String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //sign in success
                            Log.d(TAG, "signInWithEmail:success");
                            goToHome();
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(Password.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void goToHome() {
        FirebaseUser user = mAuth.getCurrentUser();
        boolean emailVerified = user != null && user.isEmailVerified();

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("emailVerified", emailVerified);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}