package com.example.rentalms;

import android.annotation.SuppressLint;
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
    EditText tenantFirstName, tenantLastName, tenantMobile, tenantusername, tenantEmail, tenantPassword, tenantRetypePassword;
    TextView tenantLogin;
    Button createAccountButton;

    // Firebase components
    FirebaseAuth mAuth;
    FirebaseFirestore tenantDatabase;  // Firestore reference

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
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
        tenantusername = findViewById(R.id.Tenantusername);
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
        String username = tenantusername.getText().toString().trim();
        String mobile = tenantMobile.getText().toString().trim();
        String email = tenantEmail.getText().toString().trim();
        String password = tenantPassword.getText().toString().trim();
        String retypePassword = tenantRetypePassword.getText().toString().trim();

        boolean isValid = true; // Flag to check if all fields are valid

        // Validate input fields and show error messages if necessary
        if (TextUtils.isEmpty(firstName)) {
            tenantFirstName.setError("First name is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(lastName)) {
            tenantLastName.setError("Last name is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(username)) {
            tenantusername.setError("Username is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(mobile)) {
            tenantMobile.setError("Mobile number is required");
            isValid = false;
        } else if (mobile.length() != 11) {  // Check if mobile number is exactly 11 digits
            tenantMobile.setError("Mobile number must be 11 digits");
            isValid = false;
        }
        if (TextUtils.isEmpty(email)) {
            tenantEmail.setError("Email is required");
            isValid = false;
        }
        else if (!email.contains("@")) {  // Check if email contains '@'
            tenantEmail.setError("Please enter a valid email address containing '@'");
            isValid = false;
        }
        if (TextUtils.isEmpty(password)) {
            tenantPassword.setError("Password is required");
            isValid = false;
        }
        else if (password.length() < 6) {  // Check if password is at least 6 characters long
            tenantPassword.setError("Password should be at least 6 characters");
            isValid = false;
        } else if (TextUtils.isDigitsOnly(password)) {  // Check if password is only numbers
            tenantPassword.setError("Password should not be only numbers");
            isValid = false;
        }
        if (TextUtils.isEmpty(retypePassword)) {
            tenantRetypePassword.setError("Please retype your password");
            isValid = false;
        }
        if (!password.equals(retypePassword)) {
            tenantRetypePassword.setError("Passwords do not match");
            isValid = false;
        }

        // If any field is invalid, return early
        if (!isValid) {
            return; // Stop further execution if validation fails
        }

        // Check if the mobile number is already registered
        tenantDatabase.collection("Tenants")
                .whereEqualTo("mobile", mobile)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Mobile number already exists
                        tenantMobile.setError("This mobile number is already in use");
                    } else {
                        // Mobile number does not exist, proceed with account creation
                        createFirebaseAccount(email, password, firstName, lastName, username, mobile);
                    }
                });
    }

    // Function to create a Firebase account and save tenant info
    private void createFirebaseAccount(String email, String password, String firstName, String lastName, String username, String mobile) {
        // Create account in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Account created successfully, now save the user information in Firestore
                String userId = mAuth.getCurrentUser().getUid();
                saveTenantInfo(userId, firstName, lastName, username, mobile, email);

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
    private void saveTenantInfo(String userId, String firstName, String lastName, String username, String mobile, String email) {
        // Create a HashMap to store the user data
        HashMap<String, Object> tenantInfo = new HashMap<>();
        tenantInfo.put("firstName", firstName);
        tenantInfo.put("lastName", lastName);
        tenantInfo.put("username", username);
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