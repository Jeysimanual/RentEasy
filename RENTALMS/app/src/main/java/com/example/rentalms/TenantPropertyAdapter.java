package com.example.rentalms;

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
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.HashMap;

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
        holder.priceTextView.setText("Price: " + property.getPrice());
        holder.propertyTypeTextView.setText("Type: " + property.getType());
        holder.barangayTextView.setText(property.getBarangay());
        holder.addressTextView.setText(property.getAddress());

        Glide.with(context)
                .load(property.getExteriorImageUrl())
                .placeholder(R.drawable.default_image)
                .into(holder.propertyImageView);

        // Check if the property is a favorite and set the button accordingly
        checkIfFavorite(property.getPropertyId(), holder.favoriteButton);

        // Set click listener for the favorite button
        holder.favoriteButton.setOnClickListener(v -> {
            boolean isFavorite = !property.isFavorite();  // Toggle favorite status
            property.setFavorite(isFavorite);
            saveFavoriteStatus(property, isFavorite);
            holder.favoriteButton.setImageResource(isFavorite ?
                    R.drawable.baseline_favorite_filled : R.drawable.baseline_favorite);

            if (isFavorite) {
                String message = "Property added to favorites";
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
        // Set click listener for item view to open property details
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
        propertyList = filteredList;
        notifyDataSetChanged();
    }

    private void saveFavoriteStatus(Property property, boolean isFavorite) {
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