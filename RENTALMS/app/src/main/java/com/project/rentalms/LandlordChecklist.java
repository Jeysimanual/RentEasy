package com.project.rentalms;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class LandlordChecklist extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landlord_checklist);  // Assuming this is your layout for the favorite page

        // Correct the IDs to match your XML resource
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.btn_checklist);  // Set the selected item to "Favorite"

        // Set up item selected listener for navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Handle the search button click (navigate to TenantPage)
            if (itemId == R.id.btn_home) {
                startActivity(new Intent(getApplicationContext(), LandlordPage.class));
                overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);  // Animation for navigation
                finish();
                return true;

                // Handle the favorite button click (stay on the same page)
            } else if (itemId == R.id.btn_checklist) {
                return true;  // Stay on the current activity

                // Handle the chat button click (navigate to TenantChat)
            } else if (itemId == R.id.btn_chat) {
                startActivity(new Intent(getApplicationContext(), LandlordChat.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;

                // Handle the more button click (navigate to TenantMore)
            } else if (itemId == R.id.btn_more) {
                startActivity(new Intent(getApplicationContext(), LandlordMore.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }

            return false;
        });
    }
}