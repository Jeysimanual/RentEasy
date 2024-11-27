// InquireOverlayFragment.java

package com.project.rentalms;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class InquireOverlayFragment extends Fragment {

    private Button messageButton, scheduleVisitButton, cancelButton;
    private String propertyName;
    private String type;
    private String city;
    private String province;
    private String price;
    private String userId;
    private FirebaseFirestore db;
    private Bundle userCredentialsBundle;

    private TextView inquireTitle, inquireSubtitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inquire_overlay, container, false);


        inquireTitle = view.findViewById(R.id.inquireTitle);
        inquireTitle.setText("You are inquiring " + propertyName);



        messageButton = view.findViewById(R.id.messageButton);
        scheduleVisitButton = view.findViewById(R.id.scheduleVisitButton);
        cancelButton = view.findViewById(R.id.cancelButton);


        if (getArguments() != null) {
            propertyName = getArguments().getString("propertyName", "Property");
            type = getArguments().getString("type", "Type");
            city = getArguments().getString("city", "City");
            province = getArguments().getString("province", "Province");
            price = getArguments().getString("price", "Price");
            userId = getArguments().getString("userId", "UserId");

        }

        getUserCreds(userId);

        inquireTitle = view.findViewById(R.id.inquireTitle);
        inquireTitle.setText("You are inquiring " + propertyName);

        inquireSubtitle = view.findViewById(R.id.inquireSubtitle);
        inquireSubtitle.setText("A " + type + " in " + city + ", " + province);



        messageButton.setOnClickListener(v -> {
            MessageOverlayFragment messageOverlayFragment = new MessageOverlayFragment();
            messageOverlayFragment.setArguments(userCredentialsBundle);
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_up, R.anim.fade_out);
            transaction.replace(R.id.inquire_section_container, messageOverlayFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        scheduleVisitButton.setOnClickListener(v -> {
            // Navigate to SetScheduleActivity
            Intent intent = new Intent(requireContext(), SetSchedule.class);

            // Pass property details to the SetSchedule activity
            intent.putExtra("propertyName", propertyName);
            intent.putExtra("barangay", getArguments().getString("barangay", "Barangay"));
            intent.putExtra("address", getArguments().getString("address", "Address"));
            intent.putExtra("city", city);

            // Pass landlordId and propertyId
            intent.putExtra("landlordId", getArguments().getString("landlordId", "landlordDocumentId"));
            intent.putExtra("propertyId", getArguments().getString("propertyId", "propertyDocumentId"));

            startActivity(intent);
        });





        cancelButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }

    private void getUserCreds(String userID) {
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Tenants").document(userID);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Retrieve user data and store it in a Bundle
                    userCredentialsBundle = new Bundle();
                    userCredentialsBundle.putString("firstName", document.getString("firstName"));
                    userCredentialsBundle.putString("lastName", document.getString("lastName"));
                    userCredentialsBundle.putString("mobile", document.getString("mobile"));
                    userCredentialsBundle.putString("email", document.getString("email"));

                    Log.d("Credentials", "User data stored in Bundle for later use");
                } else {
                    Log.d("Credentials", "No such document");
                }
            } else {
                Log.d("Credentials", "Failed to get document", task.getException());
            }
        });
    }
}
