package com.project.rentalms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CreateAccount extends AppCompatActivity {

    TextView btnlogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.project.rentalms.R.layout.create_account);

        btnlogin = findViewById(R.id.btnlogin);
        btnlogin.setOnClickListener(view -> {
            startActivity(new Intent(CreateAccount.this, Login.class));
        });


    }

    public void openCreateAccount1(View view) {
        startActivity(new Intent(this, TenantRegister.class));
    }
    public void openCreateAccount2(View view) {
        startActivity(new Intent(this, LanlordRegister.class));
    }
}
