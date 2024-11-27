package com.project.rentalms;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TenantChat extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_chat);  // Assuming this is your layout for the chat page
        String tenantId = getIntent().getStringExtra("tenantId");
        // Initialize BottomNavigationView and set the selected item to "Chat"
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_chat);  // Set the selected item to "Chat"

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

                // Handle the favorite button click (navigate to TenantFavorite)
            } else if (itemId == R.id.bottom_favorite) {
                Intent intent = new Intent(getApplicationContext(), TenantFavorite.class);
                intent.putExtra("tenantId", tenantId);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;

                // Handle the chat button click (stay on the same page)
            } else if (itemId == R.id.bottom_chat) {
                return true;  // Stay on the current activity

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
}
