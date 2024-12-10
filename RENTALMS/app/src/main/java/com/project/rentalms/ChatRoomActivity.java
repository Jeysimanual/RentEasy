package com.project.rentalms;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRoomActivity extends AppCompatActivity {

    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ChatMessageAdapter messageAdapter;
    private List<ChatMessage> messageList;
    private DatabaseReference messagesRef;
    private String chatRoomId;
    private String tenantId;
    private String senderId;

    private TextView propertyNameTextView; // TextView to display property name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        // Initialize views
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        propertyNameTextView = findViewById(R.id.propertyName); // Find the TextView for property name
        // Back button click listener

        // Retrieve chatRoomId and tenantId from Intent
        chatRoomId = getIntent().getStringExtra("chatRoomId");
        tenantId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Assuming current user is the tenant
        senderId = tenantId;

        Log.d("ChatRoomActivity", "ChatRoomId: " + chatRoomId);
        Log.d("ChatRoomActivity", "TenantId: " + tenantId);

        // Set up RecyclerView
        messageList = new ArrayList<>();
        messageAdapter = new ChatMessageAdapter(this, messageList, tenantId);

        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);

        // Set up Firebase Database reference
        messagesRef = FirebaseDatabase.getInstance().getReference("Messages").child(chatRoomId);

        // Load messages
        loadMessages();

        // Load property name
        loadPropertyName();

        // Handle send button click
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    ChatMessage message = messageSnapshot.getValue(ChatMessage.class);
                    if (message != null) {
                        messageList.add(message);
                    }
                }
                messageAdapter.notifyDataSetChanged();
                messagesRecyclerView.scrollToPosition(messageList.size() - 1); // Scroll to the latest message
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatRoomActivity.this, "Failed to load messages.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPropertyName() {
        // Extract landlordId and propertyId from chatRoomId
        String landlordId = chatRoomId.split("_")[0]; // Assuming the first part is landlordId
        String propertyId = chatRoomId.split("_")[1]; // Assuming the second part is propertyId

        // Reference to the landlord's properties in Firebase
        DatabaseReference propertyRef = FirebaseDatabase.getInstance()
                .getReference("Landlords")
                .child(landlordId)
                .child("properties")
                .child(propertyId);

        propertyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("propertyName")) {
                    String propertyName = snapshot.child("propertyName").getValue(String.class);
                    propertyNameTextView.setText(propertyName);
                } else {
                    propertyNameTextView.setText("Property Name Not Found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatRoomActivity.this, "Failed to load property name.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (messageText.isEmpty()) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String messageId = messagesRef.push().getKey();
        if (messageId == null) {
            Toast.makeText(this, "Failed to send message.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("messageText", messageText);
        messageData.put("senderId", senderId);
        messageData.put("receiverId", chatRoomId.split("_")[0]); // Assuming landlordId is the first part of chatRoomId
        messageData.put("timestamp", System.currentTimeMillis());

        messagesRef.child(messageId).setValue(messageData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                messageInput.setText(""); // Clear input field
            } else {
                Toast.makeText(this, "Failed to send message.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
