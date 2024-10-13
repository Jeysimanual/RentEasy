package com.example.rentalms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TenantLogin extends AppCompatActivity {

    TextView Tenantcreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_login);
        Tenantcreate = findViewById(R.id.Tenantcreate);
            Tenantcreate.setOnClickListener(view -> {
               startActivity(new Intent(TenantLogin.this, TenantRegister.class));
            });
    }

    public void tenantpage(View view) {
        startActivity(new Intent(this, TenantPage.class));

    }
}