package com.example.rentalms;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    TextView createacc, forgotPassword;
    EditText emailInput, passwordInput;
    Button loginButton;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        createacc = findViewById(R.id.createacc);
        forgotPassword = findViewById(R.id.FP);  // Added for "Forgot Password"
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.Password);
        loginButton = findViewById(R.id.loginbtn);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mAuth.signOut();
        }

        createacc.setOnClickListener(view -> {
            startActivity(new Intent(Login.this, CreateAccount.class));
        });

        forgotPassword.setOnClickListener(view -> {
            // Launch ForgetPassword activity when "Forgot Password?" is clicked
            startActivity(new Intent(Login.this, ForgetPassword.class));
        });

        loginButton.setOnClickListener(view -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                loginUser(email, password);
            } else {
                Toast.makeText(Login.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    checkUserType(currentUser.getUid());
                }
            } else {
                handleLoginFailure(task.getException());
            }
        });
    }

    private void handleLoginFailure(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidUserException) {
            Toast.makeText(Login.this, "No account found with this email address.", Toast.LENGTH_LONG).show();
            emailInput.setError("Invalid email address.");
            emailInput.requestFocus();
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(Login.this, "Incorrect password. Please try again.", Toast.LENGTH_LONG).show();
            passwordInput.setError("Incorrect password.");
            passwordInput.requestFocus();
        } else {
            Toast.makeText(Login.this, "Login failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void checkUserType(String userId) {
        db.collection("Landlords").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    startActivity(new Intent(Login.this, LandlordPage.class));
                    finish();
                } else {
                    checkTenantCollection(userId);
                }
            } else {
                Toast.makeText(Login.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkTenantCollection(String userId) {
        db.collection("Tenants").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Intent intent = new Intent(Login.this, TenantPage.class);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(Login.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(Login.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
