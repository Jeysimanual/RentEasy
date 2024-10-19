package com.example.rentalms;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LandlordAddProperty extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landlord_add_property);

        // Retrieve the username passed from the previous activity
        String landlordUsername = getIntent().getStringExtra("username");

        // Display the username or use it for other logic
        TextView textView = findViewById(R.id.landlordWelcome);
        textView.setText("Welcome " + landlordUsername);
    }
}