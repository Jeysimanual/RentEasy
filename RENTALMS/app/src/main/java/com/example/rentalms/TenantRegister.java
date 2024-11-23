package com.example.rentalms;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

public class TenantRegister extends AppCompatActivity {

    // UI components
    EditText tenantFirstName, tenantLastName, tenantMobile, tenantusername, tenantEmail, tenantPassword, tenantRetypePassword;
    TextView tenantLogin;
    Button createAccountButton;

    // Firebase components
    FirebaseAuth mAuth;
    FirebaseFirestore tenantDatabase;

    // Password visibility toggle icons
    ImageView lock1, unlock1, lock2, unlock2;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_register);

        // Initialize Firebase Authentication and Firestore
        mAuth = FirebaseAuth.getInstance();
        tenantDatabase = FirebaseFirestore.getInstance();

        // Initialize UI components
        tenantFirstName = findViewById(R.id.Tenantfirstname);
        tenantLastName = findViewById(R.id.Tenantlastname);
        tenantusername = findViewById(R.id.Tenantusername);
        tenantMobile = findViewById(R.id.TenantMobile);
        tenantEmail = findViewById(R.id.TenantEmail);
        tenantPassword = findViewById(R.id.TenantPassword);
        tenantRetypePassword = findViewById(R.id.TenantRetype);

        tenantLogin = findViewById(R.id.tenantlogin);
        createAccountButton = findViewById(R.id.btnCreateAccount);

        // Initialize password visibility toggle icons
        lock1 = findViewById(R.id.lock1);
        unlock1 = findViewById(R.id.unlock1);
        lock2 = findViewById(R.id.lock);
        unlock2 = findViewById(R.id.unlock);

        // Create account button click listener
        createAccountButton.setOnClickListener(view -> createAccount());

        // Login link click listener
        tenantLogin.setOnClickListener(v -> {
            startActivity(new Intent(TenantRegister.this, TenantLogin.class));
        });

        // Password visibility toggle for TenantPassword
        lock1.setOnClickListener(view -> togglePasswordVisibility(tenantPassword, lock1, unlock1));
        unlock1.setOnClickListener(view -> togglePasswordVisibility(tenantPassword, lock1, unlock1));

        // Password visibility toggle for TenantRetypePassword
        lock2.setOnClickListener(view -> togglePasswordVisibility(tenantRetypePassword, lock2, unlock2));
        unlock2.setOnClickListener(view -> togglePasswordVisibility(tenantRetypePassword, lock2, unlock2));

        // TextWatcher for tenantPassword
        tenantPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                togglePasswordIcons(tenantPassword, lock1, unlock1);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // TextWatcher for tenantRetypePassword
        tenantRetypePassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                togglePasswordIcons(tenantRetypePassword, lock2, unlock2);
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
        String firstName = tenantFirstName.getText().toString().trim();
        String lastName = tenantLastName.getText().toString().trim();
        String username = tenantusername.getText().toString().trim();
        String mobile = tenantMobile.getText().toString().trim();
        String email = tenantEmail.getText().toString().trim();
        String password = tenantPassword.getText().toString().trim();
        String retypePassword = tenantRetypePassword.getText().toString().trim();

        boolean isValid = validateInputs(firstName, lastName, username, mobile, email, password, retypePassword);
        if (!isValid) return;

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Send verification email
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    user.sendEmailVerification().addOnCompleteListener(emailTask -> {
                        if (emailTask.isSuccessful()) {
                            Toast.makeText(this, "Verification email sent. Please verify to log in.", Toast.LENGTH_SHORT).show();
                            saveTenantInfo(user.getUid(), firstName, lastName, username, mobile, email);
                            startActivity(new Intent(TenantRegister.this, TenantLogin.class));
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

        // Regular expressions
        String nameRegex = "^[A-Za-z]+( [A-Za-z]+)*$"; // Allows letters only, with optional single spaces in between
        String alphabetOnlyRegex = "^[a-zA-Z]+$"; // Only allows alphabetic characters
        String extraSpacesRegex = ".*\\s{2,}.*"; // Checks for more than one space anywhere

        // First name validation
        if (TextUtils.isEmpty(firstName)) {
            tenantFirstName.setError("First name is required");
            isValid = false;
        } else if (firstName.matches(extraSpacesRegex)) {
            tenantFirstName.setError("Invalid format: avoid extra spaces in the first name");
            isValid = false;
        } else if (!firstName.matches(alphabetOnlyRegex)) {
            tenantFirstName.setError("First name must only contain letters, no numbers or special characters");
            isValid = false;
        } else if (!firstName.matches(nameRegex)) {
            tenantFirstName.setError("Invalid format: avoid leading or trailing spaces");
            isValid = false;
        }

        // Last name validation
        if (TextUtils.isEmpty(lastName)) {
            tenantLastName.setError("Last name is required");
            isValid = false;
        } else if (lastName.matches(extraSpacesRegex)) {
            tenantLastName.setError("Invalid format: avoid extra spaces in the last name");
            isValid = false;
        } else if (!lastName.matches(alphabetOnlyRegex)) {
            tenantLastName.setError("Last name must only contain letters, no numbers or special characters");
            isValid = false;
        } else if (!lastName.matches(nameRegex)) {
            tenantLastName.setError("Invalid format: avoid leading or trailing spaces");
            isValid = false;
        }

        // Username validation
        if (TextUtils.isEmpty(username)) {
            tenantusername.setError("Username is required");
            isValid = false;
        } else {
            username = username.trim(); // Trim leading and trailing spaces
            if (!username.matches("^[a-zA-Z0-9]+( [a-zA-Z0-9]+)*$")) {
                tenantusername.setError("Username must only contain letters, numbers, and a single space between words");
                isValid = false;
            } else if (username.contains("  ")) {
                tenantusername.setError("Username must not contain multiple consecutive spaces");
                isValid = false;
            }
        }

        // Mobile number validation
        if (TextUtils.isEmpty(mobile)) {
            tenantMobile.setError("Mobile number is required");
            isValid = false;
        } else if (!mobile.startsWith("09") || mobile.length() != 11) {
            tenantMobile.setError("Mobile number must start with '09' and be 11 digits long");
            isValid = false;
        }

        // Email validation
        if (TextUtils.isEmpty(email)) {
            tenantEmail.setError("Email is required");
            isValid = false;
        } else if (!email.contains("@")) {
            tenantEmail.setError("Please enter a valid email address containing '@'");
            isValid = false;
        } else if (email.trim().contains(" ")) {
            tenantEmail.setError("Invalid format: avoid spaces in the email");
            isValid = false;
        }

        // Password validation
        if (TextUtils.isEmpty(password)) {
            tenantPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            tenantPassword.setError("Password should be at least 6 characters");
            isValid = false;
        } else if (TextUtils.isDigitsOnly(password)) {
            tenantPassword.setError("Password should not be only numbers");
            isValid = false;
        } else if (!password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*")) {
            tenantPassword.setError("Password must contain both uppercase and lowercase letters");
            isValid = false;
        }

        // Retype password validation
        if (TextUtils.isEmpty(retypePassword)) {
            tenantRetypePassword.setError("Please retype your password");
            isValid = false;
        } else if (!retypePassword.equals(password)) {
            tenantRetypePassword.setError("Passwords do not match");
            isValid = false;
        }

        if (!isValid) {
            // Hide the lock and unlock icons
            lock1.setVisibility(View.GONE);
            unlock1.setVisibility(View.GONE);
            lock2.setVisibility(View.GONE);
            unlock2.setVisibility(View.GONE);

            // Display a toast message
            Toast.makeText(this, "Please correct the highlighted fields", Toast.LENGTH_LONG).show();
        }

        return isValid;
    }

    private void saveTenantInfo(String userId, String firstName, String lastName, String username, String mobile, String email) {
        HashMap<String, Object> tenantInfo = new HashMap<>();
        tenantInfo.put("firstName", firstName);
        tenantInfo.put("lastName", lastName);
        tenantInfo.put("username", username);
        tenantInfo.put("mobile", mobile);
        tenantInfo.put("email", email);
        tenantInfo.put("accountType", "Tenant");

        tenantDatabase.collection("Tenants").document(userId).set(tenantInfo)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Tenant data saved", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to save tenant data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
