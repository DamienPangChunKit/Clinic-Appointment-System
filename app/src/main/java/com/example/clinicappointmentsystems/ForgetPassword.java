package com.example.clinicappointmentsystems;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPassword extends AppCompatActivity {
    private EditText etEmail;
    private Button btnResetPassword;
    private ProgressBar mProgressBar;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        etEmail = (EditText) findViewById(R.id.editTextEmail);
        btnResetPassword = (Button) findViewById(R.id.btnResetPassword);
        mProgressBar = (ProgressBar) findViewById(R.id.resetPasswordProgressBar);

        mAuth = FirebaseAuth.getInstance();

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword(); // include check email validation
            }
        });
    }

    private void resetPassword() {
        String emailInput = etEmail.getText().toString().trim();

        if (emailInput.isEmpty()){
            etEmail.setError("This field cannot be empty!");
            etEmail.requestFocus();
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()){
            etEmail.setError("Invalid format! Example email: jack123@gmail.com");
            etEmail.requestFocus();
            return;
        } else {
            etEmail.setError(null);
        }

        mProgressBar.setVisibility(View.VISIBLE);
        mAuth.sendPasswordResetEmail(emailInput).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ForgetPassword.this, "Please check your email to reset password!", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(ForgetPassword.this, Login.class);
                    startActivity(i);
                } else {
                    Toast.makeText(ForgetPassword.this, "Something went wrong, please try again!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}