package com.example.rentalms;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProperty extends AppCompatActivity {

    private EditText barangayEditText, addressEditText, cityEditText, priceEditText;

    private TextView propertyNameTextView;
    private Spinner paymentPeriodSpinner;
    private Button updatePropertyButton;
    private String landlordId;
    private FirebaseFirestore db;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_property); // Correct the layout

        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        propertyNameTextView = findViewById(R.id.propertyName);
        barangayEditText = findViewById(R.id.barangay);
        addressEditText = findViewById(R.id.address);
        cityEditText = findViewById(R.id.city);
        priceEditText = findViewById(R.id.price);
        paymentPeriodSpinner = findViewById(R.id.paymentPeriodSpinner);
        updatePropertyButton = findViewById(R.id.btn_update_property);

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

        // Set listener to update property in Firestore
        updatePropertyButton.setOnClickListener(v -> updatePropertyInFirestore());
    }

    private void updatePropertyInFirestore() {
        String propertyName = propertyNameTextView.getText().toString().trim();
        String barangay = barangayEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();
        String price = priceEditText.getText().toString().trim();
        String paymentPeriod = paymentPeriodSpinner.getSelectedItem().toString().trim(); // Get selected payment period

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
            // Query Firestore to find the property by its details
            db.collection("Landlords").document(landlordId).collection("properties")
                    .whereEqualTo("propertyName", propertyName)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                            // Assuming the first result is the correct property
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
}
