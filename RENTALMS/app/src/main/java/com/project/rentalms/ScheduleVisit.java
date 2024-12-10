package com.project.rentalms;

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

public class ScheduleVisit extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ScheduleAdapter adapter;
    private ArrayList<Schedule> scheduleList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_visit);

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.rvSchedules);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        scheduleList = new ArrayList<>();
        // When setting up the adapter in LandlordSchedule:
        adapter = new ScheduleAdapter(scheduleList, this); // 'this' refers to the LandlordSchedule activity
        recyclerView.setAdapter(adapter);


        // Load schedules for the currently logged-in tenant
        loadTenantSchedules();
    }

    private void loadTenantSchedules() {
        // Get the currently logged-in tenant's user ID
        String tenantId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (tenantId != null) {
            // Query to retrieve schedules specific to the tenant from their "Schedules" sub-collection
            db.collection("Tenants")
                    .document(tenantId)
                    .collection("Schedules")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy");
                            Date today = new Date();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String id = document.getId();
                                String date = document.getString("date");
                                String time = document.getString("time");
                                String status = document.getString("status");
                                String propertyName = document.getString("propertyName");
                                String barangay = document.getString("barangay");
                                String address = document.getString("address");
                                String city = document.getString("city");

                                try {
                                    Date scheduleDate = sdf.parse(date);

                                    // Add to the schedule list only if the date is not in the past
                                    if (!isDateBeforeToday(scheduleDate, today)) {
                                        scheduleList.add(new Schedule(date, time, status, propertyName, barangay, address, city));
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                            sortSchedules();
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.e("FirestoreError", "Error fetching schedules", task.getException());
                            Toast.makeText(this, "Failed to load schedules", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isDateBeforeToday(Date scheduleDate, Date today) {
        return scheduleDate.before(today) && !isSameDay(scheduleDate, today);
    }

    private boolean isSameDay(Date date1, Date date2) {
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy");
        return sdf.format(date1).equals(sdf.format(date2));
    }

    private void sortSchedules() {
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy");

        Collections.sort(scheduleList, (s1, s2) -> {
            try {
                Date date1 = sdf.parse(s1.getDate());
                Date date2 = sdf.parse(s2.getDate());
                return date1.compareTo(date2);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 0;
        });
    }
}
