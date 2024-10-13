package com.example.rentalms;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TenantRegister extends AppCompatActivity {

    TextView tenantlogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_register);
    tenantlogin = findViewById(R.id.tenantlogin);
        tenantlogin.setOnClickListener(view -> {
            startActivity(new Intent(TenantRegister.this, TenantLogin.class));
    });
    }
}