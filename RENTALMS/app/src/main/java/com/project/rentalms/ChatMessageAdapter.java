package com.project.rentalms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ChatMessageViewHolder> {

    private Context context;
    private List<ChatMessage> messageList;
    private String currentUserId;

    public ChatMessageAdapter(Context context, List<ChatMessage> messageList, String currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ChatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the chat message item layout
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
        return new ChatMessageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessageViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);

        // Hide both layouts initially
        holder.leftMessageLayout.setVisibility(View.GONE);
        holder.rightMessageLayout.setVisibility(View.GONE);

        if (message.getSenderId().equals(currentUserId)) {
            // Sent message (align right)
            holder.rightMessageLayout.setVisibility(View.VISIBLE);
            holder.rightMessageTextView.setText(message.getMessageText());
            holder.setTimestamp(message.getTimestamp());

            // Load sender's profile picture
            fetchProfileImage("Tenants", message.getSenderId(), url -> {
                Glide.with(context)
                        .load(url)
                        .placeholder(R.drawable.default_image)
                        .into(holder.rightProfilePicture);
            });

        } else {
            // Received message (align left)
            holder.leftMessageLayout.setVisibility(View.VISIBLE);
            holder.leftMessageTextView.setText(message.getMessageText());
            holder.setTimestamp(message.getTimestamp());

            // Load receiver's profile picture
            fetchProfileImage("Landlords", message.getReceiverId(), url -> {
                Glide.with(context)
                        .load(url)
                        .placeholder(R.drawable.default_image)
                        .into(holder.leftProfilePicture);
            });
        }
    }


    private void fetchProfileImage(String collection, String userId, OnImageFetchedListener listener) {
        FirebaseFirestore.getInstance().collection(collection).document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("profileImageUrl")) {
                        String imageUrl = documentSnapshot.getString("profileImageUrl");
                        listener.onImageFetched(imageUrl);
                    } else {
                        listener.onImageFetched(null);
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onImageFetched(null);
                });
    }

    public interface OnImageFetchedListener {
        void onImageFetched(String url);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // ViewHolder to bind message data to the view
    public static class ChatMessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leftMessageLayout, rightMessageLayout;
        TextView leftMessageTextView, rightMessageTextView;
        TextView leftTimestampTextView, rightTimestampTextView;
        ImageView leftProfilePicture, rightProfilePicture;

        public ChatMessageViewHolder(View itemView) {
            super(itemView);
            leftMessageLayout = itemView.findViewById(R.id.leftMessageLayout);
            rightMessageLayout = itemView.findViewById(R.id.rightMessageLayout);
            leftMessageTextView = itemView.findViewById(R.id.leftMessageText);
            rightMessageTextView = itemView.findViewById(R.id.rightMessageText);
            leftTimestampTextView = itemView.findViewById(R.id.leftTimestamp);
            rightTimestampTextView = itemView.findViewById(R.id.rightTimestamp);
            leftProfilePicture = itemView.findViewById(R.id.leftProfilePicture);
            rightProfilePicture = itemView.findViewById(R.id.rightProfilePicture);

        }

        public void setTimestamp(long timestamp) {
            String formattedTimestamp = formatTimestamp(timestamp);
            // Set timestamp in the appropriate layout (left or right)
            if (leftTimestampTextView.getVisibility() == View.VISIBLE) {
                leftTimestampTextView.setText(formattedTimestamp);
            } else {
                rightTimestampTextView.setText(formattedTimestamp);
            }
        }

        // Helper method to format timestamp into a human-readable format
        private String formatTimestamp(long timestamp) {
            java.util.Date date = new java.util.Date(timestamp);
            java.text.DateFormat format = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault());
            return format.format(date);
        }
    }
}
