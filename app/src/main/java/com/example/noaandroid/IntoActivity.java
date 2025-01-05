package com.example.noaandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class IntoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

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

//        // Create a countdown timer that runs for 5 seconds with 1-second intervals
//        CountDownTimer timer = new CountDownTimer(5000, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                // Update the TextView with the remaining countdown time every second
//                TextView countdownTimerTextView = findViewById(R.id.tvCdt);
//                String stCountDown = millisUntilFinished / 1000 + "";
//                countdownTimerTextView.setText("Login in " + stCountDown + " seconds");
//            }
//
//            @Override
//            public void onFinish() {
//                // Automatically navigate to LoginActivity when the timer finishes
//                Intent intent = new Intent(IntoActivity.this, LoginActivity.class);
//                startActivity(intent);
//            }
//        };
//
//        // Start the countdown timer
//        timer.start();
    }
}
