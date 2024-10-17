package com.example.rentalms;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LandlordLogin extends AppCompatActivity {

    // UI Components
    EditText landlordemail, landlordPassword;
    Button landlordloginbtn;
    TextView Landlordcreate;

    // Firebase Authentication
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landlord_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        landlordemail = findViewById(R.id.landlordemail);
        landlordPassword = findViewById(R.id.landlordPassword);
        landlordloginbtn = findViewById(R.id.landlordloginbtn);
        Landlordcreate = findViewById(R.id.Landlordcreate);

        // Set up the listener for the create account link
        Landlordcreate.setOnClickListener(view -> {
            startActivity(new Intent(LandlordLogin.this, LanlordRegister.class));
        });

        // Set up login button click listener
        landlordloginbtn.setOnClickListener(view -> loginUser());
    }

    private void loginUser() {
        String email = landlordemail.getText().toString().trim();
        String password = landlordPassword.getText().toString().trim();

        // Validate the email and password fields
        if (TextUtils.isEmpty(email)) {
            landlordemail.setError("Email is required");
            landlordemail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            landlordPassword.setError("Password is required");
            landlordPassword.requestFocus();
            return;
        }

        // Authenticate the user
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Login success, navigate to the LandlordPage
                Toast.makeText(LandlordLogin.this, "Login successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LandlordLogin.this, LandlordPage.class));
                finish(); // Close the login activity
            } else {
                // Login failed, show error message
                Toast.makeText(LandlordLogin.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Optional: If you need this function for future use
    public void landlordpage(View view) {
        startActivity(new Intent(this, LandlordPage.class));
    }
}
