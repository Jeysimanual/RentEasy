package com.project.rentalms;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditProperty extends AppCompatActivity {

    // UI components
    private EditText barangayEditText, addressEditText, cityEditText, priceEditText;
    private TextView propertyNameTextView;
    private Spinner paymentPeriodSpinner;
    private Button updatePropertyButton;
    private String landlordId;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private ImageView roomInteriorImageView, roomExteriorImageView;
    private Button uploadRoomInteriorButton, uploadRoomExteriorButton;
    private Uri roomInteriorUri, roomExteriorUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_property);

        // Initialize Firestore and Firebase Storage
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Initialize UI components
        propertyNameTextView = findViewById(R.id.propertyName);
        barangayEditText = findViewById(R.id.barangay);
        addressEditText = findViewById(R.id.address);
        cityEditText = findViewById(R.id.city);
        priceEditText = findViewById(R.id.price);
        paymentPeriodSpinner = findViewById(R.id.paymentPeriodSpinner);
        updatePropertyButton = findViewById(R.id.btn_update_property);

        roomInteriorImageView = findViewById(R.id.img_room_interior);
        roomExteriorImageView = findViewById(R.id.img_bathroom_shower);
        uploadRoomInteriorButton = findViewById(R.id.btn_upload_room_interior);
        uploadRoomExteriorButton = findViewById(R.id.btn_upload_bathroom_shower);

        // Set up the Spinner with options (Monthly and Yearly)
        String[] paymentPeriods = {"Monthly", "Yearly"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paymentPeriods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentPeriodSpinner.setAdapter(adapter);

        // Get data passed from PropertyAdapter
        landlordId = getIntent().getStringExtra("landlordId");

        // Pre-fill the form with property data
        propertyNameTextView.setText(getIntent().getStringExtra("propertyName"));
        barangayEditText.setText(getIntent().getStringExtra("barangay"));
        addressEditText.setText(getIntent().getStringExtra("address"));
        cityEditText.setText(getIntent().getStringExtra("city"));
        priceEditText.setText(getIntent().getStringExtra("price"));

        // Pre-select payment period in spinner
        String paymentPeriod = getIntent().getStringExtra("paymentPeriod");
        if (paymentPeriod != null) {
            int position = adapter.getPosition(paymentPeriod);
            paymentPeriodSpinner.setSelection(position);
        }

        // Fetch the image URLs and load into ImageViews
        String interiorImageUrl = getIntent().getStringExtra("interiorImageUrl");
        String exteriorImageUrl = getIntent().getStringExtra("exteriorImageUrl");

        if (interiorImageUrl != null) {
            Glide.with(this)
                    .load(interiorImageUrl)
                    .into(roomInteriorImageView);
        }

        if (exteriorImageUrl != null) {
            Glide.with(this)
                    .load(exteriorImageUrl)
                    .into(roomExteriorImageView);
        }

        // Set listener to update property in Firestore
        updatePropertyButton.setOnClickListener(v -> updatePropertyInFirestore());

        // Set listener to upload room interior image
        uploadRoomInteriorButton.setOnClickListener(v -> selectImage(1));

        // Set listener to upload room exterior image
        uploadRoomExteriorButton.setOnClickListener(v -> selectImage(2));
    }

    private void selectImage(int imageType) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, imageType);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();

            if (requestCode == 1) {
                roomInteriorUri = selectedImageUri;
                roomInteriorImageView.setImageURI(roomInteriorUri); // Display the selected image
            } else if (requestCode == 2) {
                roomExteriorUri = selectedImageUri;
                roomExteriorImageView.setImageURI(roomExteriorUri); // Display the selected image
            }
        }
    }

    private void updatePropertyInFirestore() {
        String propertyName = propertyNameTextView.getText().toString().trim();
        String barangay = barangayEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();
        String price = priceEditText.getText().toString().trim();
        String paymentPeriod = paymentPeriodSpinner.getSelectedItem().toString().trim();

        if (propertyName.isEmpty() || barangay.isEmpty() || address.isEmpty() || city.isEmpty() || price.isEmpty() || paymentPeriod.isEmpty()) {
            Toast.makeText(EditProperty.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate if the price has the peso sign (₱)
        if (!price.startsWith("₱")) {
            Toast.makeText(EditProperty.this, "Please add the Peso sign (₱) to the price", Toast.LENGTH_SHORT).show();
            return;
        }

        if (landlordId != null) {
            // Handle image upload if a new image is selected
            if (roomInteriorUri != null) {
                uploadImageToFirebase(roomInteriorUri, "interiorImageUrl", roomInteriorImageView);
            }
            if (roomExteriorUri != null) {
                uploadImageToFirebase(roomExteriorUri, "exteriorImageUrl", roomExteriorImageView);
            }

            // Update property details in Firestore
            db.collection("Landlords").document(landlordId).collection("properties")
                    .whereEqualTo("propertyName", propertyName)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                            task.getResult().getDocuments().get(0).getReference()
                                    .update("propertyName", propertyName,
                                            "barangay", barangay,
                                            "address", address,
                                            "city", city,
                                            "price", price,
                                            "paymentPeriod", paymentPeriod)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(EditProperty.this, "Property updated successfully", Toast.LENGTH_SHORT).show();
                                        finish(); // Close the activity
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(EditProperty.this, "Error updating property", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(EditProperty.this, "Property not found", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void uploadImageToFirebase(Uri imageUri, String imageField, ImageView imageView) {
        // Create a storage reference to upload the image
        StorageReference imageRef = storageReference.child("property_images/" + System.currentTimeMillis() + ".jpg");

        // Upload the image
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            // After uploading, get the download URL and update Firestore
                            db.collection("Landlords").document(landlordId).collection("properties")
                                    .document(propertyNameTextView.getText().toString())
                                    .update(imageField, uri.toString())
                                    .addOnSuccessListener(aVoid -> {
                                        // Update the ImageView with the new image URL
                                        Glide.with(EditProperty.this).load(uri).into(imageView);
                                    });
                        }))
                .addOnFailureListener(e -> Toast.makeText(EditProperty.this, "Image upload failed", Toast.LENGTH_SHORT).show());
    }
}
