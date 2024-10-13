package com.example.rentalms;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LandlordLogin extends AppCompatActivity {
    TextView Landlordcreate;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landlord_login);

        Landlordcreate = findViewById(R.id.Landlordcreate);
        Landlordcreate.setOnClickListener(view -> {
            startActivity(new Intent(LandlordLogin.this, LanlordRegister.class));
        });

    }
    public void landlordpage(View view) {
        startActivity(new Intent(this, LandlordPage.class));
    }
}