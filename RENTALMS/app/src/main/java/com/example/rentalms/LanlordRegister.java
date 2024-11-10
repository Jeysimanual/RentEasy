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
    EditText landlordFirstName, landlordLastName, landlordusername, landlordMobile, landlordEmail, landlordPassword, landlordRetypePassword;
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
    // Function to create a new landlord account
    private void createAccount() {
        String firstName = landlordFirstName.getText().toString().trim();
        String lastName = landlordLastName.getText().toString().trim();
        String username = landlordusername.getText().toString().trim();
        String mobile = landlordMobile.getText().toString().trim();
        String email = landlordEmail.getText().toString().trim();
        String password = landlordPassword.getText().toString().trim();
        String retypePassword = landlordRetypePassword.getText().toString().trim();

        boolean isValid = true; // Flag to check if all fields are valid

        // Regular expression to allow only one space between words, no leading or trailing spaces
        String nameRegex = "^[A-Za-z]+( [A-Za-z]+)*$"; // Allows letters only, with optional single spaces in between

// Validate first name and last name using the regex
        if (TextUtils.isEmpty(firstName)) {
            landlordFirstName.setError("First name is required");
            isValid = false;
        } else if (!firstName.matches(nameRegex)) {
            landlordFirstName.setError("Invalid format: avoid extra spaces in the firstname");
            isValid = false;
        }

        if (TextUtils.isEmpty(lastName)) {
            landlordLastName.setError("Last name is required");
            isValid = false;
        } else if (!lastName.matches(nameRegex)) {
            landlordLastName.setError("Invalid format: avoid extra spaces in the lastname");
            isValid = false;
        }

        if (TextUtils.isEmpty(username)) {
            landlordusername.setError("Username is required");
            isValid = false;
        } else if (!username.matches(nameRegex)) {
            landlordusername.setError("Invalid format: avoid extra spaces in the username");
            isValid = false;
        }

        if (TextUtils.isEmpty(mobile)) {
            landlordMobile.setError("Mobile number is required");
            isValid = false;
        } else if (!mobile.startsWith("09") || mobile.length() != 11) {  // Check if mobile number starts with 09 and is exactly 11 digits
            landlordMobile.setError("Mobile number must start with '09' and be 11 digits long");
            isValid = false;
        }
        if (TextUtils.isEmpty(email)) {
            landlordEmail.setError("Email is required");
            isValid = false;
        } else if (!email.contains("@")) {  // Check if email contains '@'
            landlordEmail.setError("Please enter a valid email address containing '@'");
            isValid = false;
        }  else if (email.trim().contains(" ")) {  // Ensure no spaces within email
            landlordEmail.setError("Invalid format: avoid spaces in the email");
            isValid = false;
        }

        // Password validation: at least 6 characters, first letter uppercase, and not only digits
        if (TextUtils.isEmpty(password)) {
            landlordPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {  // Check if password is at least 6 characters long
            landlordPassword.setError("Password should be at least 6 characters");
            isValid = false;
        } else if (TextUtils.isDigitsOnly(password)) {  // Check if password is only numbers
            landlordPassword.setError("Password should not be only numbers");
            isValid = false;
        } else if (!password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*")) {  // Check if password has both uppercase and lowercase letters
            landlordPassword.setError("Password must contain both Uppercase and lowercase letters");
            isValid = false;
        }

        if (TextUtils.isEmpty(retypePassword)) {
            landlordRetypePassword.setError("Please retype your password");
            isValid = false;
        }
        if (!password.equals(retypePassword)) {
            landlordRetypePassword.setError("Passwords do not match");
            isValid = false;
        }

        // If any field is invalid, return early
        if (!isValid) {
            Toast.makeText(this, "Please correct the highlighted fields", Toast.LENGTH_LONG).show();
            return; // Stop further execution if validation fails
        }

        // Continue with account creation if validation succeeds
        landlordDatabase.collection("Landlords")
                .whereEqualTo("mobile", mobile)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        landlordMobile.setError("This mobile number is already in use");
                    } else {
                        createFirebaseAccount(email, password, firstName, lastName, username, mobile);
                    }
                });
    }


    // Function to create a Firebase account and save landlord info
    private void createFirebaseAccount(String email, String password, String firstName, String lastName, String username, String mobile) {
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
