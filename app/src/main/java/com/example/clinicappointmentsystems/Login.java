package com.example.clinicappointmentsystems;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity implements View.OnClickListener{
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mReference, myRef1, myRef;

    private TextView tvRegister, tvForgetPassword;
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar loginProgressBar;

    private String userID, userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        database = FirebaseDatabase.getInstance();
        myRef1 = database.getReference();
        myRef = myRef1.child("test");

        tvRegister = (TextView) findViewById(R.id.tvRegister);
        tvForgetPassword = (TextView) findViewById(R.id.tvForgetPassword);
        tvRegister.setOnClickListener(this);
        tvForgetPassword.setOnClickListener(this);

        etEmail = (EditText) findViewById(R.id.editTextEmail);
        etPassword = (EditText) findViewById(R.id.editTextPassword);

        loginProgressBar = (ProgressBar) findViewById(R.id.loginProgressBar);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvRegister:
                Intent i = new Intent(Login.this, Register.class);
                startActivity(i);
                break;

            case R.id.tvForgetPassword:
                Intent o = new Intent(Login.this, ForgetPassword.class);
                startActivity(o);
                break;

            case R.id.btnLogin:
                userLogin();
                break;
        }
    }

    private void userLogin() {
        String emailInput = etEmail.getText().toString().trim();
        String passwordInput = etPassword.getText().toString().trim();

        if (emailInput.isEmpty()) {
            etEmail.setError("Email is required!");
            etEmail.requestFocus();
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()){
            etEmail.setError("Invalid format! Example email: jack123@gmail.com");
            etEmail.requestFocus();
            return;
        } else {
            etEmail.setError(null);
        }

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

        loginProgressBar.setVisibility(View.VISIBLE);

        // check email and password input with the database
        mAuth.signInWithEmailAndPassword(emailInput, passwordInput).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                FirebaseUser verifyEmail = FirebaseAuth.getInstance().getCurrentUser();
                if (task.isSuccessful()) { // correct email and password
                    if (verifyEmail.isEmailVerified()) {
                        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        mReference = FirebaseDatabase.getInstance().getReference("Users");
                        mReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User userProfile = snapshot.getValue(User.class);

                                if (userProfile != null) {
                                    userType = userProfile.userType;

                                    if (userType.equals("admin")) {
                                        Intent i = new Intent(Login.this, AdminHomepage.class);
                                        startActivity(i);
                                    } else {
                                        Intent i = new Intent(Login.this, Homepage.class);
                                        startActivity(i);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(Login.this, "Profile information not retrieved!", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        Toast.makeText(Login.this, "Please verify your Email before login!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Login.this, "Failed to login! Email or password is incorrect!", Toast.LENGTH_SHORT).show();
                }

                loginProgressBar.setVisibility(View.GONE);
            }
        });
    }
}