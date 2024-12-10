package com.project.rentalms;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class MessageOverlayFragment extends Fragment {

    private EditText messageEditText;
    private Button sendButton;
    private TextView nameTextView;
    private String firstName;
    private String lastName;
    private String landlordId;
    private String userId;
    private String propertyId;

    private FirebaseDatabase database;
    private DatabaseReference messagesRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_overlay, container, false);

        messageEditText = view.findViewById(R.id.messageEditText);
        sendButton = view.findViewById(R.id.sendButton);
        nameTextView = view.findViewById(R.id.nameTextView);

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance();
        messagesRef = database.getReference("Messages");

        // Retrieve the Bundle arguments
        if(getArguments() != null) {
            firstName = getArguments().getString("firstName", "First Name");
            lastName = getArguments().getString("lastName", "Last Name");
            landlordId = getArguments().getString("landlordId", "LandlordId");
            userId = getArguments().getString("userId", "UserId");
            propertyId = getArguments().getString("propertyId", "PropertyId");
            Log.e("MessageOverlayFragment","propertyId : " + propertyId);

            Log.e("firstName", firstName);
            Log.e("lastName", lastName);
        }

        nameTextView.setText("Your Name: " + firstName + " " + lastName);

        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString().trim();
            if (!message.isEmpty()) {
                // Handle message sending logic here
                sendMessage(message);
            } else {
                Toast.makeText(getContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void sendMessage(String messageText) {
        // Create a new message entry

        String messageId = messagesRef.push().getKey(); // Generate a unique message ID
        String chatRoomId = landlordId + "_" + userId + "_" + propertyId; // Chat room ID based on landlord and tenant IDs

        // Create message data
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", userId);
        messageData.put("receiverId", landlordId);
        messageData.put("messageText", messageText);
        messageData.put("timestamp", ServerValue.TIMESTAMP);

        // Save message to the chat room
        if (messageId != null) {
            messagesRef.child(chatRoomId).child(messageId).setValue(messageData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Message Sent", Toast.LENGTH_SHORT).show();
                            requireActivity().getSupportFragmentManager().popBackStack(); // Close the fragment after sending
                        } else {
                            Toast.makeText(getContext(), "Error sending message", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
