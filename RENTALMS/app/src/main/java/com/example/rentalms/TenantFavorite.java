package com.example.rentalms;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class TenantFavorite extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView favoritePropertyRecyclerView;
    private TenantFavoriteAdapter favoriteAdapter;
    private ArrayList<Property> favoritePropertyList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String tenantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_favorite);

        tenantId = getIntent().getStringExtra("tenantId");

        db = FirebaseFirestore.getInstance();
        favoritePropertyRecyclerView = findViewById(R.id.tenantFav);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        favoritePropertyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        favoritePropertyList = new ArrayList<>();
        favoriteAdapter = new TenantFavoriteAdapter(this, favoritePropertyList, tenantId);
        favoritePropertyRecyclerView.setAdapter(favoriteAdapter);

        loadFavoriteProperties();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            favoritePropertyList.clear();  // Clear list before reloading
            loadFavoriteProperties();
        });

        // Set up bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_favorite);  // Set the selected item to "Favorite"

        // Set up item selected listener for navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Handle the search button click (navigate to TenantPage)
            if (itemId == R.id.bottom_search) {
                Intent intent = new Intent(getApplicationContext(), TenantPage.class);
                intent.putExtra("tenantId", tenantId);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);  // Animation for navigation
                finish();
                return true;

                // Handle the favorite button click (stay on the same page)
            } else if (itemId == R.id.bottom_favorite) {
                return true;  // Stay on the current activity

                // Handle the chat button click (navigate to TenantChat)
            } else if (itemId == R.id.bottom_chat) {
                Intent intent = new Intent(getApplicationContext(), TenantChat.class);
                intent.putExtra("tenantId", tenantId);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;

                // Handle the more button click (navigate to TenantMore)
            } else if (itemId == R.id.bottom_more) {
                Intent intent = new Intent(getApplicationContext(), TenantMore.class);
                intent.putExtra("tenantId", tenantId);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }

            return false;
        });


    }

    private void loadFavoriteProperties() {
        // Fetch favorite property IDs from Firestore
        db.collection("Tenants")
                .document(tenantId)
                .collection("Favorite")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<String> favoritePropertyIds = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        favoritePropertyIds.add(document.getId());  // Collect favorite property IDs
                    }

                    loadProperties(favoritePropertyIds);
                })
                .addOnFailureListener(e -> Log.e("TenantFavorite", "Error fetching favorite properties.", e));
    }

    private void loadProperties(ArrayList<String> favoritePropertyIds) {
        // Fetch all properties and filter based on favorite IDs
        db.collection("Landlords")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot landlordDoc : task.getResult()) {
                            landlordDoc.getReference().collection("properties").get().addOnCompleteListener(propertiesTask -> {
                                if (propertiesTask.isSuccessful()) {
                                    for (QueryDocumentSnapshot propertyDoc : propertiesTask.getResult()) {
                                        Property property = propertyDoc.toObject(Property.class);
                                        property.setPropertyId(propertyDoc.getId());

                                        // If the property ID is in the list of favorite property IDs, add it to the list
                                        if (favoritePropertyIds.contains(property.getPropertyId())) {
                                            favoritePropertyList.add(property);
                                        }
                                    }

                                    favoriteAdapter.notifyDataSetChanged();
                                    swipeRefreshLayout.setRefreshing(false);
                                } else {
                                    Log.w("TenantFavorite", "Error getting properties.", propertiesTask.getException());
                                }
                            });
                        }
                    } else {
                        Log.w("TenantFavorite", "Error getting landlords.", task.getException());
                    }
                });
    }
}
