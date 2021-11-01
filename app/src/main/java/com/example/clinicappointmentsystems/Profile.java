package com.example.clinicappointmentsystems;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class Profile extends AppCompatActivity {
    private TextView tvUsername, tvPhone, tvEmail, tvGender, tvBirthDate, tvAddress;
    private Button btnEditProfile;

    private String userID, username, phone, email, gender, birthDate, address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        tvUsername = (TextView) findViewById(R.id.tvUsername);
        tvPhone = (TextView) findViewById(R.id.tvPhone);
        tvEmail = (TextView) findViewById(R.id.tvEmail);
        tvGender = (TextView) findViewById(R.id.tvGender);
        tvBirthDate = (TextView) findViewById(R.id.tvBirthDate);
        tvAddress = (TextView) findViewById(R.id.tvAddress);

        btnEditProfile = (Button) findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Profile.this, EditProfile.class);
                i.putExtra("userID", userID);
                i.putExtra("username", username);
                i.putExtra("phone", phone);
                i.putExtra("email", email);
                i.putExtra("birthDate", birthDate);
                i.putExtra("address", address);
                startActivity(i);
            }
        });

        Intent a = getIntent();
        userID = a.getStringExtra("userID");
        username = a.getStringExtra("username");
        phone = a.getStringExtra("phone");
        email = a.getStringExtra("email");
        gender = a.getStringExtra("gender");
        birthDate = a.getStringExtra("birthDate");
        address = a.getStringExtra("address");

        tvUsername.setText(username);
        tvPhone.setText(phone);
        tvEmail.setText(email);
        tvGender.setText(gender);
        tvBirthDate.setText(birthDate);
        tvAddress.setText(address);
    }
}