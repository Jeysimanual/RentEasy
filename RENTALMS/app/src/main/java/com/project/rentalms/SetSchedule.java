package com.project.rentalms;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SetSchedule extends AppCompatActivity {

    private FirebaseFirestore db;
    private String tenantUserId, landlordId, propertyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_schedule);

        db = FirebaseFirestore.getInstance();
        tenantUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Retrieve property and landlord details from Intent
        String propertyName = getIntent().getStringExtra("propertyName");
        String barangay = getIntent().getStringExtra("barangay");
        String address = getIntent().getStringExtra("address");
        String city = getIntent().getStringExtra("city");
        landlordId = getIntent().getStringExtra("landlordId"); // Directly received
        propertyId = getIntent().getStringExtra("propertyId");

        TextView tvPropertyName = findViewById(R.id.tv_property_name);
        TextView tvBarangay = findViewById(R.id.tv_barangay);
        TextView tvAddress = findViewById(R.id.tv_address);
        TextView tvCity = findViewById(R.id.tv_city);

        tvPropertyName.setText(propertyName);
        tvBarangay.setText(barangay);
        tvAddress.setText(address);
        tvCity.setText(city);

        DatePicker datePicker = findViewById(R.id.date_picker);
        TimePicker timePicker = findViewById(R.id.time_picker);
        Button btnSetSchedule = findViewById(R.id.btn_set_schedule);
        Button btnMySchedule = findViewById(R.id.btn_my_schedule);

        btnMySchedule.setOnClickListener(view -> {
            Intent intent = new Intent(SetSchedule.this, ScheduleVisit.class);
            startActivity(intent);
        });

        Calendar calendar = Calendar.getInstance();
        datePicker.setMinDate(calendar.getTimeInMillis());
        timePicker.setIs24HourView(false);

        btnSetSchedule.setOnClickListener(view -> {
            int day = datePicker.getDayOfMonth();
            int month = datePicker.getMonth() + 1;
            int year = datePicker.getYear();
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            String amPm = (hour >= 12) ? "PM" : "AM";
            hour = (hour > 12) ? (hour - 12) : (hour == 0 ? 12 : hour);

            String scheduleDate = String.format("%d/%d/%d", day, month, year);
            String scheduleTime = String.format("%02d:%02d %s", hour, minute, amPm);

            if (landlordId == null || propertyId == null) {
                Toast.makeText(this, "Property or landlord information is missing.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create schedule data
            Map<String, Object> scheduleData = new HashMap<>();
            scheduleData.put("propertyName", propertyName);
            scheduleData.put("barangay", barangay);
            scheduleData.put("address", address);
            scheduleData.put("city", city);
            scheduleData.put("date", scheduleDate);
            scheduleData.put("time", scheduleTime);
            scheduleData.put("status", "Pending");
            scheduleData.put("tenantId", tenantUserId);

            // Save only in the landlord's collection
            db.collection("Landlords")
                    .document(landlordId)
                    .collection("properties")
                    .document(propertyId)
                    .collection("Schedules")
                    .add(scheduleData)
                    .addOnSuccessListener(documentReference -> {
                        // Save the schedule in the tenant's collection as well
                        db.collection("Tenants")
                                .document(tenantUserId)
                                .collection("Schedules")
                                .add(scheduleData)
                                .addOnSuccessListener(tenantDocRef -> {
                                    new AlertDialog.Builder(SetSchedule.this)
                                            .setTitle("Thank You!")
                                            .setMessage("Your schedule has been set:\nDate: " + scheduleDate +
                                                    "\nTime: " + scheduleTime +
                                                    "\nProperty: " + propertyName +
                                                    "\nPlease wait for confirmation.")
                                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                            .show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save the schedule in Tenant's collection.", Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to save the schedule in Landlord's collection.", Toast.LENGTH_SHORT).show());
        });
    }
}
