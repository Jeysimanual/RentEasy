package com.project.rentalms;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

    private Context context;
    private List<ChatRoom> chatRoomList;


    public ChatRoomAdapter(Context context, List<ChatRoom> chatRoomList) {
        this.context = context;
        this.chatRoomList = chatRoomList;
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_room_item, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        ChatRoom chatRoom = chatRoomList.get(position);

        // Check if chatRoomId is null or empty
        String chatRoomId = chatRoom.getChatRoomId();
        if (chatRoomId == null || chatRoomId.isEmpty()) {
            holder.landlordName.setText("Invalid Chat Room ID");
            holder.lastMessage.setText("No messages available");
            holder.messageIcon.setVisibility(View.GONE); // Hide the icon by default
            return;
        }

        // Initialize landlordId and propertyId
        final String landlordId;
        String propertyId = null;

        // Split chatRoomId to extract landlordId and propertyId
        String[] ids = chatRoomId.split("_");
        if (ids.length == 3) {
            landlordId = ids[0]; // Extracted landlordId
            propertyId = ids[2]; // Extracted propertyId

            // Fetch propertyName from Firestore
            FirebaseFirestore.getInstance()
                    .collection("Landlords")
                    .document(landlordId) // Query by landlord ID
                    .collection("properties")
                    .document(propertyId) // Query for specific property by propertyId
                    .get()
                    .addOnSuccessListener(propertySnapshot -> {
                        if (propertySnapshot.exists()) {
                            String propertyName = propertySnapshot.getString("propertyName");
                            holder.landlordName.setText(propertyName != null ? propertyName : "Unknown Property");
                        } else {
                            holder.landlordName.setText("Property Not Found");
                        }
                    })
                    .addOnFailureListener(e -> holder.landlordName.setText("Error Loading Property"));
        } else {
            holder.landlordName.setText("Invalid Chat Room Format");
            return; // Exit early if the format is invalid
        }

        // Fetch the last message from Firebase Realtime Database
        FirebaseDatabase.getInstance().getReference()
                .child("Messages")
                .child(chatRoomId)  // Use chatRoomId to locate the chat room
                .orderByKey()  // Order messages by key (latest first)
                .limitToLast(1)  // Fetch only the last message
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                                String lastMessage = messageSnapshot.child("messageText").getValue(String.class);
                                String senderId = messageSnapshot.child("senderId").getValue(String.class);
                                Log.e("ChatRoomAdapter", "SenderId " + senderId);
                                // Set the last message text
                                holder.lastMessage.setText(lastMessage != null ? lastMessage : "No Message");
                                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                String userId = mAuth.getCurrentUser().getUid();
                                Log.e("ChatRoomAdapter", "UserId " + userId);
                                // Check if the sender is a landlord
                                if (userId != null && senderId != null && !userId.equals(senderId)) {
                                    Log.e("ChatRoomAdapter", "Sender is a landlord");
                                    holder.landlordName.setTypeface(null, Typeface.BOLD);
                                    holder.lastMessage.setTypeface(null, Typeface.BOLD);
                                    holder.messageIcon.setVisibility(View.VISIBLE);
                                } else {
                                    Log.e("ChatRoomAdapter", "Sender is the tenant or invalid senderId");
                                    holder.messageIcon.setVisibility(View.GONE);
                                }

                            }
                        } else {
                            holder.lastMessage.setText("No Messages");
                            holder.messageIcon.setVisibility(View.GONE); // Hide the icon if there are no messages
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("ChatRoomAdapter", "Error fetching last message", databaseError.toException());
                        holder.messageIcon.setVisibility(View.GONE); // Hide the icon in case of an error
                    }
                });

        // Handle item click (if needed)
        holder.itemView.setOnClickListener(v -> {
             Intent intent = new Intent(context, ChatRoomActivity.class);
             intent.putExtra("chatRoomId", chatRoomId);
             context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return chatRoomList.size();
    }

    static class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        TextView landlordName, lastMessage;
        ImageView messageIcon;


        public ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            landlordName = itemView.findViewById(R.id.landlordName);
            lastMessage = itemView.findViewById(R.id.timestamp);
            messageIcon = itemView.findViewById(R.id.messageIcon);

        }
    }
}

