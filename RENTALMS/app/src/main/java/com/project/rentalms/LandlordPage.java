package com.project.rentalms;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class LandlordPage extends AppCompatActivity {

    // Firebase Auth and Firestore instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private PropertyAdapter propertyAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;  // SwipeRefreshLayout

    private ArrayList<Property> propertyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landlord_page);

        // Initialize FirebaseAuth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewProperties);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        propertyList = new ArrayList<>();

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Reference to the TextView where the username will be displayed
        TextView usernameTextView = findViewById(R.id.landlordusername);

        // Get the currently logged-in user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();  // Initialize userId after retrieving current user

            // Initialize PropertyAdapter with userId
            propertyAdapter = new PropertyAdapter(propertyList, userId);
            recyclerView.setAdapter(propertyAdapter);

            DocumentReference userRef = db.collection("Landlords").document(userId);

            // Fetch username from Firestore
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String landlordUsername = document.getString("username");
                        if (landlordUsername != null) {
                            usernameTextView.setText(landlordUsername);
                        } else {
                            usernameTextView.setText("Username not found");
                        }
                    } else {
                        Toast.makeText(LandlordPage.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LandlordPage.this, "Failed to fetch username", Toast.LENGTH_SHORT).show();
                }
            });

            // Fetch properties belonging to the landlord
            fetchProperties(userRef);

            // Set up the SwipeRefreshLayout listener to refresh the properties when the user swipes
            swipeRefreshLayout.setOnRefreshListener(() -> {
                // Refresh the properties when the user pulls down to refresh
                fetchProperties(userRef);
            });
        }

        ImageButton addPropertyButton = findViewById(R.id.add_property);
        addPropertyButton.setOnClickListener(v -> {
            if (currentUser != null) {
                Intent intent = new Intent(LandlordPage.this, LandlordAddProperty.class);
                intent.putExtra("landlordId", currentUser.getUid());
                intent.putExtra("username", usernameTextView.getText().toString());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

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

    // Method to fetch properties from Firestore
    @SuppressLint("NotifyDataSetChanged")
    private void fetchProperties(DocumentReference userRef) {
        userRef.collection("properties").get().addOnCompleteListener(task -> {
            swipeRefreshLayout.setRefreshing(false); // Stop the refresh animation
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    propertyList.clear(); // Clear the list before adding new properties
                    for (DocumentSnapshot document : querySnapshot) {
                        Property property = document.toObject(Property.class);
                        propertyList.add(property);
                    }
                    propertyAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(LandlordPage.this, "No properties found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LandlordPage.this, "Failed to load properties", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
