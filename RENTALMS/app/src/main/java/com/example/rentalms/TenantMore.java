package com.example.rentalms;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class TenantMore extends AppCompatActivity {

    Button logoutButton;
    FirebaseAuth mAuth;
    TextView tenantUsername, tenantEmail;
    FirebaseFirestore firestore;
    LinearLayout myAccount;
    LinearLayout changepass;

    LinearLayout schedulebtn;
    ImageView profileImageView;  // Update to use profile_picture ID

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_more);

        // Initialize FirebaseAuth and Firestore instances
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        String tenantId = getIntent().getStringExtra("tenantId");
        // Get the current user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Initialize UI elements for username, email, and profile image
        tenantUsername = findViewById(R.id.Tenantusername);
        tenantEmail = findViewById(R.id.TenantEmail);
        profileImageView = findViewById(R.id.profile_picture);  // Correct ID reference

        if (currentUser != null) {
            // Set email
            String email = currentUser.getEmail();
            tenantEmail.setText(email);

            // Fetch tenant details including profile picture
            String userId = currentUser.getUid();
            firestore.collection("Tenants").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String username = document.getString("username");
                                tenantUsername.setText(username != null ? username : "No Username Found");

                                String imageUrl = document.getString("profileImageUrl");
                                if (imageUrl != null && !imageUrl.isEmpty()) {
                                    loadProfileImage(imageUrl);
                                } else {
                                    profileImageView.setImageResource(R.drawable.baseline_account_circle);
                                }
                            } else {
                                tenantUsername.setText("No Username Found");
                            }
                        } else {
                            Toast.makeText(TenantMore.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // Set up "My Account" click listener
        myAccount = findViewById(R.id.myAccount);
        myAccount.setOnClickListener(view -> {
            Intent intent = new Intent(TenantMore.this, TenantProfile.class);
            startActivity(intent);
        });

        // Set up "Change Password" click listener
        changepass = findViewById(R.id.changepass);
        changepass.setOnClickListener(view -> {
            Intent intent = new Intent(TenantMore.this, ChangePassword.class);
            startActivity(intent);
        });
        schedulebtn = findViewById(R.id.schedulebtn);
        schedulebtn.setOnClickListener(view -> {
            Intent intent = new Intent(TenantMore.this, ScheduleVisit.class);
            startActivity(intent);
        });

        // Set up BottomNavigationView and default selection
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_more);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Navigation handling
            if (itemId == R.id.bottom_search) {
                Intent intent = new Intent(TenantMore.this, TenantPage.class);
                intent.putExtra("tenantId", tenantId);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.bottom_favorite) {
                Intent intent = new Intent(TenantMore.this, TenantFavorite.class);
                intent.putExtra("tenantId", tenantId);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.bottom_chat) {
                Intent intent = new Intent(TenantMore.this, TenantChat.class);
                intent.putExtra("tenantId", tenantId);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.bottom_more) {
                return true;  // Stay on current activity
            }

            return false;
        });

        // Set up logout button
        logoutButton = findViewById(R.id.btn_logout);
        logoutButton.setOnClickListener(view -> {
            mAuth.signOut();
            Intent intent = new Intent(TenantMore.this, Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // Load profile image from URL using Picasso
    private void loadProfileImage(String imageUrl) {
        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.baseline_account_circle) // Placeholder image
                .error(R.drawable.baseline_account_circle)       // Error image
                .into(profileImageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d("TenantMore", "Image loaded successfully.");
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("TenantMore", "Error loading image", e);
                    }
                });
    }
}
