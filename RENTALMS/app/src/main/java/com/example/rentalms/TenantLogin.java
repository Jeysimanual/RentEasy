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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class TenantLogin extends AppCompatActivity {

    // UI Components
    EditText tenantemail, tenantPassword;
    Button tenantloginbtn;
    TextView Tenantcreate, tenantForgotPassword;

    // Firebase Authentication and Firestore
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_login);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();  // Initialize Firestore

        // Initialize UI elements
        tenantemail = findViewById(R.id.tenantemail);
        tenantPassword = findViewById(R.id.tenantPassword);
        tenantloginbtn = findViewById(R.id.tenantloginbtn);
        Tenantcreate = findViewById(R.id.Tenantcreate);
        tenantForgotPassword = findViewById(R.id.tenantFP); // Forgot Password TextView

        // Set up the listener for the create account link
        Tenantcreate.setOnClickListener(view -> {
            startActivity(new Intent(TenantLogin.this, TenantRegister.class));
        });

        // Set up the listener for the Forgot Password link
        tenantForgotPassword.setOnClickListener(view -> {
            startActivity(new Intent(TenantLogin.this, ForgetPassword.class));
        });

        // Set up login button click listener
        tenantloginbtn.setOnClickListener(view -> loginUser());
    }

    private void loginUser() {
        String email = tenantemail.getText().toString().trim();
        String password = tenantPassword.getText().toString().trim();

        // Validate the email and password fields
        if (TextUtils.isEmpty(email)) {
            tenantemail.setError("Email is required");
            tenantemail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            tenantPassword.setError("Password is required");
            tenantPassword.requestFocus();
            return;
        }

        // Authenticate the user
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Authentication succeeded, check the user's account type in Firestore
                String userId = mAuth.getCurrentUser().getUid();
                db.collection("Tenants").document(userId).get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        DocumentSnapshot document = task1.getResult();
                        if (document.exists()) {
                            String accounttype = document.getString("accountType");
                            if ("Tenant".equals(accounttype)) {
                                // User is a tenant, allow access
                                Toast.makeText(TenantLogin.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(TenantLogin.this, TenantPage.class));
                                finish(); // Close the login activity
                            } else {
                                // User is not a tenant, deny access
                                Toast.makeText(TenantLogin.this, "Access denied: You are not a tenant", Toast.LENGTH_LONG).show();
                                mAuth.signOut(); // Sign the user out
                            }
                        } else {
                            Toast.makeText(TenantLogin.this, "Error: Account type not found!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(TenantLogin.this, "Error: " + task1.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                // Login failed, show error message
                Toast.makeText(TenantLogin.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Optional: If you need this function for future use
    public void tenantpage(View view) {
        startActivity(new Intent(this, TenantPage.class));
    }
}
