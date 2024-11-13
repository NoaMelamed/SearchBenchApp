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
        setContentView(R.layout.activity_main);

        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(IntoActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(IntoActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        CountDownTimer timer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                TextView countdownTimerTextView = findViewById(R.id.tvCdt);
                String stCountDown = millisUntilFinished / 1000 + "";
                countdownTimerTextView.setText("Login in " + stCountDown + " seconds");
            }
            @Override
            public void onFinish() {
                // Perform any actions you need to when the timer finishes
                Intent intent = new Intent(IntoActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        };

        timer.start();

    }
}