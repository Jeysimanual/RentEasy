package com.example.rentalms;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class LandlordProfile extends AppCompatActivity {

    private static final int REQUEST_CODE_SELECT_IMAGE = 100;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 200;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;

    private TextView firstNameTextView, lastNameTextView, emailTextView, mobileTextView, accountTypeTextView;
    private TextView welcomeTextView;
    private TextView backButton;
    private ImageView profilePicture;
    private Button btnChangePicture;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landlord_profile);

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        // Initialize UI components
        firstNameTextView = findViewById(R.id.Lfirstname);
        lastNameTextView = findViewById(R.id.Llastname);
        emailTextView = findViewById(R.id.LEmail);
        mobileTextView = findViewById(R.id.LMobile);
        accountTypeTextView = findViewById(R.id.Laccounttype);
        welcomeTextView = findViewById(R.id.welcome_text);
        backButton = findViewById(R.id.backbtn);
        profilePicture = findViewById(R.id.profile_picture);
        btnChangePicture = findViewById(R.id.btn_change_picture);

        // Back button click listener
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(LandlordProfile.this, LandlordMore.class);
            startActivity(intent);
            finish();
        });

        // Change picture button click listener
        btnChangePicture.setOnClickListener(v -> {
            if (hasStoragePermission()) {
                openGallery();
            } else {
                requestStoragePermission();
            }
        });

        // Fetch landlord data from Firestore
        fetchLandlordData();
    }

    // Check if storage permission is granted
    private boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    // Request storage permission
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_CODE_STORAGE_PERMISSION);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
        }
    }

    // Open the gallery to select an image
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
    }

    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permission denied to access gallery", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Handle the result of the image selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                uploadImageToFirebase(selectedImageUri);
            }
        }
    }

    // Upload image to Firebase Storage and save URL to Firestore
    private void uploadImageToFirebase(Uri imageUri) {
        String userId = auth.getCurrentUser().getUid();
        if (userId != null) {
            StorageReference storageRef = storage.getReference().child("profile_images/" + UUID.randomUUID().toString());
            storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    db.collection("Landlords").document(userId)
                            .update("profileImageUrl", imageUrl)
                            .addOnSuccessListener(aVoid -> {
                                Picasso.get().load(imageUrl).into(profilePicture);
                                Toast.makeText(LandlordProfile.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(LandlordProfile.this, "Failed to save image URL", Toast.LENGTH_SHORT).show());
                });
            }).addOnFailureListener(e -> Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show());
        }
    }

    private void fetchLandlordData() {
        String userId = auth.getCurrentUser().getUid();
        if (userId != null) {
            db.collection("Landlords").document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String firstName = document.getString("firstName");
                                String lastName = document.getString("lastName");
                                String email = document.getString("email");
                                String mobile = document.getString("mobile");
                                String accountType = document.getString("accountType");
                                String imageUrl = document.getString("profileImageUrl");

                                firstNameTextView.setText(firstName != null ? firstName : "N/A");
                                lastNameTextView.setText(lastName != null ? lastName : "N/A");
                                welcomeTextView.setText("Welcome, " + firstName + " " + lastName);
                                emailTextView.setText(email != null ? email : "N/A");
                                mobileTextView.setText(mobile != null ? mobile : "N/A");
                                accountTypeTextView.setText(accountType != null ? accountType : "N/A");

                                if (imageUrl != null && !imageUrl.isEmpty()) {
                                    Picasso.get().load(imageUrl).into(profilePicture);
                                }
                            } else {
                                Toast.makeText(LandlordProfile.this, "No such landlord profile found!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LandlordProfile.this, "Failed to fetch profile data", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(LandlordProfile.this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }
}
