package com.example.rentalms;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LandlordLogin extends AppCompatActivity {

    // UI Components
    EditText landlordemail, landlordPassword;
    Button landlordloginbtn;
    TextView Landlordcreate, landlordForgotPassword;
    ImageView lockIcon, unlockIcon;

    // Firebase Authentication and Firestore
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landlord_login);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        landlordemail = findViewById(R.id.landlordemail);
        landlordPassword = findViewById(R.id.landlordPassword);
        landlordloginbtn = findViewById(R.id.landlordloginbtn);
        Landlordcreate = findViewById(R.id.Landlordcreate);
        landlordForgotPassword = findViewById(R.id.landlordFP);
        lockIcon = findViewById(R.id.lock);
        unlockIcon = findViewById(R.id.unlock);

        // Set up the listener for the create account link
        Landlordcreate.setOnClickListener(view -> {
            startActivity(new Intent(LandlordLogin.this, CreateAccount.class));
        });

        // Set up the listener for the Forgot Password link
        landlordForgotPassword.setOnClickListener(view -> {
            startActivity(new Intent(LandlordLogin.this, ForgetPassword.class));
        });

        // Set up login button click listener
        landlordloginbtn.setOnClickListener(view -> loginUser());

        // Set up the password visibility toggle
        lockIcon.setOnClickListener(view -> {
            landlordPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            lockIcon.setVisibility(View.GONE);
            unlockIcon.setVisibility(View.VISIBLE);
        });

        unlockIcon.setOnClickListener(view -> {
            landlordPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            unlockIcon.setVisibility(View.GONE);
            lockIcon.setVisibility(View.VISIBLE);
        });
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
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    // Reload user to ensure email verification status is up-to-date
                    user.reload().addOnCompleteListener(reloadTask -> {
                        if (reloadTask.isSuccessful()) {
                            if (user.isEmailVerified()) {
                                // Check the user's account type in Firestore
                                String userId = user.getUid();
                                db.collection("Landlords").document(userId).get().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        DocumentSnapshot document = task1.getResult();
                                        if (document != null && document.exists()) {
                                            String accountType = document.getString("accountType");
                                            if ("Landlord".equals(accountType)) {
                                                // User is a landlord, allow access
                                                Toast.makeText(LandlordLogin.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(LandlordLogin.this, LandlordPage.class));
                                                finish(); // Close the login activity
                                            } else {
                                                // User is not a landlord, deny access
                                                Toast.makeText(LandlordLogin.this, "Access denied: You are not a landlord", Toast.LENGTH_LONG).show();
                                                mAuth.signOut(); // Sign the user out
                                            }
                                        } else {
                                            Toast.makeText(LandlordLogin.this, "Error: Account type not found!", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(LandlordLogin.this, "Error: " + task1.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                // Email is not verified
                                user.sendEmailVerification().addOnCompleteListener(emailTask -> {
                                    if (emailTask.isSuccessful()) {
                                        Toast.makeText(this, "Verification email sent. Please verify to log in.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this, "Failed to send verification email: " + emailTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                                mAuth.signOut();
                            }
                        } else {
                            Toast.makeText(LandlordLogin.this, "Failed to reload user data: " + reloadTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                // Login failed, show error message
                Toast.makeText(LandlordLogin.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
