package com.example.noaandroid;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth auth; // Firebase authentication instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        // Initialize the FirebaseAuth instance
        auth = FirebaseAuth.getInstance();

        // Set up the login button click listener
        Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Retrieve email and password entered by the user
                EditText etEmailLogin = findViewById(R.id.et_email_login);
                EditText etPasswordLogin = findViewById(R.id.et_password_login);
                String emailLogin = etEmailLogin.getText().toString();
                String passwordLogin = etPasswordLogin.getText().toString();

                // Attempt to log in the user
                loginClient(emailLogin, passwordLogin);
            }
        });

        // Set up the "Sign Up" text click listener to navigate to the registration page
        TextView toRegButton = findViewById(R.id.signToReg);
        toRegButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // Set up the "Back to Main" text click listener to navigate back to the intro activity
        TextView toMainButton = findViewById(R.id.signToMain);
        toMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, IntoActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Attempts to authenticate the user with the provided email and password.
     * @param emailLogin The email entered by the user.
     * @param passwordLogin The password entered by the user.
     */
    private void loginClient(String emailLogin, String passwordLogin) {
        auth.signInWithEmailAndPassword(emailLogin, passwordLogin).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                // If login is successful, show a welcome dialog
                showLoginAlertDialog();
            }
        });
    }

    /**
     * Displays an alert dialog to welcome the user after a successful login.
     * Provides an option to navigate to the home activity.
     */
    private void showLoginAlertDialog() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("Welcome to SearchMyBench!");
        adb.setMessage("Glad you signed in! Now let's find you a bench");
        adb.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Navigate to the home activity and finish the current activity
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        adb.setCancelable(false); // Prevent dialog dismissal via outside touch or back button
        AlertDialog dialog = adb.create();
        dialog.show();
    }
}
