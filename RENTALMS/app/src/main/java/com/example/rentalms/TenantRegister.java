package com.example.rentalms;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class TenantRegister extends AppCompatActivity {

    // UI components
    EditText tenantFirstName, tenantLastName, tenantMobile, tenantEmail, tenantPassword, tenantRetypePassword;
    TextView tenantLogin;
    Button createAccountButton;

    // Firebase components
    FirebaseAuth mAuth;
    FirebaseFirestore tenantDatabase;  // Firestore reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_register);

        // Initialize Firebase Authentication and Firestore Database
        mAuth = FirebaseAuth.getInstance();
        tenantDatabase = FirebaseFirestore.getInstance();  // Firestore initialization

        // Initialize UI components
        tenantFirstName = findViewById(R.id.Tenantfirstname);
        tenantLastName = findViewById(R.id.Tenantlastname);
        tenantMobile = findViewById(R.id.TenantMobile);
        tenantEmail = findViewById(R.id.TenantEmail);
        tenantPassword = findViewById(R.id.TenantPassword);
        tenantRetypePassword = findViewById(R.id.TenantRetype);

        tenantLogin = findViewById(R.id.tenantlogin);
        createAccountButton = findViewById(R.id.btnCreateAccount);

        // Create account button click listener
        createAccountButton.setOnClickListener(view -> createAccount());

        // Login link click listener
        tenantLogin.setOnClickListener(v -> {
            startActivity(new Intent(TenantRegister.this, TenantLogin.class));
        });
    }

    // Function to create a new tenant account
    private void createAccount() {
        String firstName = tenantFirstName.getText().toString().trim();
        String lastName = tenantLastName.getText().toString().trim();
        String mobile = tenantMobile.getText().toString().trim();
        String email = tenantEmail.getText().toString().trim();
        String password = tenantPassword.getText().toString().trim();
        String retypePassword = tenantRetypePassword.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(mobile)
                || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(TenantRegister.this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(retypePassword)) {
            Toast.makeText(TenantRegister.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create account in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Account created successfully, now save the user information in Firestore
                String userId = mAuth.getCurrentUser().getUid();
                saveTenantInfo(userId, firstName, lastName, mobile, email);

                // Show success message and navigate to login
                Toast.makeText(TenantRegister.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(TenantRegister.this, TenantLogin.class));
                finish();
            } else {
                // If account creation fails, display an error message
                Toast.makeText(TenantRegister.this, "Account creation failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Function to save tenant information in Firestore Database
    private void saveTenantInfo(String userId, String firstName, String lastName, String mobile, String email) {
        // Create a HashMap to store the user data
        HashMap<String, Object> tenantInfo = new HashMap<>();
        tenantInfo.put("firstName", firstName);
        tenantInfo.put("lastName", lastName);
        tenantInfo.put("mobile", mobile);
        tenantInfo.put("email", email);

        // Add a new document to the "Tenants" collection with the user's ID as the document ID
        DocumentReference tenantRef = tenantDatabase.collection("Tenants").document(userId);

        tenantRef.set(tenantInfo).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Data saved successfully
                Toast.makeText(TenantRegister.this, "Tenant data saved", Toast.LENGTH_SHORT).show();
            } else {
                // Error in saving data
                Toast.makeText(TenantRegister.this, "Failed to save tenant data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}