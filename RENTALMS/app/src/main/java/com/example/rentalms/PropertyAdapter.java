package com.example.rentalms;

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
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.ViewHolder> {

    private ArrayList<Property> propertyList;
    private String userId;  // Add userId field to filter properties by user

    // Constructor to pass the userId to the adapter
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
        holder.propertyNameTextView.setText(property.getPropertyName());
        holder.barangayTextView.setText(property.getBarangay());
        holder.addressTextView.setText(property.getAddress());
        holder.cityTextView.setText(property.getCity());
        holder.priceTextView.setText(property.getPrice());

        // Set click listener for the delete button
        holder.deleteButton.setOnClickListener(v -> {
            // Remove the property from the list
            propertyList.remove(position);
            notifyItemRemoved(position);

            // Get the propertyName and pass it to the delete method
            deletePropertyFromFirebase(property.getPropertyName(), holder.itemView.getContext());
        });
    }

    @Override
    public int getItemCount() {
        return propertyList.size();
    }

    // Delete property from Firebase Firestore based on propertyName
    // Updated deletePropertyFromFirebase method
    private void deletePropertyFromFirebase(String propertyName, android.content.Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Navigate to the user's properties subcollection based on userId
        db.collection("Landlords").document(userId).collection("properties")
                .whereEqualTo("propertyName", propertyName)  // Filter by propertyName in the properties subcollection
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        // If property is found, delete it
                        for (DocumentSnapshot document : task.getResult()) {
                            DocumentReference propertyRef = document.getReference();
                            propertyRef.delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "Property deleted successfully", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Error deleting property", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        // If no property is found with the given propertyName
                        Toast.makeText(context, "Property not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error querying Firestore", Toast.LENGTH_SHORT).show();
                });


}

    // ViewHolder class to hold references to the views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView propertyNameTextView;
        public TextView cityTextView;
        public TextView barangayTextView;
        public TextView addressTextView;
        public TextView priceTextView;
        public Button deleteButton;  // Delete button

        public ViewHolder(View itemView) {
            super(itemView);
            propertyNameTextView = itemView.findViewById(R.id.propertyName);
            cityTextView = itemView.findViewById(R.id.city);
            barangayTextView = itemView.findViewById(R.id.barangay);
            addressTextView = itemView.findViewById(R.id.address);
            priceTextView = itemView.findViewById(R.id.price);
            deleteButton = itemView.findViewById(R.id.deleteButton);  // Initialize the delete button
        }
    }
}

