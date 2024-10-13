package com.example.rentalms;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LanlordRegister extends AppCompatActivity {

    TextView landlordlogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lanlord_register);

        landlordlogin = findViewById(R.id.landlordlogin);
            landlordlogin.setOnClickListener(v -> {
                startActivity(new Intent(LanlordRegister.this, LandlordLogin.class));
            });

    }
}