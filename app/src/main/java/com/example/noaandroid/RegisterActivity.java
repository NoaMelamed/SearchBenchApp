package com.example.noaandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

/**
 * Activity for user registration.
 * Handles user input, validates credentials, and registers the user with Firebase Authentication.
 */
public class RegisterActivity extends AppCompatActivity {

    // Initialize Firebase Authentication instance.
    FirebaseAuth auth;

    /**
     * Called when the activity is created. Initializes Firebase, sets up button listeners,
     * and handles navigation between the registration, sign-in, and intro activities.
     *
     * @param savedInstanceState The saved instance state, if available.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);

        // Reference to the register button in the layout.
        Button registerButton = findViewById(R.id.register_button);

        // Initialize Firebase Authentication instance.
        auth = FirebaseAuth.getInstance();

        // Set up the click listener for the register button.
        registerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                registerUser(); // Call the method to register the user
            }
        });

        // Set up a click listener for navigating back to the main activity.
        TextView toMainButton = findViewById(R.id.regToMain);
        toMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the intro activity.
                Intent intent = new Intent(RegisterActivity.this, IntoActivity.class);
                startActivity(intent);
            }
        });

        // Set up a click listener for navigating to the sign-in activity.
        TextView toSignInButton = findViewById(R.id.tv_signin);
        toSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the sign-in activity.
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Registers a new user with Firebase Authentication. Validates user input for email and password,
     * then attempts to create the user with Firebase's `createUserWithEmailAndPassword` method.
     *
     * Displays error messages if validation fails or if Firebase registration fails.
     */
    private void registerUser() {
        // Get user inputs from the form.
        EditText etEmailRegister = findViewById(R.id.et_email_register);
        EditText etNameRegister = findViewById(R.id.et_name_register);
        EditText etPwdRegister = findViewById(R.id.et_password_register);
        EditText etRePwdRegister = findViewById(R.id.et_rePassword_register);

        // Retrieve text input from EditText fields.
        String nameRegister = etNameRegister.getText().toString();
        String pwdRegister = etPwdRegister.getText().toString();
        String rePwdRegister = etRePwdRegister.getText().toString();
        String emailRegister = etEmailRegister.getText().toString();

        // Validate password: check if it's at least 6 characters long.
        if (pwdRegister.isEmpty() || pwdRegister.length() < 6) {
            Toast.makeText(RegisterActivity.this, "INVALID PASSWORD. Password must be at least 6 characters long.", Toast.LENGTH_LONG).show();
            return;
        }

        // Validate that the passwords match.
        if (!pwdRegister.equals(rePwdRegister)) {
            Toast.makeText(RegisterActivity.this, "PASSWORDS DON'T MATCH", Toast.LENGTH_LONG).show();
            return;
        }

        // Register the user with Firebase Authentication.
        auth.createUserWithEmailAndPassword(emailRegister, pwdRegister)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registration successful, show a welcome dialog or navigate to the next activity.
                            showRegistrationAlertDialog();
                        } else {
                            // Handle registration errors.
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                // Email is already registered.
                                Toast.makeText(RegisterActivity.this, "A user with this email already exists.", Toast.LENGTH_SHORT).show();
                            } else {
                                // Handle other registration errors.
                                Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    /**
     * Displays an alert dialog welcoming the user after successful registration.
     * Provides a button to navigate to the home activity.
     */
    private void showRegistrationAlertDialog() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("Welcome to SearchMyBench!");
        adb.setMessage("Glad you joined in! Now let's find you a bench");
        adb.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Navigate to the home activity and finish this activity.
                Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        adb.setCancelable(false); // Prevent dialog from being dismissed by outside touch or back press.
        AlertDialog dialog = adb.create();
        dialog.show();
    }
}
