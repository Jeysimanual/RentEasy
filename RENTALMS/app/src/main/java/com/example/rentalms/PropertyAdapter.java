package com.example.rentalms;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.ViewHolder> {

    private ArrayList<Property> propertyList;

    public PropertyAdapter(ArrayList<Property> propertyList) {
        this.propertyList = propertyList;
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
        holder.cityTextView.setText(property.getCity());
        holder.priceTextView.setText(property.getPrice());
        // You can load images using a library like Glide or Picasso if needed
    }

    @Override
    public int getItemCount() {
        return propertyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView propertyNameTextView;
        public TextView cityTextView;
        public TextView priceTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            propertyNameTextView = itemView.findViewById(R.id.propertyName);
            cityTextView = itemView.findViewById(R.id.city);
            priceTextView = itemView.findViewById(R.id.price);
        }
    }
}
