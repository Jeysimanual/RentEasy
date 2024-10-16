package com.example.rentalms;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LanlordRegister extends AppCompatActivity {

    TextView landlordlogin;
    Button createAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lanlord_register);

        landlordlogin = findViewById(R.id.landlordlogin);
        createAccountButton = findViewById(R.id.btnCreateAccount);
        createAccountButton.setOnClickListener(view -> {
            // Show a toast message
            Toast.makeText(LanlordRegister.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
            // Navigate to the login activity
            startActivity(new Intent(LanlordRegister.this, LandlordLogin.class));
        });

        landlordlogin.setOnClickListener(v -> {
            startActivity(new Intent(LanlordRegister.this, LandlordLogin.class));
        });

    }

}
