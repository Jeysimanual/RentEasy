package com.project.rentalms;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class InquireSectionFragment extends Fragment {

    private Button inquireButton;
    private TextView priceTextView;

    private String propertyName;
    private String type;
    private String barangay;
    private String address;
    private String city;
    private String province;
    private String price;
    private String paymentPeriod;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inquire_section, container, false);

        if (getArguments() != null) {
            propertyName = getArguments().getString("propertyName", "Property");
            type = getArguments().getString("type", "Type");
            barangay = getArguments().getString("barangay", "Barangay");
            address = getArguments().getString("address", "Address");
            city = getArguments().getString("city", "City");
            province = getArguments().getString("province", "Province");
            price = getArguments().getString("price", "Price");
            paymentPeriod = getArguments().getString("paymentPeriod", "Period"); // Retrieve paymentPeriod
            userId = getArguments().getString("userId", "UserId");
        }

        priceTextView = view.findViewById(R.id.priceTextView);

        // Set the price with payment period in priceTextView
        priceTextView.setText("Price:" + price + " " + paymentPeriod);

        inquireButton = view.findViewById(R.id.inquireButton);
        inquireButton.setOnClickListener(v -> {
            ((PropertyDetailsActivity) getActivity()).showInquireOverlayFragment(
                    propertyName, type, barangay, address, city, province, price, paymentPeriod, userId);
        });

        return view;
    }
}
