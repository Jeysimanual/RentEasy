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
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;

    import java.util.ArrayList;
    import java.util.List;

    public class LandlordChat extends AppCompatActivity {

        String landlordId;
        FirebaseAuth mAuth;
        private RecyclerView LandlordChatRecylerView;
        private LandlordChatRoomAdapter adapter;
        private List<ChatRoom> chatRoomList;
        private DatabaseReference messagesRef;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_landlord_chat);  // Assuming this is your layout for the chat page

            mAuth = FirebaseAuth.getInstance();

            // Check if the user is logged in
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "User not logged in. Redirecting to login page.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, Login.class)); // Redirect to your login activity
                finish();
                return;
            }

            landlordId = mAuth.getCurrentUser().getUid();

            LandlordChatRecylerView = findViewById(R.id.LandlordchatRecyclerView);
            LandlordChatRecylerView.setLayoutManager(new LinearLayoutManager(this));

            chatRoomList = new ArrayList<>();
            adapter = new LandlordChatRoomAdapter(this, chatRoomList);
            LandlordChatRecylerView.setAdapter(adapter);

            messagesRef = FirebaseDatabase.getInstance().getReference("Messages");

            fetchChatRooms(landlordId);



            // Initialize BottomNavigationView and set the selected item to "Chat"
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
            bottomNavigationView.setSelectedItemId(R.id.btn_chat);  // Set the selected item to "Chat"

            // Set up item selected listener for navigation
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();


                if (itemId == R.id.btn_home) {
                    startActivity(new Intent(getApplicationContext(), LandlordPage.class));
                    overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);  // Animation for navigation
                    finish();
                    return true;


                } else if (itemId == R.id.btn_checklist) {
                    startActivity(new Intent(getApplicationContext(), LandlordChecklist.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;


                } else if (itemId == R.id.btn_chat) {
                    return true;


                } else if (itemId == R.id.btn_more) {
                    startActivity(new Intent(getApplicationContext(), LandlordMore.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                }

                return false;
            });
        }

        private void fetchChatRooms(String ChatLandlordId) {
            messagesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    chatRoomList.clear();
                    for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                        String chatRoomId = chatSnapshot.getKey(); // Get the key
                        Log.d("TenantChat", "Checking chatRoomId: " + chatRoomId);

                        if (chatRoomId != null && chatRoomId.contains(ChatLandlordId + "_")) {
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
                        LandlordChatRecylerView.setVisibility(View.GONE); // Hide RecyclerView
                    } else {
                        findViewById(R.id.noChatTextView).setVisibility(View.GONE); // Hide "No messages" text
                        LandlordChatRecylerView.setVisibility(View.VISIBLE); // Show RecyclerView
                    }

                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(LandlordChat.this, "Failed to load chat rooms.", Toast.LENGTH_SHORT).show();
                }
            });
        }


    }