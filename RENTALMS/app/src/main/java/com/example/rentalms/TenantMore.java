package com.example.rentalms;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class TenantMore extends AppCompatActivity {

    Button logoutButton;
    FirebaseAuth mAuth;
    TextView tenantUsername, tenantEmail;
    FirebaseFirestore firestore;
    LinearLayout myAccount; // Declare LinearLayout for My Account

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_more);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance(); // Initialize Firestore

        // Get the current user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Initialize UI elements for username and email
        tenantUsername = findViewById(R.id.Tenantusername);
        tenantEmail = findViewById(R.id.TenantEmail);

        if (currentUser != null) {
            // Set email
            String email = currentUser.getEmail();
            tenantEmail.setText(email);

            // Fetch username from Firestore
            String userId = currentUser.getUid();
            firestore.collection("Tenants").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Get the username from Firestore
                                String username = document.getString("username");
                                tenantUsername.setText(username);
                            } else {
                                tenantUsername.setText("No Username Found");
                            }
                        } else {
                            Toast.makeText(TenantMore.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // Initialize the LinearLayout for "My Account" and set the click listener
        myAccount = findViewById(R.id.myAccount);
        myAccount.setOnClickListener(view -> {
            // Navigate to TenantProfile activity when "My Account" is clicked
            Intent intent = new Intent(TenantMore.this, TenantProfile.class);
            startActivity(intent);
        });

        // Initialize BottomNavigationView and set the selected item to "More"
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_more);  // Set the selected item to "More"

        // Set up item selected listener for navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Handle the search button click (navigate to TenantPage)
            if (itemId == R.id.bottom_search) {
                startActivity(new Intent(getApplicationContext(), TenantPage.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);  // Animation for navigation
                finish();
                return true;

                // Handle the favorite button click (navigate to TenantFavorite)
            } else if (itemId == R.id.bottom_favorite) {
                startActivity(new Intent(getApplicationContext(), TenantFavorite.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;

                // Handle the chat button click (navigate to TenantChat)
            } else if (itemId == R.id.bottom_chat) {
                startActivity(new Intent(getApplicationContext(), TenantChat.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;

                // Handle the more button click (stay on the same page)
            } else if (itemId == R.id.bottom_more) {
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
            Intent intent = new Intent(TenantMore.this, CreateAccount.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
            startActivity(intent);
            finish();  // Finish current activity to prevent back navigation to this screen
        });
    }
}
