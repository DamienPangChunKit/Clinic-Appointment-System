package com.example.clinicappointmentsystems;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.UUID;

public class NavigationContactUs extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private DatabaseReference mReference;

    private EditText etName, etEmail, etPhone, etMessage;
    private Spinner spinnerTitle;
    private Button btnContactUs;
    private ProgressBar contactUsProgressBar;

    private String email, phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_contact_us);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        etName = (EditText) findViewById(R.id.editTextName);
        etEmail = (EditText) findViewById(R.id.editTextEmail);
        etPhone = (EditText) findViewById(R.id.editTextPhone);
        etMessage = (EditText) findViewById(R.id.editTextMessage);
        btnContactUs = (Button) findViewById(R.id.btnContactUs);
        btnContactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendContactUs();
            }
        });

        spinnerTitle = (Spinner) findViewById(R.id.spinnerTitle);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.title, R.layout.spinner_title);
        adapter.setDropDownViewResource(R.layout.custom_drop_down_spinner);
        spinnerTitle.setPadding(10, 10, 10, 10);
        spinnerTitle.setAdapter(adapter);
        spinnerTitle.setOnItemSelectedListener(this);

        Intent i = getIntent();
        email = i.getStringExtra("email");
        phone = i.getStringExtra("phone");

        etEmail.setText(email);
        etPhone.setText(phone);

        contactUsProgressBar = (ProgressBar) findViewById(R.id.contactUsProgressBar);
    }

    private void sendContactUs() {
        String nameInput = etName.getText().toString().trim();
        String phoneInput = etPhone.getText().toString().trim();
        String emailInput = etEmail.getText().toString().trim();
        String messageInput = etMessage.getText().toString().trim();
        String titleInput = spinnerTitle.getSelectedItem().toString().trim();
        String status = "pending";
        String mailID = UUID.randomUUID().toString().substring(0,10);

        // get current date and time
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        String date = day + "/" + month + "/" + year;
        String time;

        if (hour >= 12) {
            if (hour != 12) {
                hour = hour - 12;
            }

            if (minute < 10) {
                time = hour + ":0" + minute + " pm";
            } else {
                time = hour + ":" + minute + " pm";
            }

        } else {
            if (minute < 10) {
                time = hour + ":0" + minute + " am";
            } else {
                time = hour + ":" + minute + " am";
            }
        }
        String dateTime = date + " " + time;

        if (nameInput.isEmpty()){
            etName.setError("This field cannot be empty!");
            etName.requestFocus();
            return;
        } else {
            etName.setError(null);
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

        if (messageInput.isEmpty()){
            etMessage.setError("This field cannot be empty!");
            etMessage.requestFocus();
            return;
        } else {
            etMessage.setError(null);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(NavigationContactUs.this);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure want to send this message?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String getPushKey;
                contactUsProgressBar.setVisibility(View.VISIBLE);

                mReference =  FirebaseDatabase.getInstance().getReference("ContactUs").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).push();
                getPushKey = mReference.getKey();
                ContactUs contactUs = new ContactUs(nameInput, emailInput, phoneInput, messageInput, titleInput, dateTime, status, mailID, getPushKey);
                mReference.setValue(contactUs).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(NavigationContactUs.this, "Your message have been received, thank you!", Toast.LENGTH_SHORT).show();
                            contactUsProgressBar.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(NavigationContactUs.this, "Error sending message, please try again!", Toast.LENGTH_SHORT).show();
                            contactUsProgressBar.setVisibility(View.GONE);
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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}