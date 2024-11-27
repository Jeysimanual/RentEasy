package com.project.rentalms;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AlertDialog;
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

    // Additional variable for filtering
    private String selectedPriceRange = "All";  // Default to showing all properties

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

        // ImageView filter for price range
        ImageView filterImageView = findViewById(R.id.filter); // Make sure you have the correct ID for your filter icon
        filterImageView.setOnClickListener(v -> showFilterDialog());

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
        String lowerCaseQuery = query.toLowerCase(); // Case-insensitive search

        for (Property property : propertyList) {
            // Check if query matches any relevant property field
            if (property.getPropertyName().toLowerCase().contains(lowerCaseQuery) ||
                    property.getType().toLowerCase().contains(lowerCaseQuery) ||           // Match property type
                    property.getBarangay().toLowerCase().contains(lowerCaseQuery) ||       // Match barangay
                    property.getCity().toLowerCase().contains(lowerCaseQuery) ||           // Match city
                    property.getProvince().toLowerCase().contains(lowerCaseQuery) ||       // Match province
                    property.getAddress().toLowerCase().contains(lowerCaseQuery)) {        // Match address
                filteredList.add(property); // Add matching property to filtered list
            }
        }

        // Update the RecyclerView with the filtered list
        propertyAdapter.updateList(filteredList);
    }

    private void showFilterDialog() {
        // Inflate the dialog layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_filter, null);

        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroupPriceRange);
        RadioButton radioAll = dialogView.findViewById(R.id.radioAll);
        RadioButton radio1000to5000 = dialogView.findViewById(R.id.radio1000to5000);
        RadioButton radio5000to10000 = dialogView.findViewById(R.id.radio5000to10000);
        RadioButton radio10000to20000 = dialogView.findViewById(R.id.radio10000to20000);
        RadioButton radioAbove20000 = dialogView.findViewById(R.id.radioAbove20000);

        // Set checked based on the current selected price range
        switch (selectedPriceRange) {
            case "1000-5000":
                radio1000to5000.setChecked(true);
                break;
            case "5000-10000":
                radio5000to10000.setChecked(true);
                break;
            case "10000-20000":
                radio10000to20000.setChecked(true);
                break;
            case "Above 20000":
                radioAbove20000.setChecked(true);
                break;
            default:
                radioAll.setChecked(true); // All
        }

        // Build the dialog
        new AlertDialog.Builder(this)
                .setTitle("Price Range")
                .setView(dialogView)
                .setPositiveButton("Apply", (dialog, which) -> {
                    int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();
                    if (selectedRadioButtonId == R.id.radio1000to5000) {
                        selectedPriceRange = "1000-5000";
                    } else if (selectedRadioButtonId == R.id.radio5000to10000) {
                        selectedPriceRange = "5000-10000";
                    } else if (selectedRadioButtonId == R.id.radio10000to20000) {
                        selectedPriceRange = "10000-20000";
                    } else if (selectedRadioButtonId == R.id.radioAbove20000) {
                        selectedPriceRange = "Above 20000";
                    } else {
                        selectedPriceRange = "All";  // Reset to 'All' if 'All' is selected
                    }

                    // Filter the properties based on the selected range
                    filterPropertiesByPrice(propertyList); // Pass the full list of properties to filter

                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void filterPropertiesByPrice(ArrayList<Property> filteredList) {
        ArrayList<Property> finalFilteredList = new ArrayList<>();

        for (Property property : filteredList) {
            String priceString = property.getPrice();  // Get the price string (e.g., "₱1500")

            // Remove the '₱' symbol and any other non-numeric characters
            priceString = priceString.replaceAll("[^\\d]", "");

            // Convert the cleaned-up price string to an integer
            int price = 0;
            try {
                price = Integer.parseInt(priceString);  // Convert to integer
            } catch (NumberFormatException e) {
                Log.e("TenantPage", "Error parsing price: " + priceString, e);
                continue;  // Skip this property if the price is invalid
            }

            // Apply the price range filter based on selectedPriceRange
            switch (selectedPriceRange) {
                case "1000-5000":
                    if (price >= 1000 && price <= 5000) {
                        finalFilteredList.add(property);
                    }
                    break;
                case "5000-10000":
                    if (price > 5000 && price <= 10000) {
                        finalFilteredList.add(property);
                    }
                    break;
                case "10000-20000":
                    if (price > 10000 && price <= 20000) {
                        finalFilteredList.add(property);
                    }
                    break;
                case "Above 20000":
                    if (price > 20000) {
                        finalFilteredList.add(property);
                    }
                    break;
                default:
                    finalFilteredList.add(property);  // Show all properties if no price range is selected
                    break;
            }
        }

        // Update the adapter with the filtered list of properties
        propertyAdapter.updateList(finalFilteredList);
    }

}
