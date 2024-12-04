package com.project.rentalms;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TenantPropertyAdapter extends RecyclerView.Adapter<TenantPropertyAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Property> propertyList;
    private FirebaseFirestore firestore;
    private String tenantId;  // Tenant ID of the logged-in user

    public TenantPropertyAdapter(Context context, ArrayList<Property> propertyList, String tenantId) {
        this.context = context;
        this.propertyList = propertyList;
        this.tenantId = tenantId;
        this.firestore = FirebaseFirestore.getInstance();
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
                .load(property.getInteriorImageUrl())
                .placeholder(R.drawable.default_image)
                .into(holder.propertyImageView);

        // Check if the property is a favorite and set the button accordingly
        checkIfFavorite(property.getPropertyId(), holder.favoriteButton);

        // Set click listener for the favorite button
        holder.favoriteButton.setOnClickListener(v -> {
            // Handle the case where getIsFavorite might be null
            boolean isFavorite = (property.getIsFavorite() != null && property.getIsFavorite()) ? false : true;

            property.setIsFavorite(isFavorite);  // Update property status

            // Update Firestore and UI
            if (isFavorite) {
                saveFavoriteStatus(property);                // Save property to favorites
                holder.favoriteButton.setImageResource(R.drawable.baseline_favorite_filled);
                Toast.makeText(context, "Property added to favorites", Toast.LENGTH_SHORT).show();
            } else {
                removeFavoriteStatus(property);              // Remove property from favorites
                holder.favoriteButton.setImageResource(R.drawable.baseline_favorite);
                Toast.makeText(context, "Property removed from favorites", Toast.LENGTH_SHORT).show();
            }
        });
        List<String> featuresList = property.getFeatures();
        // Set click listener for item view to open property details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PropertyDetailsActivity.class);
            intent.putExtra("userId", property.getUserId());
            intent.putExtra("propertyName", property.getPropertyName());
            intent.putExtra("province", property.getProvince());
            intent.putExtra("city", property.getCity());
            intent.putExtra("price", property.getPrice());
            intent.putExtra("type", property.getType());
            intent.putExtra("description", property.getDescription());
            intent.putExtra("exteriorImageUrl", property.getExteriorImageUrl());
            intent.putExtra("interiorImageUrl", property.getInteriorImageUrl());
            intent.putExtra("barangay", property.getBarangay());
            intent.putExtra("address", property.getAddress());
            intent.putExtra("paymentPeriod", property.getPaymentPeriod());
            intent.putStringArrayListExtra("features", new ArrayList<>(featuresList));  // Pass list of features
            context.startActivity(intent);
        });
    }

    private void checkIfFavorite(String propertyId, ImageButton favoriteButton) {
        if (tenantId == null || propertyId == null) {
            return;
        }

        // Reference to the tenant's favorites collection in Firestore
        CollectionReference favoritesRef = firestore
                .collection("Tenants")
                .document(tenantId)
                .collection("Favorite");

        // Check if the property is in the tenant's favorites collection
        favoritesRef.document(propertyId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // If the document exists, the property is marked as a favorite
                        favoriteButton.setImageResource(R.drawable.baseline_favorite_filled);
                    } else {
                        // If the document does not exist, set the default icon
                        favoriteButton.setImageResource(R.drawable.baseline_favorite);
                    }
                })
                .addOnFailureListener(e -> Log.e("TenantPropertyAdapter", "Failed to check favorite status", e));
    }

    @Override
    public int getItemCount() {
        return propertyList.size();
    }

    // Method to update the property list
    public void updateList(ArrayList<Property> filteredList) {
        this.propertyList = filteredList; // Update the adapter's list
        notifyDataSetChanged();           // Refresh RecyclerView
    }

    private void saveFavoriteStatus(Property property) {
        if (tenantId == null || property.getPropertyId() == null) {
            Log.e("TenantPropertyAdapter", "tenantId: " + tenantId + " | propertyId: " + property.getPropertyId());
            return;
        }

        // Reference to the tenant's favorites collection in Firestore
        CollectionReference favoritesRef = firestore
                .collection("Tenants")
                .document(tenantId)  // This must be a valid tenantId
                .collection("Favorite");

        // Save the property ID as a document in the 'Favorite' collection
        favoritesRef.document(property.getPropertyId())  // Use the propertyId from the Property object
                .set(new HashMap<>())
                .addOnSuccessListener(aVoid -> Log.d("TenantPropertyAdapter", "Favorite status saved successfully"))
                .addOnFailureListener(e -> Log.e("TenantPropertyAdapter", "Failed to save favorite status", e));
    }

    private void removeFavoriteStatus(Property property) {
        if (tenantId == null || property.getPropertyId() == null) {
            Log.e("TenantPropertyAdapter", "tenantId: " + tenantId + " | propertyId: " + property.getPropertyId());
            return;
        }

        // Reference to the tenant's favorites collection in Firestore
        CollectionReference favoritesRef = firestore
                .collection("Tenants")
                .document(tenantId)  // This must be a valid tenantId
                .collection("Favorite");

        // Remove the property ID from the 'Favorite' collection
        favoritesRef.document(property.getPropertyId())  // Use the propertyId from the Property object
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("TenantPropertyAdapter", "Favorite status removed successfully"))
                .addOnFailureListener(e -> Log.e("TenantPropertyAdapter", "Failed to remove favorite status", e));
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
            favoriteButton = itemView.findViewById(R.id.favoriteButton);  // Initialize favorite button
        }
    }
}
