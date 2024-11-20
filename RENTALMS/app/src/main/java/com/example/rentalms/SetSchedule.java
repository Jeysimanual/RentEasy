package com.example.rentalms;

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
    private String tenantUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_schedule);

        db = FirebaseFirestore.getInstance();
        tenantUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Retrieve property details from the Intent
        String propertyName = getIntent().getStringExtra("propertyName");
        String barangay = getIntent().getStringExtra("barangay");
        String address = getIntent().getStringExtra("address");
        String city = getIntent().getStringExtra("city");

        // Display property details
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

            Map<String, Object> scheduleData = new HashMap<>();
            scheduleData.put("propertyName", propertyName);
            scheduleData.put("barangay", barangay);
            scheduleData.put("address", address);
            scheduleData.put("city", city);
            scheduleData.put("date", scheduleDate);
            scheduleData.put("time", scheduleTime);
            scheduleData.put("status", "Pending");

            db.collection("Tenants")
                    .document(tenantUserId)
                    .collection("Schedules")
                    .add(scheduleData)
                    .addOnSuccessListener(documentReference -> {
                        new AlertDialog.Builder(SetSchedule.this)
                                .setTitle("Thank You!")
                                .setMessage("Your schedule has been set.\nDate: " + scheduleDate +
                                        "\nTime: " + scheduleTime +
                                        "\nProperty: " + propertyName +
                                        "\nPlease wait for the landlord's confirmation.")
                                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                .show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(SetSchedule.this, "Failed to set schedule. Please try again.", Toast.LENGTH_SHORT).show();
                    });
        });
    }


}