package com.example.rentalms;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class TenantPropertyAdapter extends RecyclerView.Adapter<TenantPropertyAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Property> propertyList;

    public TenantPropertyAdapter(Context context, ArrayList<Property> propertyList) {
        this.context = context;
        this.propertyList = propertyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tenant_property_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Property property = propertyList.get(position);

        // Set property name, barangay, and address
        holder.propertyNameTextView.setText(property.getPropertyName());
        holder.cityTextView.setText(property.getCity());
        holder.propertyTypeTextView.setText("Type: " + property.getType());
        holder.barangayTextView.setText(property.getBarangay());
        holder.addressTextView.setText(property.getAddress());

        // Get the price, ensuring there is only one ₱ sign
        String price = property.getPrice();  // Assuming price is already in the format ₱2000
        if (price != null && !price.startsWith("₱")) {
            price = "₱" + price;  // Prepend ₱ if it's missing
        }

        // Get payment period (Monthly or Yearly)
        String paymentPeriod = property.getPaymentPeriod();

        // If there's a payment period, append it to the price
        if (paymentPeriod != null && !paymentPeriod.isEmpty()) {
            holder.priceTextView.setText(price + " " + paymentPeriod);
        } else {
            holder.priceTextView.setText(price);  // Just display the price if no payment period
        }

        // Load image
        Glide.with(context)
                .load(property.getInteriorImageUrl())
                .placeholder(R.drawable.default_image)
                .into(holder.propertyImageView);

        // Set click listener for each item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PropertyDetailsActivity.class);
            intent.putExtra("userId", property.getUserId());
            intent.putExtra("propertyName", property.getPropertyName());
            intent.putExtra("province", property.getProvince());
            intent.putExtra("city", property.getCity());
            intent.putExtra("price", property.getPrice());
            intent.putExtra("type", property.getType());
            intent.putExtra("interiorImageUrl", property.getInteriorImageUrl());
            intent.putExtra("exteriorImageUrl", property.getExteriorImageUrl());
            intent.putExtra("barangay", property.getBarangay());
            intent.putExtra("address", property.getAddress());
            intent.putExtra("paymentPeriod", property.getPaymentPeriod()); // Add paymentPeriod to the intent
            context.startActivity(intent);
        });
    }



    @Override
    public int getItemCount() {
        return propertyList.size();
    }

    // Method to update the property list
    public void updateList(ArrayList<Property> filteredList) {
        propertyList = filteredList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView propertyNameTextView;
        public TextView cityTextView;
        public TextView priceTextView;
        public TextView propertyTypeTextView;
        public TextView barangayTextView;  // New TextView for barangay
        public TextView addressTextView;   // New TextView for address
        public ImageView propertyImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            propertyNameTextView = itemView.findViewById(R.id.propertyName);
            barangayTextView = itemView.findViewById(R.id.barangay);    // Initialize barangay TextView
            addressTextView = itemView.findViewById(R.id.address);      // Initialize address TextView
            cityTextView = itemView.findViewById(R.id.city);
            priceTextView = itemView.findViewById(R.id.price);
            propertyTypeTextView = itemView.findViewById(R.id.propertyType);
            propertyImageView = itemView.findViewById(R.id.propertyImage);
        }
    }
}
