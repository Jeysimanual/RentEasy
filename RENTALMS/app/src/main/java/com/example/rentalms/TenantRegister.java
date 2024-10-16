package com.example.rentalms;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class TenantRegister extends AppCompatActivity {

    TextView tenantlogin;
    Button createAccountButton; // Declare the button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_register);

        tenantlogin = findViewById(R.id.tenantlogin);
        createAccountButton = findViewById(R.id.btnCreateAccount); // Initialize the button

        // Set the click listener for the "Create Account" button
        createAccountButton.setOnClickListener(view -> {
            // Show a toast message
            Toast.makeText(TenantRegister.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
            // Navigate to the login activity
            startActivity(new Intent(TenantRegister.this, TenantLogin.class));
        });

        // Set the click listener for the "Already have an account?" text
        tenantlogin.setOnClickListener(view -> {
            startActivity(new Intent(TenantRegister.this, TenantLogin.class));
        });
    }
}
