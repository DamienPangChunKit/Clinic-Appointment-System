package com.example.clinicappointmentsystems;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ChangePassword extends AppCompatActivity {
    private DatabaseReference mReference;
    private FirebaseUser user;
    private FirebaseAuth mAuth;

    private EditText etPassword, etNewPassword, etConfirmPassword;
    private Button btnChangePassword;
    private ProgressBar changePasswordProgressBar;

    private String userID, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        Intent i = getIntent();
        userID = i.getStringExtra("userID");

        etPassword = (EditText) findViewById(R.id.editTextPassword);
        etNewPassword = (EditText) findViewById(R.id.editTextNewPassword);
        etConfirmPassword = (EditText) findViewById(R.id.editTextConfirmPassword);
        btnChangePassword = (Button) findViewById(R.id.btnChangePassword);
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassword();
            }
        });

        changePasswordProgressBar = (ProgressBar) findViewById(R.id.changePasswordProgressBar);
    }

    private void changePassword() {
        String passwordInput = etPassword.getText().toString().trim();
        String newPasswordInput = etNewPassword.getText().toString().trim();
        String confirmPasswordInput = etConfirmPassword.getText().toString().trim();

        if (passwordInput.isEmpty()){
            etPassword.setError("This field cannot be empty!");
            etPassword.requestFocus();
            return;
        } else if (passwordInput.length() < 8){
            etPassword.setError("Password must contain at least 8 character!");
            etPassword.requestFocus();
            return;
        } else {
            etPassword.setError(null);
        }

        if (newPasswordInput.isEmpty()){
            etNewPassword.setError("This field cannot be empty!");
            etNewPassword.requestFocus();
            return;
        } else if (newPasswordInput.length() < 8){
            etNewPassword.setError("Password must contain at least 8 character!");
            etNewPassword.requestFocus();
            return;
        } else if (newPasswordInput.equals(passwordInput)){
            etNewPassword.setError("Old and new password must not be same!");
            etNewPassword.requestFocus();
            return;
        } else {
            etNewPassword.setError(null);
        }

        if (confirmPasswordInput.isEmpty()){
            etConfirmPassword.setError("This field cannot be empty!");
            etConfirmPassword.requestFocus();
            return;
        } else if (confirmPasswordInput.length() < 8){
            etConfirmPassword.setError("Password must contain at least 8 character!");
            etConfirmPassword.requestFocus();
            return;
        } else if (!confirmPasswordInput.equals(newPasswordInput)){
            etConfirmPassword.setError("Confirm password input was not same with new password!");
            etConfirmPassword.requestFocus();
            return;
        } else {
            etConfirmPassword.setError(null);
        }


        mReference = FirebaseDatabase.getInstance().getReference("Users");
        mReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);

                if (userProfile != null) {
                    password = userProfile.password;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChangePassword.this, "Information was not retrieved from database!", Toast.LENGTH_SHORT).show();
            }
        });

        if (!passwordInput.equals(password)) {
            etPassword.setError("Password input incorrect!");
            etPassword.requestFocus();
            return;
        } else {
            changePasswordProgressBar.setVisibility(View.VISIBLE);
            user.updatePassword(newPasswordInput).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("password" , newPasswordInput);
                    mReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
                    mReference.updateChildren(childUpdates);

                    Toast.makeText(ChangePassword.this, "Change password successfully!", Toast.LENGTH_SHORT).show();
                    changePasswordProgressBar.setVisibility(View.GONE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ChangePassword.this, "Failed to change password!", Toast.LENGTH_SHORT).show();
                    changePasswordProgressBar.setVisibility(View.GONE);
                }
            });
        }
    }
}