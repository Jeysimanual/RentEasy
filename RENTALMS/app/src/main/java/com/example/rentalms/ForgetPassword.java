package com.example.rentalms;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgetPassword extends AppCompatActivity {

    EditText emailInput;
    Button resetPasswordButton, loginButton, createAccountButton;
    FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        // Initialize UI elements
        emailInput = findViewById(R.id.email);
        resetPasswordButton = findViewById(R.id.resetbtn);
        loginButton = findViewById(R.id.loginbtn); // Reference to the login button
        createAccountButton = findViewById(R.id.createbtn); // Reference to the create account button
        mAuth = FirebaseAuth.getInstance();

        // Handle password reset
        resetPasswordButton.setOnClickListener(view -> {
            String email = emailInput.getText().toString().trim();
            if (!email.isEmpty()) {
                resetPassword(email);
            } else {
                Toast.makeText(ForgetPassword.this, "Please enter your email", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle login button click: Redirect to Login page
        loginButton.setOnClickListener(view -> {
            Intent loginIntent = new Intent(ForgetPassword.this, Login.class);
            startActivity(loginIntent);
        });

        // Handle create account button click: Redirect to CreateAccount page
        createAccountButton.setOnClickListener(view -> {
            Intent createAccountIntent = new Intent(ForgetPassword.this, CreateAccount.class);
            startActivity(createAccountIntent);
        });
    }

    // Method to reset the password
    private void resetPassword(String email) {
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ForgetPassword.this, "Password reset link sent to your email", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ForgetPassword.this, "Failed to send reset link: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
