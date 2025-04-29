package com.example.noaandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;

public class IntoActivity extends AppCompatActivity {

    /**
     * Initializes the activity, sets up the Firebase instance,
     * and sets click listeners for login and register buttons.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Initialize the login button and set up a click listener
        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Start the LoginActivity when the login button is clicked
                Intent intent = new Intent(IntoActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        // Initialize the register button and set up a click listener
        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Start the RegisterActivity when the register button is clicked
                Intent intent = new Intent(IntoActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}
