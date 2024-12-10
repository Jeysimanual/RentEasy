package com.project.rentalms;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class TenantLogin extends AppCompatActivity {

    // UI Components
    EditText tenantemail, tenantPassword;
    Button tenantloginbtn;
    TextView Tenantcreate, tenantForgotPassword;
    ImageView lockIcon, unlockIcon;

    // Firebase Authentication and Firestore
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_login);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        // Initialize UI elements
        tenantemail = findViewById(R.id.tenantemail);
        tenantPassword = findViewById(R.id.tenantPassword);
        tenantloginbtn = findViewById(R.id.tenantloginbtn);
        Tenantcreate = findViewById(R.id.Tenantcreate);
        tenantForgotPassword = findViewById(R.id.tenantFP); // Forgot Password TextView
        lockIcon = findViewById(R.id.lockIcon); // Lock icon for hiding password
        unlockIcon = findViewById(R.id.unlockIcon); // Unlock icon for showing password

        // Set up the listener for the create account link
        Tenantcreate.setOnClickListener(view -> {
            startActivity(new Intent(TenantLogin.this, TenantRegister.class));
        });

        // Set up the listener for the Forgot Password link
        tenantForgotPassword.setOnClickListener(view -> {
            startActivity(new Intent(TenantLogin.this, ForgetPassword.class));
        });

        // Set up login button click listener
        tenantloginbtn.setOnClickListener(view -> loginUser());

        // Set up the password visibility toggle
        lockIcon.setOnClickListener(view -> {
            tenantPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            lockIcon.setVisibility(View.GONE);
            unlockIcon.setVisibility(View.VISIBLE);
        });

        unlockIcon.setOnClickListener(view -> {
            tenantPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            unlockIcon.setVisibility(View.GONE);
            lockIcon.setVisibility(View.VISIBLE);
        });
    }

    private void loginUser() {
        String email = tenantemail.getText().toString().trim();
        String password = tenantPassword.getText().toString().trim();

        // Validate the email and password fields
        if (TextUtils.isEmpty(email)) {
            tenantemail.setError("Email is required");
            tenantemail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            tenantPassword.setError("Password is required");
            tenantPassword.requestFocus();
            return;
        }

        // Authenticate the user
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    // Reload user to ensure email verification status is up-to-date
                    user.reload().addOnCompleteListener(reloadTask -> {
                        if (reloadTask.isSuccessful()) {
                            if (user.isEmailVerified()) {
                                // Check the user's account type in Firestore
                                String userId = user.getUid();
                                db.collection("Tenants").document(userId).get().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        DocumentSnapshot document = task1.getResult();
                                        if (document != null && document.exists()) {
                                            String accountType = document.getString("accountType");
                                            if ("Tenant".equals(accountType)) {
                                                // User is a tenant, allow access
                                                Toast.makeText(TenantLogin.this, "Login successful!", Toast.LENGTH_SHORT).show();

                                                // Pass the tenantId (userId) to TenantPage
                                                Intent intent = new Intent(TenantLogin.this, TenantPage.class);
                                                intent.putExtra("tenantId", mAuth.getCurrentUser().getUid());
                                                startActivity(intent);
                                                finish(); // Close the login activity
                                            } else {
                                                // User is not a tenant, deny access
                                                Toast.makeText(TenantLogin.this, "Access denied: You are not a tenant", Toast.LENGTH_LONG).show();
                                                mAuth.signOut(); // Sign the user out
                                            }
                                        } else {
                                            Toast.makeText(TenantLogin.this, "Error: Account type not found!", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(TenantLogin.this, "Error: " + task1.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                // Email is not verified
                                user.sendEmailVerification().addOnCompleteListener(emailTask -> {
                                    if (emailTask.isSuccessful()) {
                                        Toast.makeText(this, "Verification email sent. Please verify to log in.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this, "Failed to send verification email: " + emailTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                                mAuth.signOut(); // Sign out if the email is not verified
                            }
                        } else {
                            Toast.makeText(TenantLogin.this, "Failed to reload user data: " + reloadTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                // Login failed, show error message
                Toast.makeText(TenantLogin.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
