package com.example.rentalms;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatActivity {

    private EditText oldPassword, newPassword, retypeNewPassword;

    private TextView backButton;
    private Button updatePasswordButton;
    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        oldPassword = findViewById(R.id.newPass); // Old password field
        newPassword = findViewById(R.id.oldPass); // New password field
        retypeNewPassword = findViewById(R.id.retypePass); // Retype new password field
        updatePasswordButton = findViewById(R.id.update_password_button); // Update button ID

        backButton = findViewById(R.id.backbtn); // Initialize the back button TextView

        // Set click listener for the back button
        backButton.setOnClickListener(v -> {
            // Start TenantMore activity
            Intent intent = new Intent(ChangePassword.this, TenantMore.class);
            startActivity(intent);
            finish(); // Optional: Finish current activity if you don't want to return to it
        });

        updatePasswordButton.setOnClickListener(view -> updatePassword());
    }

    private void updatePassword() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String oldPass = oldPassword.getText().toString().trim();
        String newPass = newPassword.getText().toString().trim();
        String retypePass = retypeNewPassword.getText().toString().trim();

        // Check if new passwords match
        if (!newPass.equals(retypePass)) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Re-authenticate the user with old password
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPass);
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Update password
                user.updatePassword(newPass).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(ChangePassword.this, "Password updated successfully", Toast.LENGTH_SHORT).show();

                        // Logout the user
                        mAuth.signOut();
                        // Redirect to login screen
                        Intent intent = new Intent(ChangePassword.this, Login.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish(); // Close the current activity
                    } else {
                        Toast.makeText(ChangePassword.this, "Password update failed", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(ChangePassword.this, "Authentication failed. Check your old password.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
