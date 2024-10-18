package com.example.rentalms;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LandlordMore extends AppCompatActivity {

    Button logoutButton;
    FirebaseAuth mAuth;
    TextView landlordUsername, landlordEmail;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landlord_more);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance(); // Initialize Firestore

        // Get the current user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Initialize UI elements for username and email
        landlordUsername = findViewById(R.id.Landlordusername);
        landlordEmail = findViewById(R.id.LandlordEmail);

        if (currentUser != null) {
            // Set email
            String email = currentUser.getEmail();
            landlordEmail.setText(email);

            // Fetch username from Firestore
            String userId = currentUser.getUid();
            firestore.collection("Landlords").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Get the username from Firestore
                                String username = document.getString("username");
                                landlordUsername.setText(username);
                            } else {
                                landlordUsername.setText("No Username Found");
                            }
                        } else {
                            Toast.makeText(LandlordMore.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // Initialize BottomNavigationView and set the selected item to "More"
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.btn_more);  // Set the selected item to "More"

        // Set up item selected listener for navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.btn_home) {
                startActivity(new Intent(getApplicationContext(), LandlordPage.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.btn_checklist) {
                startActivity(new Intent(getApplicationContext(), LandlordChecklist.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.btn_chat) {
                startActivity(new Intent(getApplicationContext(), LandlordChat.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.btn_more) {
                return true;  // Stay on the current activity
            }

            return false;
        });

        // Initialize the logout button
        logoutButton = findViewById(R.id.btn_logout);

        // Set click listener for the logout button
        logoutButton.setOnClickListener(view -> {
            // Sign out from Firebase Auth
            mAuth.signOut();

            // Redirect to the CreateAccount activity
            Intent intent = new Intent(LandlordMore.this, CreateAccount.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
            startActivity(intent);
            finish();  // Finish current activity to prevent back navigation to this screen
        });
    }
}
