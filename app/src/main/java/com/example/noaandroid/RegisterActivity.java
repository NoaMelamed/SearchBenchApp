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

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);

        Button registerButton = findViewById(R.id.register_button);

        FirebaseAuth auth;
        auth = FirebaseAuth.getInstance();
        registerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText etEmailRegister = findViewById(R.id.et_email_register);
                EditText etNameRegister = findViewById(R.id.et_name_register);
                EditText etPwdRegister = findViewById(R.id.et_password_register);
                EditText etRePwdRegister = findViewById(R.id.et_rePassword_register);
                EditText etPhoneRegister = findViewById(R.id.et_phone_register);

                String nameRegister = etNameRegister.getText().toString();
                String pwdRegister = etPwdRegister.getText().toString();
                String rePwdRegister = etRePwdRegister.getText().toString();
                String phoneRegister = etPhoneRegister.getText().toString();
                String emailRegister = etEmailRegister.getText().toString();

                // Validate password and confirm password
                if (pwdRegister.isEmpty() || pwdRegister.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "INVALID PASSWORD. Password must be at least 6 characters long.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!pwdRegister.equals(rePwdRegister)) {
                    Toast.makeText(RegisterActivity.this, "PASSWORDS DON'T MATCH", Toast.LENGTH_LONG).show();
                    return;
                }

                // Proceed with Firebase registration
                auth.createUserWithEmailAndPassword(emailRegister, pwdRegister)
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Registration successful, show a success dialog or navigate to the next activity
                                    showRegistrationAlertDialog();
                                } else {
                                    // Registration failed, check if it's due to email collision
                                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                        // Email already exists
                                        Toast.makeText(RegisterActivity.this, "A user with this email already exists.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Handle other registration errors
                                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
            }
        });

        TextView toMainButton = findViewById(R.id.regToMain);
        toMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, IntoActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showRegistrationAlertDialog() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("Welcome to SearchMyBench!");
        adb.setMessage("Glad you joined in! Now let's find you a bench");
        adb.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        adb.setCancelable(false); // Prevent dialog from being dismissed on outside touch or back press
        AlertDialog dialog = adb.create();
        dialog.show();
    }
}
