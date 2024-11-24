package com.example.rentalms;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class TenantPage extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView tenantPropertyRecyclerView;
    private TenantPropertyAdapter propertyAdapter;
    private ArrayList<Property> propertyList;
    private ArrayList<String> favoritePropertyIds;  // Store favorite property IDs
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchView;
    private String tenantId;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_page);

        // Retrieve tenantId from intent
        tenantId = getIntent().getStringExtra("tenantId");
        Log.d("TenantPage", "Tenant ID: " + tenantId);

        if (tenantId == null) {
            Log.e("TenantPage", "tenantId is null. Check if it's passed correctly.");
        }

        db = FirebaseFirestore.getInstance();
        tenantPropertyRecyclerView = findViewById(R.id.tenantPropertyRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        searchView = findViewById(R.id.searchView);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        tenantPropertyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        propertyList = new ArrayList<>();
        favoritePropertyIds = new ArrayList<>();  // Initialize empty list for favorite property IDs
        propertyAdapter = new TenantPropertyAdapter(this, propertyList, tenantId);
        tenantPropertyRecyclerView.setAdapter(propertyAdapter);

        loadProperties();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            propertyList.clear();  // Clear list before reloading
            loadProperties();
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProperties(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProperties(newText);
                return false;
            }
        });

        // Inside onCreate()
        tenantPropertyRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private boolean isNavigationVisible = true; // Track visibility state

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0 && isNavigationVisible) {
                    // User scrolls down; hide BottomNavigationView
                    bottomNavigationView.animate()
                            .translationY(bottomNavigationView.getHeight() + 20)
                            .setDuration(20)
                            .withEndAction(() -> isNavigationVisible = false);
                } else if (dy < 0 && !isNavigationVisible) {
                    // User scrolls up; show BottomNavigationView
                    bottomNavigationView.animate()
                            .translationY(0)
                            .setDuration(0)
                            .withEndAction(() -> isNavigationVisible = true);
                }
            }
        });


        // Set up bottom navigation
        bottomNavigationView.setSelectedItemId(R.id.bottom_search);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.bottom_search) {
                return true;
            } else if (itemId == R.id.bottom_favorite) {
                Intent intent = new Intent(getApplicationContext(), TenantFavorite.class);
                intent.putExtra("tenantId", tenantId);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.bottom_chat) {
                Intent intent = new Intent(getApplicationContext(), TenantChat.class);
                intent.putExtra("tenantId", tenantId);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
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

    private void loadProperties() {
        CollectionReference landlordsRef = db.collection("Landlords");

        landlordsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot landlordDoc : task.getResult()) {
                    landlordDoc.getReference().collection("properties").get().addOnCompleteListener(propertiesTask -> {
                        if (propertiesTask.isSuccessful()) {
                            for (QueryDocumentSnapshot propertyDoc : propertiesTask.getResult()) {
                                Property property = propertyDoc.toObject(Property.class);
                                property.setPropertyId(propertyDoc.getId());
                                propertyList.add(property);
                            }
                            propertyAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                        } else {
                            Log.w("TenantPage", "Error getting properties.", propertiesTask.getException());
                        }
                    });
                }
            } else {
                Log.w("TenantPage", "Error getting landlords.", task.getException());
            }
        });
    }

    private void filterProperties(String query) {
        ArrayList<Property> filteredList = new ArrayList<>();
        for (Property property : propertyList) {
            if (property.getPropertyName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(property);
            }
        }
        propertyAdapter.updateList(filteredList);
    }
}

