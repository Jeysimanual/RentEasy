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

public class LanlordRegister extends AppCompatActivity {

    // UI components
    EditText landlordFirstName, landlordLastName,landlordusername, landlordMobile, landlordEmail, landlordPassword, landlordRetypePassword;
    TextView landlordlogin;
    Button createAccountButton;

    // Firebase components
    FirebaseAuth mAuth;
    FirebaseFirestore landlordDatabase;  // Firestore reference

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lanlord_register);

        // Initialize Firebase Authentication and Firestore Database
        mAuth = FirebaseAuth.getInstance();
        landlordDatabase = FirebaseFirestore.getInstance();  // Firestore initialization

        // Initialize UI components
        landlordFirstName = findViewById(R.id.Landlordfirstname);
        landlordLastName = findViewById(R.id.Landlordlastname);
        landlordusername = findViewById(R.id.Landlordusername);
        landlordMobile = findViewById(R.id.LandlordMobile);
        landlordEmail = findViewById(R.id.LandlordEmail);
        landlordPassword = findViewById(R.id.LandlordPassword);
        landlordRetypePassword = findViewById(R.id.LandlordRetype);

        landlordlogin = findViewById(R.id.landlordlogin);
        createAccountButton = findViewById(R.id.btnCreateAccount);

        // Create account button click listener
        createAccountButton.setOnClickListener(view -> createAccount());

        // Login link click listener
        landlordlogin.setOnClickListener(v -> {
            startActivity(new Intent(LanlordRegister.this, LandlordLogin.class));
        });
    }

    // Function to create a new landlord account
    private void createAccount() {
        String firstName = landlordFirstName.getText().toString().trim();
        String lastName = landlordLastName.getText().toString().trim();
        String username = landlordusername.getText().toString().trim();
        String mobile = landlordMobile.getText().toString().trim();
        String email = landlordEmail.getText().toString().trim();
        String password = landlordPassword.getText().toString().trim();
        String retypePassword = landlordRetypePassword.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(mobile)
                || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(LanlordRegister.this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(retypePassword)) {
            Toast.makeText(LanlordRegister.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create account in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Account created successfully, now save the user information in Firestore
                String userId = mAuth.getCurrentUser().getUid();
                saveLandlordInfo(userId, firstName, lastName, username, mobile, email);

                // Show success message and navigate to login
                Toast.makeText(LanlordRegister.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LanlordRegister.this, LandlordLogin.class));
                finish();
            } else {
                // If account creation fails, display an error message
                Toast.makeText(LanlordRegister.this, "Account creation failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Function to save landlord information in Firestore Database
    // Function to save landlord information in Firestore Database
    private void saveLandlordInfo(String userId, String firstName, String lastName, String username, String mobile, String email) {
        // Create a HashMap to store the user data
        HashMap<String, Object> landlordInfo = new HashMap<>();
        landlordInfo.put("firstName", firstName);
        landlordInfo.put("lastName", lastName);
        landlordInfo.put("username", username);
        landlordInfo.put("mobile", mobile);
        landlordInfo.put("email", email);

        // Add the account type to the HashMap
        landlordInfo.put("accountType", "Landlord"); // This line adds the account type

        // Add a new document to the "Landlords" collection with the user's ID as the document ID
        DocumentReference landlordRef = landlordDatabase.collection("Landlords").document(userId);

        landlordRef.set(landlordInfo).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Data saved successfully
                Toast.makeText(LanlordRegister.this, "Landlord data saved", Toast.LENGTH_SHORT).show();
            } else {
                // Error in saving data
                Toast.makeText(LanlordRegister.this, "Failed to save landlord data", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
