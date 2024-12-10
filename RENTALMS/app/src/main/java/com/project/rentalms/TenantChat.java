package com.project.rentalms;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TenantChat extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private ChatRoomAdapter adapter;
    private List<ChatRoom> chatRoomList;
    private DatabaseReference messagesRef;
    private String tenantId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_chat);  // Assuming this is your layout for the chat page
        String tenantId = getIntent().getStringExtra("tenantId");
        Log.e("TenantChat", "Tenant ID: " + tenantId);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        chatRoomList = new ArrayList<>();
        adapter = new ChatRoomAdapter(this, chatRoomList);
        chatRecyclerView.setAdapter(adapter);

        messagesRef = FirebaseDatabase.getInstance().getReference("Messages");

        fetchChatRooms(tenantId);

        // Initialize BottomNavigationView and set the selected item to "Chat"
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_chat);  // Set the selected item to "Chat"

        // Set up item selected listener for navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Handle the search button click (navigate to TenantPage)
            if (itemId == R.id.bottom_search) {
                Intent intent = new Intent(getApplicationContext(), TenantPage.class);
                intent.putExtra("tenantId", tenantId);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);  // Animation for navigation
                finish();
                return true;

                // Handle the favorite button click (navigate to TenantFavorite)
            } else if (itemId == R.id.bottom_favorite) {
                Intent intent = new Intent(getApplicationContext(), TenantFavorite.class);
                intent.putExtra("tenantId", tenantId);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;

                // Handle the chat button click (stay on the same page)
            } else if (itemId == R.id.bottom_chat) {
                return true;  // Stay on the current activity

                // Handle the more button click (navigate to TenantMore)
            } else if (itemId == R.id.bottom_more) {
                Intent intent = new Intent(getApplicationContext(), TenantMore.class);
                intent.putExtra("tenantId", tenantId);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }

            return false;
        });
    }

    private void fetchChatRooms(String ChatTenantId) {
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                chatRoomList.clear();
                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    String chatRoomId = chatSnapshot.getKey(); // Get the key
                    Log.d("TenantChat", "Checking chatRoomId: " + chatRoomId);

                    if (chatRoomId != null && chatRoomId.contains("_" + ChatTenantId + "_")) {
                        ChatRoom chatRoom = chatSnapshot.getValue(ChatRoom.class);
                        if (chatRoom != null) {
                            chatRoom.setChatRoomId(chatRoomId); // Set the ID
                            chatRoomList.add(chatRoom);
                        } else {
                            Log.e("TenantChat", "Failed to parse chat room for ID: " + chatRoomId);
                        }
                    }
                }

                if (chatRoomList.isEmpty()) {
                    findViewById(R.id.noChatTextView).setVisibility(View.VISIBLE); // Show "No messages" text
                    chatRecyclerView.setVisibility(View.GONE); // Hide RecyclerView
                } else {
                    findViewById(R.id.noChatTextView).setVisibility(View.GONE); // Hide "No messages" text
                    chatRecyclerView.setVisibility(View.VISIBLE); // Show RecyclerView
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(TenantChat.this, "Failed to load chat rooms.", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
