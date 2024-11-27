package com.project.rentalms;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class LandlordSchedule extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ScheduleAdapter adapter;
    private ArrayList<Schedule> scheduleList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landlord_schedule);

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.rvLandlordSchedules);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        scheduleList = new ArrayList<>();
        adapter = new ScheduleAdapter(scheduleList);
        recyclerView.setAdapter(adapter);

        // Load landlord's schedules
        loadLandlordSchedules();
    }

    private void loadLandlordSchedules() {
        String userId = auth.getCurrentUser().getUid();

        if (userId != null) {
            // Fetch properties owned by the landlord
            db.collection("Landlords")
                    .document(userId)
                    .collection("Properties")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            ArrayList<String> ownedProperties = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String propertyName = document.getString("propertyName");
                                if (propertyName != null) {
                                    ownedProperties.add(propertyName);
                                    Log.d("LandlordSchedule", "Property found: " + propertyName);
                                }
                            }

                            // Only proceed if we have properties
                            if (!ownedProperties.isEmpty()) {
                                loadSchedulesForProperties(ownedProperties);
                            } else {
                                Log.e("FirestoreError", "No properties found for landlord");
                                Toast.makeText(this, "No properties found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("FirestoreError", "Error fetching properties", task.getException());
                            Toast.makeText(this, "Failed to load properties", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSchedulesForProperties(ArrayList<String> ownedProperties) {
        db.collection("Tenants")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy");
                        Date today = new Date();

                        for (QueryDocumentSnapshot tenantDoc : task.getResult()) {
                            String tenantId = tenantDoc.getId();
                            // Fetch tenant's schedules
                            db.collection("Tenants")
                                    .document(tenantId)
                                    .collection("Schedules")
                                    .get()
                                    .addOnCompleteListener(scheduleTask -> {
                                        if (scheduleTask.isSuccessful()) {
                                            for (QueryDocumentSnapshot scheduleDoc : scheduleTask.getResult()) {
                                                String propertyName = scheduleDoc.getString("propertyName");
                                                String date = scheduleDoc.getString("date");
                                                String time = scheduleDoc.getString("time");
                                                String status = scheduleDoc.getString("status");

                                                // New fields added: barangay, address, city
                                                String barangay = scheduleDoc.getString("barangay");
                                                String address = scheduleDoc.getString("address");
                                                String city = scheduleDoc.getString("city");

                                                // Log the schedule data
                                                Log.d("LandlordSchedule", "Schedule found for property: " + propertyName + " on " + date);

                                                // Only add the schedule if the property name is in the landlord's owned properties
                                                if (ownedProperties.contains(propertyName)) {
                                                    try {
                                                        Date scheduleDate = sdf.parse(date);

                                                        if (!isDateBeforeToday(scheduleDate, today)) {
                                                            // Add the schedule to the list
                                                            scheduleList.add(new Schedule(date, time, status, propertyName, barangay, address, city));
                                                        }
                                                    } catch (ParseException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }

                                            // Sort and update the adapter
                                            sortSchedules();
                                            adapter.notifyDataSetChanged();
                                        } else {
                                            // Handle permission denied
                                            Log.e("FirestoreError", "Error fetching schedules: " + scheduleTask.getException());
                                            Toast.makeText(LandlordSchedule.this, "Failed to load schedules. Check permissions.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Log.e("FirestoreError", "Error fetching tenants: " + task.getException());
                        Toast.makeText(this, "Failed to load tenants", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isDateBeforeToday(Date scheduleDate, Date today) {
        // Check if the scheduleDate is before today but not equal
        return scheduleDate.before(today) && !isSameDay(scheduleDate, today);
    }

    private boolean isSameDay(Date date1, Date date2) {
        // Simple date comparison for day, month, and year
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy");
        return sdf.format(date1).equals(sdf.format(date2));
    }

    private void sortSchedules() {
        // Define a SimpleDateFormat for parsing full dates (including year)
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy");

        // Sort the schedule list based on the full date
        Collections.sort(scheduleList, new Comparator<Schedule>() {
            @Override
            public int compare(Schedule s1, Schedule s2) {
                try {
                    // Parse the date strings into Date objects
                    Date date1 = sdf.parse(s1.getDate());
                    Date date2 = sdf.parse(s2.getDate());

                    // Compare the parsed dates
                    return date1.compareTo(date2); // Ascending order (earliest first)
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });
    }
}
