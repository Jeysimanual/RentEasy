package com.example.rentalms;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class TenantFavorite extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView favoriteRecyclerView;
    private TenantPropertyAdapter favoriteAdapter;
    private ArrayList<Property> favoriteList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_favorite);

        // Initialize Firestore instance and RecyclerView
        db = FirebaseFirestore.getInstance();
        favoriteRecyclerView = findViewById(R.id.favoriteRecyclerView);
        favoriteRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the favorite list and adapter
        favoriteList = new ArrayList<>();
        favoriteAdapter = new TenantPropertyAdapter(this, favoriteList);
        favoriteRecyclerView.setAdapter(favoriteAdapter);

        // Retrieve the favorites list from the intent, if available
        ArrayList<Property> intentFavoritesList = getIntent().getParcelableArrayListExtra("favoritesList");
        if (intentFavoritesList != null) {
            favoriteList.clear();
            favoriteList.addAll(intentFavoritesList);
            favoriteAdapter.notifyDataSetChanged();
        } else {
            loadFavorites(); // Load from Firestore if intent data is null
        }

        // Bottom navigation setup
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_favorite);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.bottom_search) {
                startActivity(new Intent(getApplicationContext(), TenantPage.class));
                overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);
                finish();
                return true;
            } else if (itemId == R.id.bottom_favorite) {
                return true; // No action needed if already on this page
            } else if (itemId == R.id.bottom_chat) {
                startActivity(new Intent(getApplicationContext(), TenantChat.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.bottom_more) {
                startActivity(new Intent(getApplicationContext(), TenantMore.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });
    }

    // Load favorite properties from Firestore if not provided in the intent
    private void loadFavorites() {
        String tenantId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (tenantId == null) {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Tenants")
                .document(tenantId)
                .collection("Favorite")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        favoriteList.clear(); // Clear old favorites
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Property property = document.toObject(Property.class);
                            favoriteList.add(property);
                        }
                        favoriteAdapter.notifyDataSetChanged(); // Refresh the adapter
                    } else {
                        Toast.makeText(TenantFavorite.this, "Failed to load favorites", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}