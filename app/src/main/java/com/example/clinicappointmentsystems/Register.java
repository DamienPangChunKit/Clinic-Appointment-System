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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class Register extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private FirebaseAuth mAuth;

    private EditText etUsername, etPhone, etEmail, etPassword, etAddress;
    private Button btnRegister, btnBirthDate;
    private ProgressBar registerProgressBar;
    private Spinner spinnerGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mAuth = FirebaseAuth.getInstance();

        etUsername = (EditText) findViewById(R.id.editTextUsername);
        etPhone = (EditText) findViewById(R.id.editTextPhone);
        etEmail = (EditText) findViewById(R.id.editTextEmail);
        etPassword = (EditText) findViewById(R.id.editTextPassword);
        etAddress = (EditText) findViewById(R.id.editTextAddress);

        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(this);

        btnBirthDate = (Button) findViewById(R.id.btnBirthDate);
        btnBirthDate.setOnClickListener(this);

        spinnerGender = (Spinner) findViewById(R.id.spinnerGender);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gender, R.layout.spinner_text);
        adapter.setDropDownViewResource(R.layout.custom_drop_down_spinner);
        spinnerGender.setPadding(10, 10, 10, 10);
        spinnerGender.setAdapter(adapter);
        spinnerGender.setOnItemSelectedListener(this);

        registerProgressBar = (ProgressBar) findViewById(R.id.registerProgressBar);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnRegister:
                validateRegister();
                break;

            case R.id.btnBirthDate:
                selectBirthDate();
                break;
        }
    }

    private void selectBirthDate() {
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                Register.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month = month + 1;
                String date = day + "/" + month + "/" + year;
                btnBirthDate.setText(date);
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    private void validateRegister() {
        String usernameInput = etUsername.getText().toString().trim();
        String genderInput = spinnerGender.getSelectedItem().toString().trim();
        String birthDateInput = btnBirthDate.getText().toString().trim();
        String phoneInput = etPhone.getText().toString().trim();
        String emailInput = etEmail.getText().toString().trim();
        String passwordInput = etPassword.getText().toString().trim();
        String addressInput = etAddress.getText().toString().trim();
        String balance = "0.00";
        String userType = "customer";
        String imageInput = "https://firebasestorage.googleapis.com/v0/b/clinic-appointment-syste-9137d.appspot.com/o/images%2F94484911-0075-4c5f-9d35-631e94ee727b?alt=media&token=27c7ab98-2546-41d0-9b2e-f25b3bb3d1e1";
        String role = "";

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

        AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure want to register account?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                mAuth.fetchSignInMethodsForEmail(emailInput)
                        .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                            @Override
                            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                boolean checkEmail = !task.getResult().getSignInMethods().isEmpty();

                                if (!checkEmail) {
                                    registerProgressBar.setVisibility(View.VISIBLE);
                                    mAuth.createUserWithEmailAndPassword(emailInput, passwordInput)
                                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if (task.isSuccessful()) {
                                                        User user = new User(usernameInput, genderInput, birthDateInput, phoneInput, emailInput, passwordInput, imageInput, addressInput, balance, userType, role);
                                                        FirebaseUser verifyEmail = FirebaseAuth.getInstance().getCurrentUser();

                                                        FirebaseDatabase.getInstance().getReference()
                                                                .child("Users")
                                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    verifyEmail.sendEmailVerification();
                                                                    Toast.makeText(Register.this, "Register account successfully! Please verified your account before login!", Toast.LENGTH_SHORT).show();
                                                                    Intent i = new Intent(Register.this, Login.class);
                                                                    startActivity(i);
                                                                } else {
                                                                    Toast.makeText(Register.this, "Failed to register an account!", Toast.LENGTH_SHORT).show();
                                                                }

                                                                registerProgressBar.setVisibility(View.GONE);
                                                            }
                                                        });

                                                    } else {
                                                        Toast.makeText(Register.this, "Failed to register an account!", Toast.LENGTH_SHORT).show();
                                                        registerProgressBar.setVisibility(View.GONE);
                                                    }
                                                }
                                            });
                                } else {
                                    Toast.makeText(Register.this, "Email has already been register before, please try a new Email!", Toast.LENGTH_SHORT).show();
                                    return;
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
}