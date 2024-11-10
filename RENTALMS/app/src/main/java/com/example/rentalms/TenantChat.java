package com.example.rentalms;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rentalms.Property;
import com.example.rentalms.R;
import com.example.rentalms.TenantMore;
import com.example.rentalms.TenantPage;
import com.example.rentalms.TenantPropertyAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class TenantChat extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView favoriteRecyclerView; // Assuming you may want to show favorites in this chat
    private TenantPropertyAdapter favoriteAdapter;
    private ArrayList<Property> favoriteList;
    private String userId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_chat); // Assuming this is your layout for the chat page

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null; // Get user ID safely

        // Check if userId is null
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish(); // Exit the activity if the user is not logged in
            return; // Prevent further execution
        }

        // Initialize RecyclerView for favorites
        favoriteRecyclerView = findViewById(R.id.favoriteRecyclerView);
        favoriteRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoriteList = new ArrayList<>();
        favoriteAdapter = new TenantPropertyAdapter(this, favoriteList);
        favoriteRecyclerView.setAdapter(favoriteAdapter);

        loadFavorites(); // Load favorites on activity start

        // Initialize BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_chat); // Set the selected item to "Chat"

        // Set up item selected listener for navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Handle the search button click (navigate to TenantPage)
            if (itemId == R.id.bottom_search) {
                startActivity(new Intent(getApplicationContext(), TenantPage.class));
                overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right); // Animation for navigation
                finish();
                return true;

                // Handle the favorite button click (navigate to TenantFavorite)
            } else if (itemId == R.id.bottom_favorite) {
                startActivity(new Intent(getApplicationContext(), TenantFavorite.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;

                // Handle the chat button click (stay on the same page)
            } else if (itemId == R.id.bottom_chat) {
                return true; // Stay on the current activity

                // Handle the more button click (navigate to TenantMore)
            } else if (itemId == R.id.bottom_more) {
                startActivity(new Intent(getApplicationContext(), TenantMore.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }

            return false;
        });
    }

    private void loadFavorites() {
        // Make sure the path to the Favorites collection is correct
        CollectionReference favoritesRef = db.collection("Tenants").document(userId).collection("Favorite");

        favoritesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                favoriteList.clear(); // Clear previous data
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Property property = document.toObject(Property.class);
                    favoriteList.add(property); // Add each property to the favorite list
                }
                favoriteAdapter.notifyDataSetChanged(); // Notify adapter of data change
            } else {
                Toast.makeText(TenantChat.this, "Error loading favorites", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
