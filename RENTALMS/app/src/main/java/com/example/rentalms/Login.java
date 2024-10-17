package com.example.rentalms;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    TextView createacc;
    EditText emailInput, passwordInput;
    Button loginButton;

    // Firebase components
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        createacc = findViewById(R.id.createacc);
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.Password);
        loginButton = findViewById(R.id.loginbtn);

        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already logged in, check account type and redirect
            checkUserType(currentUser.getUid());
        }

        // Handle create account redirection
        createacc.setOnClickListener(view -> {
            startActivity(new Intent(Login.this, CreateAccount.class));
        });

        // Handle login button click
        loginButton.setOnClickListener(view -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                loginUser(email, password);
            } else {
                Toast.makeText(Login.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser(String email, String password) {
        // Firebase Authentication: Log in the user
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // User logged in successfully, now get user account type from Firestore
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    checkUserType(currentUser.getUid());
                }
            } else {
                // Login failed
                Toast.makeText(Login.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkUserType(String userId) {
        // Retrieve the user's document from Firestore
        db.collection("Users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Assuming there is a field called "accountType" in the user document
                    String accountType = document.getString("accountType");
                    if (accountType != null) {
                        // Redirect to the appropriate page based on account type
                        if (accountType.equals("Landlords")) {
                            startActivity(new Intent(Login.this, LandlordPage.class));
                        } else if (accountType.equals("Tenants")) {
                            startActivity(new Intent(Login.this, TenantPage.class));
                        }
                        finish(); // Close login activity
                    } else {
                        Toast.makeText(Login.this, "Account type not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Login.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(Login.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
