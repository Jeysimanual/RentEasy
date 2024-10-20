package com.example.rentalms;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TenantPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_page);

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
}
