package com.example.rentalms;

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
        adapter = new ScheduleAdapter(scheduleList);
        recyclerView.setAdapter(adapter);

        // Load schedules dynamically for the logged-in tenant
        loadSchedules();
    }

    private void loadSchedules() {
        String userId = auth.getCurrentUser().getUid();

        if (userId != null) {
            db.collection("Tenants")
                    .document(userId)
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

                                    if (isDateBeforeToday(scheduleDate, today)) {
                                        deleteSchedule(userId, id);
                                    } else {
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
        // Check if the scheduleDate is before today but not equal
        return scheduleDate.before(today) && !isSameDay(scheduleDate, today);
    }

    private boolean isSameDay(Date date1, Date date2) {
        // Simple date comparison for day, month, and year
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy");
        return sdf.format(date1).equals(sdf.format(date2));
    }

    private void deleteSchedule(String userId, String scheduleId) {
        db.collection("Tenants")
                .document(userId)
                .collection("Schedules")
                .document(scheduleId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("FirestoreDelete", "Schedule deleted successfully"))
                .addOnFailureListener(e -> Log.e("FirestoreDelete", "Error deleting schedule", e));
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
