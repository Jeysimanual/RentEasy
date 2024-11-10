package com.example.rentalms;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Arrays;
import java.util.List;

public class PropertyDetailsActivity extends AppCompatActivity {

    private TextView propertyNameTextView, barangayTextView, addressTextView, cityTextView, priceTextView, typeTextView;
    private ViewPager2 imageSlider;
    private TabLayout indicatorTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_details);

        propertyNameTextView = findViewById(R.id.propertyNameDetails);
        barangayTextView = findViewById(R.id.barangayDetails);
        addressTextView = findViewById(R.id.addressDetails);
        cityTextView = findViewById(R.id.cityDetails);
        priceTextView = findViewById(R.id.priceDetails);
        typeTextView = findViewById(R.id.typeDetails);
        imageSlider = findViewById(R.id.imageSlider);
        indicatorTabLayout = findViewById(R.id.indicatorTabLayout);

        // Retrieve data from the Intent
        String propertyName = getIntent().getStringExtra("propertyName");
        String barangay = getIntent().getStringExtra("barangay");
        String address = getIntent().getStringExtra("address");
        String city = getIntent().getStringExtra("city");
        String province = getIntent().getStringExtra("province");
        String price = getIntent().getStringExtra("price");
        String type = getIntent().getStringExtra("type");
        String exteriorImageUrl = getIntent().getStringExtra("exteriorImageUrl");
        String interiorImageUrl = getIntent().getStringExtra("interiorImageUrl");
        String userId = getIntent().getStringExtra("userId");

        // Set data to views
        propertyNameTextView.setText(propertyName);
        barangayTextView.setText(barangay);
        addressTextView.setText(address);
        cityTextView.setText(city);
        priceTextView.setText("Price: " + price);
        typeTextView.setText("Type: " + type);

        // Image URLs list
        List<String> imageUrls = Arrays.asList(exteriorImageUrl, interiorImageUrl);

        // Set up the image slider adapter
        ImageSliderAdapter adapter = new ImageSliderAdapter(this, imageUrls);
        imageSlider.setAdapter(adapter);

        // Attach TabLayout to ViewPager2 for dot indicators
        new TabLayoutMediator(indicatorTabLayout, imageSlider,
                (tab, position) -> {
                    // No need to set text or icon as we just want dots
                }).attach();
        // Add inquire section fragment
        addInquireSectionFragment(propertyName, type, barangay, address, city, province, price, userId);
    }

    // Method to show the overlay fragment
    public void showInquireOverlayFragment(String propertyName, String type, String barangay, String address, String city, String province, String price, String userId) {
        InquireOverlayFragment inquireOverlayFragment = new InquireOverlayFragment();
        Bundle bundle = new Bundle();
        bundle.putString("propertyName", propertyName);
        bundle.putString("type", type);
        bundle.putString("barangay", barangay);
        bundle.putString("address", address);
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

    private void addInquireSectionFragment(String propertyName, String type, String barangay, String address, String city, String province, String price, String userId) {
        InquireSectionFragment inquireSectionFragment = new InquireSectionFragment();

        // Set up the bundle with property data
        Bundle bundle = new Bundle();
        bundle.putString("propertyName", propertyName);
        bundle.putString("type", type);
        bundle.putString("barangay", barangay);
        bundle.putString("address", address);
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
