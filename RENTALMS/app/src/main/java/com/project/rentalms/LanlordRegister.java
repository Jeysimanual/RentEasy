package com.project.rentalms;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class LanlordRegister extends AppCompatActivity {

    // UI components
    EditText landlordFirstName, landlordLastName, landlordusername, landlordMobile, landlordEmail, landlordPassword, landlordRetypePassword;
    TextView landlordlogin;
    Button createAccountButton;

    // Firebase components
    FirebaseAuth mAuth;
    FirebaseFirestore landlordDatabase;

    // Lock and Unlock ImageViews for password toggle
    ImageView lockIconPassword, unlockIconPassword, lockIconRetype, unlockIconRetype;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lanlord_register);

        // Initialize Firebase Authentication and Firestore
        mAuth = FirebaseAuth.getInstance();
        landlordDatabase = FirebaseFirestore.getInstance();

        // Initialize UI components
        landlordFirstName = findViewById(R.id.Landlordfirstname);
        landlordLastName = findViewById(R.id.Landlordlastname);
        landlordusername = findViewById(R.id.Landlordusername);
        landlordMobile = findViewById(R.id.LandlordMobile);
        landlordEmail = findViewById(R.id.LandlordEmail);
        landlordPassword = findViewById(R.id.LandlordPassword);
        landlordRetypePassword = findViewById(R.id.LandlordRetype);

        landlordlogin = findViewById(R.id.landlordlogin);
        createAccountButton = findViewById(R.id.btnCreateAccount);

        // Initialize Lock/Unlock icons for password visibility toggle
        lockIconPassword = findViewById(R.id.lock1);
        unlockIconPassword = findViewById(R.id.unlock1);
        lockIconRetype = findViewById(R.id.lock);
        unlockIconRetype = findViewById(R.id.unlock);

        createAccountButton.setOnClickListener(view -> createAccount());

        // Login link click listener
        landlordlogin.setOnClickListener(v -> {
            startActivity(new Intent(LanlordRegister.this, LandlordLogin.class));
        });

        // Password visibility toggle for landlordPassword
        lockIconPassword.setOnClickListener(view -> togglePasswordVisibility(landlordPassword, lockIconPassword, unlockIconPassword));
        unlockIconPassword.setOnClickListener(view -> togglePasswordVisibility(landlordPassword, lockIconPassword, unlockIconPassword));

        // Password visibility toggle for landlordRetypePassword
        lockIconRetype.setOnClickListener(view -> togglePasswordVisibility(landlordRetypePassword, lockIconRetype, unlockIconRetype));
        unlockIconRetype.setOnClickListener(view -> togglePasswordVisibility(landlordRetypePassword, lockIconRetype, unlockIconRetype));

        // TextWatcher for landlordPassword
        landlordPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                togglePasswordIcons(landlordPassword, lockIconPassword, unlockIconPassword);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // TextWatcher for landlordRetypePassword
        landlordRetypePassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                togglePasswordIcons(landlordRetypePassword, lockIconRetype, unlockIconRetype);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    // Method to toggle the visibility of the password
    private void togglePasswordVisibility(EditText editText, ImageView lockIcon, ImageView unlockIcon) {
        if (editText.getTransformationMethod() instanceof PasswordTransformationMethod) {
            // Show the password
            editText.setTransformationMethod(null); // Show the text
            lockIcon.setVisibility(View.GONE);
            unlockIcon.setVisibility(View.VISIBLE);
        } else {
            // Hide the password
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance()); // Hide the text
            lockIcon.setVisibility(View.VISIBLE);
            unlockIcon.setVisibility(View.GONE);
        }
        // Move the cursor to the end of the text
        editText.setSelection(editText.getText().length());
    }

    // Method to update icon visibility based on password field state
    private void togglePasswordIcons(EditText editText, ImageView lockIcon, ImageView unlockIcon) {
        if (editText.getTransformationMethod() instanceof PasswordTransformationMethod) {
            lockIcon.setVisibility(View.VISIBLE);  // Lock icon visible (password hidden)
            unlockIcon.setVisibility(View.GONE);   // Unlock icon hidden
        } else {
            lockIcon.setVisibility(View.GONE);     // Lock icon hidden (password visible)
            unlockIcon.setVisibility(View.VISIBLE); // Unlock icon visible
        }
    }
    private void createAccount() {
        String firstName = landlordFirstName.getText().toString().trim();
        String lastName = landlordLastName.getText().toString().trim();
        String username = landlordusername.getText().toString().trim();
        String mobile = landlordMobile.getText().toString().trim();
        String email = landlordEmail.getText().toString().trim();
        String password = landlordPassword.getText().toString().trim();
        String retypePassword = landlordRetypePassword.getText().toString().trim();

        boolean isValid = validateInputs(firstName, lastName, username, mobile, email, password, retypePassword);
        if (!isValid) return;

        if (TextUtils.isEmpty(password) || TextUtils.isEmpty(retypePassword)) {
            // Show an alert if password or retype password is empty
            Toast.makeText(this, "Password and Retype Password are required", Toast.LENGTH_SHORT).show();

            // Hide the icons when the alert is shown
            lockIconPassword.setVisibility(View.GONE);
            unlockIconPassword.setVisibility(View.GONE);
            lockIconRetype.setVisibility(View.GONE);
            unlockIconRetype.setVisibility(View.GONE);

            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Send verification email
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    user.sendEmailVerification().addOnCompleteListener(emailTask -> {
                        if (emailTask.isSuccessful()) {
                            Toast.makeText(this, "Verification email sent. Please verify to log in.", Toast.LENGTH_SHORT).show();
                            saveLandlordInfo(user.getUid(), firstName, lastName, username, mobile, email);
                            startActivity(new Intent(LanlordRegister.this, LandlordLogin.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Failed to send verification email: " + emailTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Toast.makeText(this, "Account creation failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateInputs(String firstName, String lastName, String username, String mobile, String email, String password, String retypePassword) {
        boolean isValid = true;
        String nameRegex = "^[A-Za-z]+( [A-Za-z]+)*$";  // Allows letters only, with optional single spaces in between
        String alphabetOnlyRegex = "^[a-zA-Z]+$"; // Only allows alphabetic characters (no numbers, no special characters)
        String extraSpacesRegex = ".*\\s{2,}.*"; // Checks for more than one space anywhere in the name

        // Check for first name: Must not have extra spaces and only alphabetic characters
        if (TextUtils.isEmpty(firstName)) {
            landlordFirstName.setError("First name is required");
            isValid = false;
        } else if (firstName.matches(extraSpacesRegex)) {
            landlordFirstName.setError("Invalid format: avoid extra spaces in the first name");
            isValid = false;
        } else if (!firstName.matches(alphabetOnlyRegex)) {
            landlordFirstName.setError("First name must only contain letters, no numbers or special characters");
            isValid = false;
        } else if (!firstName.matches(nameRegex)) {
            landlordFirstName.setError("Invalid format: avoid extra spaces in the first name");
            isValid = false;
        }

        // Check for last name: Must not have extra spaces and only alphabetic characters
        if (TextUtils.isEmpty(lastName)) {
            landlordLastName.setError("Last name is required");
            isValid = false;
        } else if (lastName.matches(extraSpacesRegex)) {
            landlordLastName.setError("Invalid format: avoid extra spaces in the last name");
            isValid = false;
        } else if (!lastName.matches(alphabetOnlyRegex)) {
            landlordLastName.setError("Last name must only contain letters, no numbers or special characters");
            isValid = false;
        } else if (!lastName.matches(nameRegex)) {
            landlordLastName.setError("Invalid format: avoid extra spaces in the last name");
            isValid = false;
        }

        // Check for username: Allow letters, numbers, and a single space between words
        if (TextUtils.isEmpty(username)) {
            landlordusername.setError("Username is required");
            isValid = false;
        } else {
            username = username.trim(); // Trim leading and trailing spaces
            if (!username.matches("^[a-zA-Z0-9]+( [a-zA-Z0-9]+)*$")) { // Allows single spaces between words
                landlordusername.setError("Username must only contain letters, numbers, and a single space between words");
                isValid = false;
            } else if (username.contains("  ")) { // Check for multiple consecutive spaces
                landlordusername.setError("Username must not contain multiple consecutive spaces");
                isValid = false;
            }
        }



        // Validate mobile number
        if (TextUtils.isEmpty(mobile)) {
            landlordMobile.setError("Mobile number is required");
            isValid = false;
        } else if (!mobile.startsWith("09") || mobile.length() != 11) {
            landlordMobile.setError("Mobile number must start with '09' and be 11 digits long");
            isValid = false;
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            landlordEmail.setError("Email is required");
            isValid = false;
        } else if (!email.contains("@")) {
            landlordEmail.setError("Please enter a valid email address containing '@'");
            isValid = false;
        } else if (email.trim().contains(" ")) {
            landlordEmail.setError("Invalid format: avoid spaces in the email");
            isValid = false;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            landlordPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            landlordPassword.setError("Password should be at least 6 characters");
            isValid = false;
        } else if (TextUtils.isDigitsOnly(password)) {
            landlordPassword.setError("Password should not be only numbers");
            isValid = false;
        } else if (!password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*")) {
            landlordPassword.setError("Password must contain both uppercase and lowercase letters");
            isValid = false;
        }

        // Validate retype password
        if (TextUtils.isEmpty(retypePassword)) {
            landlordRetypePassword.setError("Retype password is required");
            isValid = false;
        } else if (!retypePassword.equals(password)) {
            landlordRetypePassword.setError("Passwords do not match");
            isValid = false;
        }

        // Hide icons if any validation fails and show a toast message
        if (!isValid) {
            // Hide the lock and unlock icons
            lockIconPassword.setVisibility(View.GONE);
            unlockIconPassword.setVisibility(View.GONE);
            lockIconRetype.setVisibility(View.GONE);
            unlockIconRetype.setVisibility(View.GONE);

            // Display a toast message
            Toast.makeText(this, "Please correct the highlighted fields", Toast.LENGTH_LONG).show();
        }


        return isValid;
    }




    private void saveLandlordInfo(String userId, String firstName, String lastName, String username, String mobile, String email) {
        HashMap<String, Object> landlordInfo = new HashMap<>();
        landlordInfo.put("userId", userId);
        landlordInfo.put("firstName", firstName);
        landlordInfo.put("lastName", lastName);
        landlordInfo.put("username", username);
        landlordInfo.put("mobile", mobile);
        landlordInfo.put("email", email);
        landlordInfo.put("accountType", "Landlord");

        landlordDatabase.collection("Landlords").document(userId).set(landlordInfo)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LanlordRegister.this, "Landlord info saved", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LanlordRegister.this, "Failed to save landlord info", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
