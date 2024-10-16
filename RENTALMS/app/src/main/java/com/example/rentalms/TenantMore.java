package com.example.rentalms;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TenantMore extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_more);  // Assuming this is your layout for the "More" page

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
    }
}
