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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class LandlordChatRoomAdapter extends RecyclerView.Adapter<LandlordChatRoomAdapter.LandlordChatRoomViewHolder> {

    private Context context;
    private List<ChatRoom> chatRoomList;

    public LandlordChatRoomAdapter(Context context, List<ChatRoom> chatRoomList) {
        this.context = context;
        this.chatRoomList = chatRoomList;
    }

    @NonNull
    @Override
    public LandlordChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_room_item, parent, false);
        return new LandlordChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LandlordChatRoomViewHolder holder, int position) {
        ChatRoom chatRoom = chatRoomList.get(position);

        String chatRoomId = chatRoom.getChatRoomId();
        if (chatRoomId == null || chatRoomId.isEmpty()) {
            holder.tenantName.setText("Invalid Chat Room ID");
            holder.lastMessage.setText("No messages available");
            holder.messageIcon.setVisibility(View.GONE);
            return;
        }

        String[] ids = chatRoomId.split("_");
        if (ids.length == 3) {
            String tenantId = ids[1]; // Tenant ID
            String propertyId = ids[2]; // Property ID

            // Fetch tenant's name from Firestore
            FirebaseFirestore.getInstance()
                    .collection("Tenants")
                    .document(tenantId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String firstName = documentSnapshot.getString("firstName");
                            String lastName = documentSnapshot.getString("lastName");

                            if (firstName != null && lastName != null) {
                                holder.tenantName.setText(firstName + " " + lastName);
                            } else if (firstName != null) {
                                holder.tenantName.setText(firstName);
                            } else if (lastName != null) {
                                holder.tenantName.setText(lastName);
                            } else {
                                holder.tenantName.setText("Unknown Tenant");
                            }
                        } else {
                            holder.tenantName.setText("Tenant Not Found");
                        }
                    })
                    .addOnFailureListener(e -> holder.tenantName.setText("Error Loading Tenant"));


            // Fetch last message from Firebase Realtime Database
            FirebaseDatabase.getInstance().getReference()
                    .child("Messages")
                    .child(chatRoomId)
                    .orderByKey()
                    .limitToLast(1)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                                    String lastMessage = messageSnapshot.child("messageText").getValue(String.class);
                                    String senderId = messageSnapshot.child("senderId").getValue(String.class);

                                    holder.lastMessage.setText(lastMessage != null ? lastMessage : "No Message");
                                    if (tenantId.equals(senderId)) {
                                        holder.tenantName.setTypeface(null, Typeface.BOLD);
                                        holder.lastMessage.setTypeface(null, Typeface.BOLD);
                                        holder.messageIcon.setVisibility(View.VISIBLE);
                                    } else {
                                        holder.messageIcon.setVisibility(View.GONE);
                                    }
                                }
                            } else {
                                holder.lastMessage.setText("No Messages");
                                holder.messageIcon.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("LandlordChatRoomAdapter", "Error fetching last message", databaseError.toException());
                            holder.messageIcon.setVisibility(View.GONE);
                        }
                    });
        } else {
            holder.tenantName.setText("Invalid Chat Room Format");
            return;
        }

        // Set onClick listener to open ChatRoomActivity
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

    static class LandlordChatRoomViewHolder extends RecyclerView.ViewHolder {
        TextView tenantName, lastMessage;
        ImageView messageIcon;

        public LandlordChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tenantName = itemView.findViewById(R.id.landlordName); // Reuse landlordName TextView for tenant's name
            lastMessage = itemView.findViewById(R.id.timestamp);
            messageIcon = itemView.findViewById(R.id.messageIcon);
        }
    }
}
