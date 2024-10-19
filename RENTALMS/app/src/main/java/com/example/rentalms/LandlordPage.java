package com.example.rentalms;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LandlordPage extends AppCompatActivity {

    // Firebase Auth and Firestore instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landlord_page);

        // Initialize FirebaseAuth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get the currently logged-in user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Reference to the TextView where the username will be displayed
        TextView usernameTextView = findViewById(R.id.landlordusername);

        if (currentUser != null) {
            // Get the user ID from the currently logged-in user
            String userId = currentUser.getUid();

            // Reference the user document in Firestore
            DocumentReference userRef = db.collection("Landlords").document(userId);

            // Fetch the username from Firestore
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Get the username from Firestore
                        String landlordUsername = document.getString("username");
                        if (landlordUsername != null) {
                            // Set the username in the TextView directly
                            usernameTextView.setText(landlordUsername);
                        } else {
                            // Default text if the username is missing
                            usernameTextView.setText("Username not found");
                        }
                    } else {
                        Toast.makeText(LandlordPage.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LandlordPage.this, "Failed to fetch username", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Set up the ImageButton click listener to navigate to the LandlordAddProperty page
        ImageButton addPropertyButton = findViewById(R.id.add_property);
        addPropertyButton.setOnClickListener(v -> {
            // Create an Intent to navigate to LandlordAddProperty
            Intent intent = new Intent(LandlordPage.this, LandlordAddProperty.class);
            // Pass the landlord's user ID to the next activity if needed
            intent.putExtra("userId", currentUser.getUid());
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // Set up BottomNavigationView navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.btn_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.btn_home) {
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
                startActivity(new Intent(getApplicationContext(), LandlordMore.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }

            return false;
        });
    }
}
