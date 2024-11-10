package com.example.rentalms;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.ViewHolder> {

    private ArrayList<Property> propertyList;
    private String userId;  // User ID to identify landlord

    // Constructor to initialize property list and user ID
    public PropertyAdapter(ArrayList<Property> propertyList, String userId) {
        this.propertyList = propertyList;
        this.userId = userId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.property_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Property property = propertyList.get(position);

        // Populate property details in UI
        holder.propertyNameTextView.setText(property.getPropertyName());
        holder.barangayTextView.setText(property.getBarangay());
        holder.addressTextView.setText(property.getAddress());
        holder.cityTextView.setText(property.getCity());
        holder.priceTextView.setText(property.getPrice());
        holder.paymentPeriodTextView.setText(property.getPaymentPeriod());

        // Set up delete button functionality
        holder.deleteButton.setOnClickListener(v -> {
            propertyList.remove(position);
            notifyItemRemoved(position);
            deletePropertyFromFirebase(property.getPropertyName(), holder.itemView.getContext());
        });

        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), EditProperty.class);

            // Log values to ensure they're not null
            Log.d("EditButton", "Landlord ID: " + userId);
            Log.d("EditButton", "Property Name: " + property.getPropertyName());
            Log.d("EditButton", "Barangay: " + property.getBarangay());
            Log.d("EditButton", "Address: " + property.getAddress());
            Log.d("EditButton", "City: " + property.getCity());
            Log.d("EditButton", "Price: " + property.getPrice());
            Log.d("EditButton", "Payment Period: " + property.getPaymentPeriod());

            // Pass necessary data to EditProperty activity
            intent.putExtra("landlordId", userId);
            intent.putExtra("propertyName", property.getPropertyName());
            intent.putExtra("barangay", property.getBarangay());
            intent.putExtra("address", property.getAddress());
            intent.putExtra("city", property.getCity());
            intent.putExtra("price", property.getPrice());
            intent.putExtra("paymentPeriod", property.getPaymentPeriod());

            // Start the EditProperty activity
            holder.itemView.getContext().startActivity(intent);
        });
    }
        @Override
    public int getItemCount() {
        return propertyList.size();
    }

    // Method to delete property from Firestore
    private void deletePropertyFromFirebase(String propertyName, android.content.Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query Firestore for property in landlord's properties subcollection
        db.collection("Landlords").document(userId).collection("properties")
                .whereEqualTo("propertyName", propertyName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            DocumentReference propertyRef = document.getReference();
                            propertyRef.delete()
                                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Property deleted successfully", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(context, "Error deleting property", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Toast.makeText(context, "Property not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Error querying Firestore", Toast.LENGTH_SHORT).show());
    }

    // ViewHolder class to hold references to the views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView propertyNameTextView;
        public TextView cityTextView;
        public TextView barangayTextView;
        public TextView addressTextView;
        public TextView priceTextView;
        public TextView paymentPeriodTextView;
        public Button deleteButton;
        public Button editButton;  // Edit button

        public ViewHolder(View itemView) {
            super(itemView);
            propertyNameTextView = itemView.findViewById(R.id.propertyName);
            cityTextView = itemView.findViewById(R.id.city);
            barangayTextView = itemView.findViewById(R.id.barangay);
            addressTextView = itemView.findViewById(R.id.address);
            priceTextView = itemView.findViewById(R.id.price);
            paymentPeriodTextView = itemView.findViewById(R.id.paymentPeriod);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editButton = itemView.findViewById(R.id.editButton);  // Initialize the edit button
        }
    }
}
