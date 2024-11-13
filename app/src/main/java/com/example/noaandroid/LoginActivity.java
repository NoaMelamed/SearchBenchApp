package com.example.noaandroid;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        // מופע של המחלקה
        auth = FirebaseAuth.getInstance();

        Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText etEmailLogin = findViewById(R.id.et_email_login);
                EditText etPasswordLogin = findViewById(R.id.et_password_login);
                String emailLogin = etEmailLogin.getText().toString();
                String passwordLogin = etPasswordLogin.getText().toString();
                //
                loginClient(emailLogin, passwordLogin);
            }});




        TextView toRegButton = findViewById(R.id.signToReg);
        toRegButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        TextView toMainButton = findViewById(R.id.signToMain);
        toMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, IntoActivity.class);
                startActivity(intent);
            }
        });

        private void loginClient(String emailLogin, String passwordLogin) {
            auth.signInWithEmailAndPassword(emailLogin, passwordLogin).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    // NEXT ACTIVITY
                }
            });
        }
    }
}