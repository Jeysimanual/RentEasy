package com.example.rentalms;

import android.annotation.SuppressLint;
import android.content.Intent;  // Import the Intent class
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LandlordProfile extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView firstNameTextView, lastNameTextView, emailTextView, mobileTextView, accountTypeTextView;
    private TextView welcomeTextView; // Declare a TextView for the welcome message
    private TextView backButton; // Declare a TextView for the back button

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landlord_profile);

        // Initialize Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize TextViews from the layout
        firstNameTextView = findViewById(R.id.Lfirstname);
        lastNameTextView = findViewById(R.id.Llastname);
        emailTextView = findViewById(R.id.LEmail);
        mobileTextView = findViewById(R.id.LMobile);
        accountTypeTextView = findViewById(R.id.Laccounttype);
        welcomeTextView = findViewById(R.id.welcome_text); // Initialize the welcome TextView
        backButton = findViewById(R.id.backbtn); // Initialize the back button TextView

        // Set click listener for the back button
        backButton.setOnClickListener(v -> {
            // Start TenantMore activity
            Intent intent = new Intent(LandlordProfile.this, LandlordMore.class);
            startActivity(intent);
            finish(); // Optional: Finish current activity if you don't want to return to it
        });

        // Fetch tenant data from Firestore and display it
        fetchTenantData();
    }

    private void fetchTenantData() {
        // Get the current user's ID
        String userId = auth.getCurrentUser().getUid();  // Gets the UID of the logged-in user

        if (userId != null) {
            // Fetch the tenant's profile from Firestore using their UID
            db.collection("Landlords").document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Get data from the Firestore document and display it
                                String firstName = document.getString("firstName");
                                String lastName = document.getString("lastName");
                                String email = document.getString("email");
                                String mobile = document.getString("mobile");
                                String accountType = document.getString("accountType");

                                // Create full name with proper spacing
                                StringBuilder fullName = new StringBuilder();
                                if (firstName != null && !firstName.trim().isEmpty()) {
                                    fullName.append(firstName.trim()); // Add firstName if it's not empty
                                }
                                if (lastName != null && !lastName.trim().isEmpty()) {
                                    if (fullName.length() > 0) {
                                        fullName.append(" "); // Add a space if firstName has been added
                                    }
                                    fullName.append(lastName.trim()); // Add lastName if it's not empty
                                }

                                // Set the data to the TextViews
                                firstNameTextView.setText(firstName != null ? firstName : "N/A");
                                lastNameTextView.setText(lastName != null ? lastName : "N/A");
                                welcomeTextView.setText("Welcome, " + fullName.toString()); // Set the welcome message
                                emailTextView.setText(email != null ? email : "N/A");
                                mobileTextView.setText(mobile != null ? mobile : "N/A");
                                accountTypeTextView.setText(accountType != null ? accountType : "N/A");
                            } else {
                                Toast.makeText(LandlordProfile.this, "No such tenant profile found!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LandlordProfile.this, "Failed to fetch profile data", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Handle the case when the user is not logged in
            Toast.makeText(LandlordProfile.this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }


}
