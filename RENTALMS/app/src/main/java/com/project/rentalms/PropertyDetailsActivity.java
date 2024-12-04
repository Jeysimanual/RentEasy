package com.project.rentalms;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public class PropertyDetailsActivity extends AppCompatActivity {

    private TextView propertyNameTextView, barangayTextView, addressTextView, cityTextView, priceTextView, typeTextView, descriptionTextView, featuresTextView;
    private ViewPager2 imageSlider;
    private TabLayout indicatorTabLayout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_details);

        propertyNameTextView = findViewById(R.id.propertyNameDetails);
        barangayTextView = findViewById(R.id.barangayDetails);
        addressTextView = findViewById(R.id.addressDetails);
        cityTextView = findViewById(R.id.cityDetails);
        priceTextView = findViewById(R.id.priceDetails);
        descriptionTextView = findViewById(R.id.Property_description);
        featuresTextView = findViewById(R.id.features);
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
        String paymentPeriod = getIntent().getStringExtra("paymentPeriod"); // Retrieve paymentPeriod
        String description = getIntent().getStringExtra("description");
        String exteriorImageUrl = getIntent().getStringExtra("exteriorImageUrl");
        String interiorImageUrl = getIntent().getStringExtra("interiorImageUrl");
        String userId = getIntent().getStringExtra("userId");

        // Retrieve features as a list of strings
        ArrayList<String> featuresList = getIntent().getStringArrayListExtra("features");

        // Format features into a readable string
        StringJoiner featuresJoiner = new StringJoiner("\n");  // Use newline to separate features
        if (featuresList != null && !featuresList.isEmpty()) {
            for (String feature : featuresList) {
                featuresJoiner.add(feature);  // Add each feature to the Joiner
            }
        } else {
            featuresJoiner.add("No features available.");
        }
        String formattedFeatures = featuresJoiner.toString();
        // Set data to views
        propertyNameTextView.setText(propertyName);
        barangayTextView.setText(barangay);
        addressTextView.setText(address);
        cityTextView.setText(city);
        descriptionTextView.setText(description);
        featuresTextView.setText(formattedFeatures);  // Set the formatted features
        priceTextView.setText("Price:" + price + " " + paymentPeriod); // Set price with payment period
        typeTextView.setText("Type: " + type);

        // Image URLs list
        List<String> imageUrls = Arrays.asList(interiorImageUrl, exteriorImageUrl);

        // Set up the image slider adapter
        ImageSliderAdapter adapter = new ImageSliderAdapter(this, imageUrls);
        imageSlider.setAdapter(adapter);

        // Attach TabLayout to ViewPager2 for dot indicators
        new TabLayoutMediator(indicatorTabLayout, imageSlider,
                (tab, position) -> {
                    // No need to set text or icon as we just want dots
                }).attach();

        // Add inquire section fragment
        addInquireSectionFragment(propertyName, type, barangay, address, city, province, price, paymentPeriod, description, userId);
    }

    // Method to show the overlay fragment
    public void showInquireOverlayFragment(String propertyName, String type, String barangay, String address, String city, String province, String price, String paymentPeriod,String description, String userId) {
        InquireOverlayFragment inquireOverlayFragment = new InquireOverlayFragment();
        Bundle bundle = new Bundle();
        bundle.putString("propertyName", propertyName);
        bundle.putString("type", type);
        bundle.putString("barangay", barangay);
        bundle.putString("address", address);
        bundle.putString("city", city);
        bundle.putString("province", province);
        bundle.putString("price", price);
        bundle.putString("paymentPeriod", paymentPeriod); // Pass paymentPeriod to overlay fragment
        bundle.putString("description", description);
        bundle.putString("userId", userId);
        inquireOverlayFragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_up, R.anim.fade_out); // Ensure animations are defined
        transaction.replace(R.id.inquire_section_container, inquireOverlayFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void addInquireSectionFragment(String propertyName, String type, String barangay, String address, String city, String province, String price, String paymentPeriod, String description, String userId) {
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
        bundle.putString("paymentPeriod", paymentPeriod); // Add paymentPeriod to bundle
        bundle.putString("description", description);
        bundle.putString("userId", userId);


        inquireSectionFragment.setArguments(bundle);

        // Add the fragment to the container
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.inquire_section_container, inquireSectionFragment)
                .commit();
    }
}
