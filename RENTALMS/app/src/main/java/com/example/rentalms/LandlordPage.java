package com.example.rentalms;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class LandlordPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landlord_page);

        // Correct the IDs to match your XML resource
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.btn_home);

        // Set up item selected listener for navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Handle the search button click (remains on the same page)
            if (itemId == R.id.btn_home) {
                return true;

                // Handle the favorite button click (navigate to LandlordChecklist)
            } else if (itemId == R.id.btn_checklist) {
                startActivity(new Intent(getApplicationContext(), LandlordChecklist.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;

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