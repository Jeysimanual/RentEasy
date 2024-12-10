package com.project.rentalms;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {
    private ArrayList<Schedule> scheduleList;
    private Context context;  // Store the context to check the current activity

    public ScheduleAdapter(ArrayList<Schedule> scheduleList, Context context) {
        this.scheduleList = scheduleList;
        this.context = context;  // Save the context for later use
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shedule, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        Schedule schedule = scheduleList.get(position);

        holder.tvPropertyName.setText("Property Name: " + schedule.getPropertyName());
        holder.tvBarangay.setText(schedule.getBarangay() + " ");
        holder.tvAddress.setText(schedule.getAddress() + " ");
        holder.tvCity.setText(schedule.getCity());
        holder.tvDate.setText("Date: " + schedule.getDate());
        holder.tvTime.setText("Time: " + schedule.getTime());
        holder.tvStatus.setText("Status: " + schedule.getStatus());

        // Check if we are in LandlordSchedule or ScheduleVisit and show/hide buttons accordingly
        if (context instanceof LandlordSchedule) {
            holder.buttonsLayout.setVisibility(View.VISIBLE); // Show buttons in LandlordSchedule
        } else {
            holder.buttonsLayout.setVisibility(View.GONE); // Hide buttons in ScheduleVisit
        }
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView tvPropertyName, tvBarangay, tvAddress, tvCity, tvDate, tvTime, tvStatus;
        LinearLayout buttonsLayout; // Reference to the layout holding the buttons

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);

            tvPropertyName = itemView.findViewById(R.id.tvpropertyname);
            tvBarangay = itemView.findViewById(R.id.tvbarangay);
            tvAddress = itemView.findViewById(R.id.tvaddress);
            tvCity = itemView.findViewById(R.id.tvcity);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            buttonsLayout = itemView.findViewById(R.id.buttonsLayout); // Initialize the buttons layout
        }
    }
}

