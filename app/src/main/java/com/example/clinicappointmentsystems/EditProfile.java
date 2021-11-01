package com.example.clinicappointmentsystems;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference mReference;

    private EditText etUsername, etPhone, etEmail, etAddress;
    private Button btnConfirmEditProfile, btnBirthDate;
    private ProgressBar editProfileProgressBar;
    private Spinner spinnerGender;

    private String userID, username, phone, email, birthDate, address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        etUsername = (EditText) findViewById(R.id.editTextUsername);
        etPhone = (EditText) findViewById(R.id.editTextPhone);
        etEmail = (EditText) findViewById(R.id.editTextEmail);
        etAddress = (EditText) findViewById(R.id.editTextAddress);

        btnConfirmEditProfile = (Button) findViewById(R.id.btnConfirmEditProfile);
        btnConfirmEditProfile.setOnClickListener(this);

        btnBirthDate = (Button) findViewById(R.id.btnBirthDate);
        btnBirthDate.setOnClickListener(this);

        spinnerGender = (Spinner) findViewById(R.id.spinnerGender);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gender, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
        spinnerGender.setOnItemSelectedListener(this);

        editProfileProgressBar = (ProgressBar) findViewById(R.id.editProfileProgressBar);

        Intent a = getIntent();
        userID = a.getStringExtra("userID");
        username = a.getStringExtra("username");
        phone = a.getStringExtra("phone");
        email = a.getStringExtra("email");
        birthDate = a.getStringExtra("birthDate");
        address = a.getStringExtra("address");

        etUsername.setText(username);
        etPhone.setText(phone);
        etEmail.setText(email);
        etAddress.setText(address);
        btnBirthDate.setText(birthDate);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnConfirmEditProfile:
                confirmEditProfile();
                break;

            case R.id.btnBirthDate:
                selectBirthDate();
                break;
        }
    }

    private void confirmEditProfile() {
        String usernameInput = etUsername.getText().toString().trim();
        String genderInput = spinnerGender.getSelectedItem().toString().trim();
        String birthDateInput = btnBirthDate.getText().toString().trim();
        String phoneInput = etPhone.getText().toString().trim();
        String emailInput = etEmail.getText().toString().trim();
        String addressInput = etAddress.getText().toString().trim();

        if (usernameInput.isEmpty()){
            etUsername.setError("This field cannot be empty!");
            etUsername.requestFocus();
            return;
        } else if (usernameInput.length() < 6){
            etUsername.setError("Username must contain at least 6 character!");
            etUsername.requestFocus();
            return;
        } else if (usernameInput.length() > 15){
            etUsername.setError("Username too long!");
            etUsername.requestFocus();
            return;
        } else {
            etUsername.setError(null);
        }

        if (birthDateInput.equals("Date of Birth")){
            btnBirthDate.setError("Please select date of birth!");
            btnBirthDate.requestFocus();
            return;
        } else {
            btnBirthDate.setError(null);
        }

        if (addressInput.isEmpty()){
            etAddress.setError("This field cannot be empty!");
            etAddress.requestFocus();
            return;
        } else {
            etAddress.setError(null);
        }

        if (phoneInput.isEmpty()){
            etPhone.setError("This field cannot be empty!");
            etPhone.requestFocus();
            return;
        } else if (phoneInput.charAt(0) != '0' |
                phoneInput.charAt(1) != '1' |
                phoneInput.length() < 10 |
                phoneInput.length() > 11) {
            etPhone.setError("Please input phone number format as 0123456789");
            etPhone.requestFocus();
            return;
        } else {
            etPhone.setError(null);
        }

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

        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfile.this);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure want to edit profile?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mAuth.fetchSignInMethodsForEmail(emailInput).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() { // check whether email is unique
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        editProfileProgressBar.setVisibility(View.VISIBLE);
                        boolean checkEmail = !task.getResult().getSignInMethods().isEmpty();

                        if (!checkEmail || emailInput.equals(email)) { // if email input is not same with the email in database
                            user.updateEmail(emailInput).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    FirebaseUser verifyEmail = FirebaseAuth.getInstance().getCurrentUser();
                                    verifyEmail.sendEmailVerification();

                                    Map<String, Object> childUpdates = new HashMap<>();
                                    childUpdates.put("username" , usernameInput);
                                    childUpdates.put("gender" , genderInput);
                                    childUpdates.put("birthDate" , birthDateInput);
                                    childUpdates.put("phone" , phoneInput);
                                    childUpdates.put("email" , emailInput);
                                    childUpdates.put("address" , addressInput);
                                    mReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
                                    mReference.updateChildren(childUpdates);

                                    Intent z = new Intent(EditProfile.this, Homepage.class);
                                    startActivity(z);
                                    Toast.makeText(EditProfile.this, "Edit profile successfully!", Toast.LENGTH_SHORT).show();
                                    editProfileProgressBar.setVisibility(View.GONE);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(EditProfile.this, "Failed to edit profile!", Toast.LENGTH_SHORT).show();
                                    editProfileProgressBar.setVisibility(View.GONE);
                                }
                            });
                        } else {
                            Toast.makeText(EditProfile.this, "Email has already been register before, please try a new Email!", Toast.LENGTH_SHORT).show();
                            editProfileProgressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void selectBirthDate() {
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                EditProfile.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month = month + 1;
                String date = day + "/" + month + "/" + year;
                btnBirthDate.setText(date);
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
//        String gender = adapterView.getItemAtPosition(position).toString();
//        Toast.makeText(adapterView.getContext(), gender, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}