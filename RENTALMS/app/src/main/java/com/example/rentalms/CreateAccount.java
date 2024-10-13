package com.example.rentalms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class CreateAccount extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account);

    }

    public void openCreateAccount1(View view) {
        startActivity(new Intent(this, TenantRegister.class));
    }
    public void openCreateAccount2(View view) {
        startActivity(new Intent(this, LanlordRegister.class));
    }
}
