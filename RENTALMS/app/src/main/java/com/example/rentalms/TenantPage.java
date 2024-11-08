package com.example.rentalms;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class TenantPage extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView tenantPropertyRecyclerView;
    private TenantPropertyAdapter propertyAdapter;
    private ArrayList<Property> propertyList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchView;

    private String userID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_page);
        Intent intent;
        intent = getIntent();
        userID = intent.getStringExtra("userId");
        Log.d("TenantPage", "User ID received: " + userID);

        db = FirebaseFirestore.getInstance();
        tenantPropertyRecyclerView = findViewById(R.id.tenantPropertyRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        searchView = findViewById(R.id.searchView);

        tenantPropertyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        propertyList = new ArrayList<>();
        propertyAdapter = new TenantPropertyAdapter(this, propertyList);
        tenantPropertyRecyclerView.setAdapter(propertyAdapter);






        loadProperties();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            propertyList.clear();
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






        // Correct the IDs to match your XML resource
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_search);

        // Set up item selected listener for navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Handle the search button click (remains on the same page)
            if (itemId == R.id.bottom_search) {
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



    private void loadProperties() {
        CollectionReference landlordsRef = db.collection("Landlords");

        landlordsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot landlordDoc : task.getResult()) {
                    CollectionReference propertiesRef = landlordDoc.getReference().collection("properties");

                    propertiesRef.get().addOnCompleteListener(propertyTask -> {
                        if (propertyTask.isSuccessful()) {
                            for (QueryDocumentSnapshot propertyDoc : propertyTask.getResult()) {
                                Property property = propertyDoc.toObject(Property.class);
                                property.setUserId(userID);
                                propertyList.add(property);
                            }
                            propertyAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(TenantPage.this, "Error loading properties", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    });
                }
            } else {
                Toast.makeText(TenantPage.this, "Error loading landlords", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void filterProperties(String query) {
        ArrayList<Property> filteredList = new ArrayList<>();
        if (!TextUtils.isEmpty(query)) {
            for (Property property : propertyList) {
                if (property.getPropertyName().toLowerCase().contains(query.toLowerCase())
                        || property.getCity().toLowerCase().contains(query.toLowerCase())
                        || property.getProvince().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(property);
                }
            }
        } else {
            filteredList.addAll(propertyList);
        }
        propertyAdapter.updateList(filteredList);
    }



}
