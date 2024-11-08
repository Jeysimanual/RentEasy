package com.example.rentalms;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class LandlordChat extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landlord_chat);  // Assuming this is your layout for the chat page

        // Initialize BottomNavigationView and set the selected item to "Chat"
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.btn_chat);  // Set the selected item to "Chat"

        // Set up item selected listener for navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();


            if (itemId == R.id.btn_home) {
                startActivity(new Intent(getApplicationContext(), LandlordPage.class));
                overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);  // Animation for navigation
                finish();
                return true;


            } else if (itemId == R.id.btn_checklist) {
                startActivity(new Intent(getApplicationContext(), LandlordChecklist.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;


            } else if (itemId == R.id.btn_chat) {
                return true;


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