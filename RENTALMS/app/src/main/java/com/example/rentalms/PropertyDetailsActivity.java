package com.example.rentalms;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;

public class PropertyDetailsActivity extends AppCompatActivity {

    private TextView propertyNameTextView, cityTextView, priceTextView, typeTextView;
    private ImageView exteriorImageView, interiorImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_details);

        propertyNameTextView = findViewById(R.id.propertyNameDetails);
        cityTextView = findViewById(R.id.cityDetails);
        priceTextView = findViewById(R.id.priceDetails);
        typeTextView = findViewById(R.id.typeDetails);
        exteriorImageView = findViewById(R.id.exteriorImage);
        interiorImageView = findViewById(R.id.interiorImage);

        // Retrieve data from the Intent
        String propertyName = getIntent().getStringExtra("propertyName");
        String city = getIntent().getStringExtra("city");
        String province = getIntent().getStringExtra("province");
        String price = getIntent().getStringExtra("price");
        String type = getIntent().getStringExtra("type");
        String exteriorImageUrl = getIntent().getStringExtra("exteriorImageUrl");
        String interiorImageUrl = getIntent().getStringExtra("interiorImageUrl");
        String userId = getIntent().getStringExtra("userId");

        // Set data to views
        propertyNameTextView.setText(propertyName);
        cityTextView.setText(city);
        priceTextView.setText("Price: ₱" + price);
        typeTextView.setText("Type: " + type);



        Glide.with(this).load(exteriorImageUrl).placeholder(R.drawable.default_image).into(exteriorImageView);
        Glide.with(this).load(interiorImageUrl).placeholder(R.drawable.default_image).into(interiorImageView);


        addInquireSectionFragment(propertyName, type, city, province, price, userId);

    }

    // Method to show the overlay fragment
    public void showInquireOverlayFragment(String propertyName, String type, String city, String province, String price, String userId) {
        InquireOverlayFragment inquireOverlayFragment = new InquireOverlayFragment();
        Bundle bundle = new Bundle();
        bundle.putString("propertyName", propertyName);
        bundle.putString("type", type);
        bundle.putString("city", city);
        bundle.putString("province", province);
        bundle.putString("price", price);
        bundle.putString("userId", userId);
        inquireOverlayFragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_up, R.anim.fade_out); // Ensure animations are defined
        transaction.replace(R.id.inquire_section_container, inquireOverlayFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    private void addInquireSectionFragment(String propertyName, String type, String city, String province, String price, String userId) {
        InquireSectionFragment inquireSectionFragment = new InquireSectionFragment();

        // Set up the bundle with property data
        Bundle bundle = new Bundle();
        bundle.putString("propertyName", propertyName);
        bundle.putString("type", type);
        bundle.putString("city", city);
        bundle.putString("province", province);
        bundle.putString("price", price);
        bundle.putString("userId", userId);

        inquireSectionFragment.setArguments(bundle);

        // Add the fragment to the container
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.inquire_section_container, inquireSectionFragment)
                .commit();
    }



}