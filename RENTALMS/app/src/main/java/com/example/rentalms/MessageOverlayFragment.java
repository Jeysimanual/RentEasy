package com.example.rentalms;

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

public class MessageOverlayFragment extends Fragment {

    private EditText messageEditText;
    private Button sendButton;
    private TextView nameTextView;
    private String firstName;
    private String lastName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_overlay, container, false);

        messageEditText = view.findViewById(R.id.messageEditText);
        sendButton = view.findViewById(R.id.sendButton);


        // Retrieve the Bundle arguments
        if(getArguments() != null) {
            firstName = getArguments().getString("firstName", "First Name");
            lastName = getArguments().getString("lastName", "Last Name");
            Log.e("firstName", firstName);
            Log.e("lastName", lastName);
        }
        nameTextView = view.findViewById(R.id.nameTextView);

        nameTextView.setText("Your Name : " + firstName + " " + lastName);

        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString().trim();
            if (!message.isEmpty()) {
                // Handle message sending logic here
                Toast.makeText(getContext(), "Message Sent: " + message, Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack(); // Close the fragment after sending
            } else {
                Toast.makeText(getContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

}
