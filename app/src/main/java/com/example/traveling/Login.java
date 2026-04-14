package com.example.traveling;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    EditText etEmail;
    Button btnContinue, btnGuest;
    LinearLayout btnGoogle, btnApple;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        init();
        setListeners();
    }

    private void init() {
        etEmail = findViewById(R.id.etEmail);
        btnContinue = findViewById(R.id.btnContinue);
        btnGuest = findViewById(R.id.btnGuest);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnApple = findViewById(R.id.btnApple);
        mAuth = FirebaseAuth.getInstance();
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
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.putExtra("emailVerified", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, getString(R.string.signInFailG), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleGoogle() {
        //TODO: Google Signin logic
    }

    private void handleApple() {
        //TODO: Apple Signin logic
    }
}