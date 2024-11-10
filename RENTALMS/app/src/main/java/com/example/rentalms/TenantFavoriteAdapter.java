package com.example.rentalms;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TenantFavoriteAdapter extends RecyclerView.Adapter<TenantFavoriteAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Property> favoritePropertyList;
    private FirebaseFirestore firestore;
    private String tenantId;

    public TenantFavoriteAdapter(Context context, ArrayList<Property> favoritePropertyList, String tenantId) {
        this.context = context;
        this.favoritePropertyList = favoritePropertyList;
        this.tenantId = tenantId;
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tenant_favorite_property, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Property property = favoritePropertyList.get(position);
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

        Glide.with(context)
                .load(property.getExteriorImageUrl())
                .placeholder(R.drawable.default_image)
                .into(holder.propertyImageView);

        holder.favoriteButton.setImageResource(R.drawable.baseline_favorite_filled); // Filled icon for favorite state

        // Unfavorite button click listener
        holder.favoriteButton.setOnClickListener(v -> {
            // Remove from Firestore
            firestore.collection("Tenants")
                    .document(tenantId)
                    .collection("Favorite")
                    .document(property.getPropertyId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Remove property from the local list and update the adapter
                        favoritePropertyList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, favoritePropertyList.size());

                        Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Failed to remove favorite", Toast.LENGTH_SHORT).show()
                    );
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PropertyDetailsActivity.class);
            intent.putExtra("userId", property.getUserId());
            intent.putExtra("propertyName", property.getPropertyName());
            intent.putExtra("province", property.getProvince());
            intent.putExtra("city", property.getCity());
            intent.putExtra("price", property.getPrice());
            intent.putExtra("type", property.getType());
            intent.putExtra("exteriorImageUrl", property.getExteriorImageUrl());
            intent.putExtra("interiorImageUrl", property.getInteriorImageUrl());
            intent.putExtra("barangay", property.getBarangay());
            intent.putExtra("address", property.getAddress());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return favoritePropertyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView propertyNameTextView;
        public TextView cityTextView;
        public TextView priceTextView;
        public TextView propertyTypeTextView;
        public TextView barangayTextView;
        public TextView addressTextView;
        public ImageView propertyImageView;
        public ImageButton favoriteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            propertyNameTextView = itemView.findViewById(R.id.propertyName);
            barangayTextView = itemView.findViewById(R.id.barangay);
            addressTextView = itemView.findViewById(R.id.address);
            cityTextView = itemView.findViewById(R.id.city);
            priceTextView = itemView.findViewById(R.id.price);
            propertyTypeTextView = itemView.findViewById(R.id.propertyType);
            propertyImageView = itemView.findViewById(R.id.propertyImage);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
        }
    }
}
