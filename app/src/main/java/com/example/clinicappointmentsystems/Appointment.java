package com.example.clinicappointmentsystems;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Appointment extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private DatabaseReference mReference;

    private Spinner spinnerGender;
    private EditText etFirstName, etLastName, etPhone, etBirthDate, etStreetAddress, etPostcode, etCity, etEmail, etSymptoms, etOther, etAppointmentDate, etAppointmentTime;
    private Button btnMakeAppointment;
    private ProgressBar appointmentProgressBar;

    private String deletePM, deletePMInput;
    private String[] splitDate, splitTime;
    private String[] takenDate, takenMonth, takenYear, takenHour, takenMin;
    private String[] splitDateInput, splitTimeInput;
    private String dateInput, monthInput, yearInput, hourInput, minInput;
    private String userID, balance;
    int i = 0;
    private int size;
    ArrayList<String> bookedDate = new ArrayList<String>();
    ArrayList<String> bookedTime = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // retrieve appointment date and time that has already booked
        fetchBookedDateTime();
        bookedDate = new ArrayList<>(size);
        bookedTime = new ArrayList<>(size);
        loadDatabase();

        spinnerGender = (Spinner) findViewById(R.id.spinnerGender);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gender, R.layout.spinner_text);
        adapter.setDropDownViewResource(R.layout.custom_drop_down_spinner);
        spinnerGender.setPadding(10, 10, 10, 10);
        spinnerGender.setAdapter(adapter);
        spinnerGender.setOnItemSelectedListener(this);

        Intent i = getIntent();
        userID = i.getStringExtra("userID");
        balance = i.getStringExtra("balance");

        appointmentProgressBar = (ProgressBar) findViewById(R.id.appointmentProgressBar);
        etFirstName = (EditText) findViewById(R.id.editTextFirstName);
        etLastName = (EditText) findViewById(R.id.editTextLastName);
        etPhone = (EditText) findViewById(R.id.editTextPhone);
        etBirthDate = (EditText) findViewById(R.id.editTextDateBirth);
        etStreetAddress = (EditText) findViewById(R.id.editTextStreetAddress);
        etPostcode = (EditText) findViewById(R.id.editTextPostcode);
        etCity = (EditText) findViewById(R.id.editTextCity);
        etEmail = (EditText) findViewById(R.id.editTextEmail);
        etSymptoms = (EditText) findViewById(R.id.editTextSymptoms);
        etOther = (EditText) findViewById(R.id.editTextOther);
        etAppointmentDate = (EditText) findViewById(R.id.editTextAppointmentDate);
        etAppointmentTime = (EditText) findViewById(R.id.editTextAppointmentTime);
        btnMakeAppointment = (Button) findViewById(R.id.btnMakeAppointment);
        btnMakeAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeAppointment();
            }
        });

        etBirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayBirthDateCalendar();
            }
        });

        etAppointmentDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAppointmentDateCalendar();
            }
        });

        etAppointmentTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAppointmentTimeSelection();
            }
        });
    }

    private void loadDatabase(){
        mReference = FirebaseDatabase.getInstance().getReference("AppointmentMade");
        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot allUserID : snapshot.getChildren()) {
                    String userID = allUserID.getKey();

                    mReference.child(userID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                AppointmentMade appointmentMade = dataSnapshot.getValue(AppointmentMade.class);

                                if (appointmentMade != null && i < size) {
                                    bookedDate.add(appointmentMade.appointmentDate); // add appointment date from database one by one into array list
                                    bookedTime.add(appointmentMade.appointmentTime);

                                    deletePM = bookedTime.get(i).replace(" pm", "");

                                    splitDate = bookedDate.get(i).split("/"); // split all the / and store one by one into array
                                    splitTime = deletePM.split(":");
                                    takenDate[i] = splitDate[0];
                                    takenMonth[i] = splitDate[1];
                                    takenYear[i] = splitDate[2];
                                    takenHour[i] = splitTime[0];
                                    takenMin[i] = splitTime[1];

                                    i++;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Appointment.this, "Appointment made information not retrieved!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchBookedDateTime() {
        mReference = FirebaseDatabase.getInstance().getReference("AppointmentMade");
        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot allUserID : snapshot.getChildren()) {
                    String userID = allUserID.getKey();

                    mReference.child(userID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                size = (int) snapshot.getChildrenCount();
                                takenDate = new String[size];
                                takenMonth = new String[size];
                                takenYear = new String[size];
                                takenHour = new String[size];
                                takenMin = new String[size];

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Appointment.this, "Appointment made information not retrieved!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayAppointmentTimeSelection() {
        Calendar c = Calendar.getInstance();
        final int hour = c.get(Calendar.HOUR_OF_DAY);
        final int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        c.set(Calendar.MINUTE, minute);

                        if (minute >= 0 && minute <= 7) {
                            minute = 0;
                        } else if (minute > 7 && minute < 15) {
                            minute = 15;
                        } else if (minute >= 15 && minute <= 22) {
                            minute = 15;
                        } else if (minute > 22 && minute < 30) {
                            minute = 30;
                        } else if (minute >= 30 && minute <= 37) {
                            minute = 30;
                        } else if (minute > 37 && minute < 45) {
                            minute = 45;
                        } else if (minute >= 45 && minute <= 52) {
                            minute = 45;
                        } else {
                            minute = 0;
                            hourOfDay = hourOfDay + 1;
                        }

                        if (hourOfDay < 9 || hourOfDay >= 18 || hourOfDay == 13) {
                            etAppointmentTime.setText(null);
                            etAppointmentTime.setHint("Time");
                            etAppointmentTime.setError("Please select time within 9am - 1pm and 2pm - 6pm!");
                            Toast.makeText(Appointment.this, "Please select time within 9am - 1pm and 2pm - 6pm!", Toast.LENGTH_SHORT).show();
                            etAppointmentTime.requestFocus();

                        } else {
                            if (hourOfDay == 17 && minute > 40) {
                                etAppointmentTime.setText(null);
                                etAppointmentTime.setHint("Time");
                                etAppointmentTime.setError("Please select time before 5:40pm!");
                                Toast.makeText(Appointment.this, "Please select time before 5:40pm!", Toast.LENGTH_SHORT).show();
                                etAppointmentTime.requestFocus();

                            } else if (hourOfDay == 12 && minute > 40) {
                                etAppointmentTime.setText(null);
                                etAppointmentTime.setHint("Time");
                                etAppointmentTime.setError("Please select time before 12:40pm!");
                                Toast.makeText(Appointment.this, "Please select time before 12:40pm!", Toast.LENGTH_SHORT).show();
                                etAppointmentTime.requestFocus();

                            } else {
                                if (hourOfDay >= 12) {
                                    if (hourOfDay != 12) {
                                        hourOfDay = hourOfDay - 12;
                                    }

                                    if (minute < 10) {
                                        etAppointmentTime.setText(hourOfDay + ":0" + minute + " pm");
                                    } else {
                                        etAppointmentTime.setText(hourOfDay + ":" + minute + " pm");
                                    }

                                } else {
                                    if (minute < 10) {
                                        etAppointmentTime.setText(hourOfDay + ":0" + minute + " am");
                                    } else {
                                        etAppointmentTime.setText(hourOfDay + ":" + minute + " am");
                                    }
                                }
                                etAppointmentTime.setError(null);
                            }
                        }
                    }
                }, hour, minute, false);
        timePickerDialog.show();
    }

    private void displayAppointmentDateCalendar() {
        Calendar calendar = Calendar.getInstance();
        final int yearCurrent = calendar.get(Calendar.YEAR);
        final int monthCurrent = calendar.get(Calendar.MONTH);
        final int dayCurrent = calendar.get(Calendar.DAY_OF_MONTH);
        Date today = calendar.getTime();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                Appointment.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                Date chooseDate = calendar.getTime();

                if (chooseDate.before(today)) {
                    etAppointmentDate.setText(null);
                    etAppointmentDate.setHint("Date");
                    etAppointmentDate.setError("Passed date cannot be choose!");
                    Toast.makeText(Appointment.this, "Passed date cannot be choose!", Toast.LENGTH_SHORT).show();
                    etAppointmentDate.requestFocus();
                    return;
                } else {
                    month = month + 1;
                    String date = day + "/" + month + "/" + year;
                    etAppointmentDate.setText(date);
                    etAppointmentDate.setError(null);
                }
            }
        }, yearCurrent, monthCurrent, dayCurrent);
        datePickerDialog.show();
    }

    private void displayBirthDateCalendar() {
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                Appointment.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month = month + 1;
                String date = day + "/" + month + "/" + year;
                etBirthDate.setText(date);
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

    private void makeAppointment() {
        String firstNameInput = etFirstName.getText().toString().trim();
        String lastNameInput = etLastName.getText().toString().trim();
        String genderInput = spinnerGender.getSelectedItem().toString().trim();
        String phoneInput = etPhone.getText().toString().trim();
        String birthDateInput = etBirthDate.getText().toString().trim();
        String streetAddressInput = etStreetAddress.getText().toString().trim();
        String postcodeInput = etPostcode.getText().toString().trim();
        String cityInput = etCity.getText().toString().trim();
        String emailInput = etEmail.getText().toString().trim();
        String symptomsInput = etSymptoms.getText().toString().trim();
        String otherInput = etOther.getText().toString().trim();
        String appointmentDateInput = etAppointmentDate.getText().toString().trim();
        String appointmentTimeInput = etAppointmentTime.getText().toString().trim();
        String status = "waiting for approval";
        String message = "";
        String report = "";

        // validation for all field
        if (firstNameInput.isEmpty() && lastNameInput.isEmpty()){
            etFirstName.setError("This field cannot be empty!");
            etFirstName.requestFocus();
            return;
        } else if (firstNameInput.isEmpty()) {
            firstNameInput = "";
        } else if (lastNameInput.isEmpty()) {
            lastNameInput = "";
        } else {
            etFirstName.setError(null);
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

        if (birthDateInput.equals("Date of Birth") || birthDateInput.isEmpty()){
            etBirthDate.setError("Please select date of birth!");
            etBirthDate.requestFocus();
            return;
        } else {
            etBirthDate.setError(null);
        }

        if (streetAddressInput.isEmpty()){
            etStreetAddress.setError("This field cannot be empty!");
            etStreetAddress.requestFocus();
            return;
        } else if (postcodeInput.isEmpty()) {
            postcodeInput = "";
        } else if (cityInput.isEmpty()) {
            cityInput = "";
        } else {
            etStreetAddress.setError(null);
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

        if (symptomsInput.isEmpty() && otherInput.isEmpty()){
            etSymptoms.setError("Please input at least one in symptoms or others field!");
            etSymptoms.requestFocus();
            return;
        } else if (symptomsInput.isEmpty()) {
            symptomsInput = "";
        } else if (otherInput.isEmpty()) {
            otherInput = "";
        } else {
            etSymptoms.setError(null);
        }

        if (appointmentDateInput.isEmpty()){
            etAppointmentDate.setError("Please choose appointment date!");
            etAppointmentDate.requestFocus();
            return;
        } else {
            etAppointmentDate.setError(null);
        }

        if (appointmentTimeInput.isEmpty()){
            etAppointmentTime.setError("Please choose appointment time!");
            etAppointmentTime.requestFocus();
            return;
        } else {
            etAppointmentTime.setError(null);
        }

        String name = firstNameInput + " " + lastNameInput;
        String address = streetAddressInput;
        if (postcodeInput.isEmpty()) {
            address  = streetAddressInput + ", " + cityInput;
        } else if (cityInput.isEmpty()) {
            address = streetAddressInput + ", " + postcodeInput;
        } else {
            address = streetAddressInput + ", " + postcodeInput + ", " + cityInput;
        }

        String finalAddress = address;
        String finalSymptoms = symptomsInput;
        String finalOther = otherInput;

        AlertDialog.Builder builder = new AlertDialog.Builder(Appointment.this);
        builder.setTitle("Appointment Confirmation!");
        builder.setMessage("Name: " + name + "\n" +
                            "Gender: " + genderInput + "\n" +
                            "Phone: " + phoneInput + "\n" +
                            "Date of Birth: " + birthDateInput + "\n" +
                            "Address: " + finalAddress + "\n" +
                            "Email: " + emailInput + "\n" +
                            "Symptoms: " + finalSymptoms + "\n" +
                            "Others: " + finalOther + "\n" +
                            "Appointment Date: " + appointmentDateInput + "\n" +
                            "Appointment Time: " + appointmentTimeInput + "\n\n" +
                            "Please make sure all the information input are correct. Are you sure want to make appointment?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                appointmentProgressBar.setVisibility(View.VISIBLE);

                Float convertBalance = Float.valueOf(balance);

                // split the date and time input
                deletePMInput = appointmentTimeInput.replace(" pm", ""); // remove pm from the text
                splitDateInput = appointmentDateInput.split("/"); // split date into day month and year
                splitTimeInput = deletePMInput.split(":"); // split time into hour and minute
                dateInput = splitDateInput[0];
                monthInput = splitDateInput[1];
                yearInput = splitDateInput[2];
                hourInput = splitTimeInput[0];
                minInput = splitTimeInput[1];

                // check all the appointment date and time with input from user and the appointment that has already booked
                for (int k = 0; k < size; k++) {
                    if (dateInput.equals(takenDate[k]) && monthInput.equals(takenMonth[k]) && yearInput.equals(takenYear[k]) && hourInput.equals(takenHour[k]) && minInput.equals(takenMin[k])){
                        AlertDialog.Builder builder = new AlertDialog.Builder(Appointment.this);
                        builder.setTitle("Attention!");
                        builder.setMessage("Appointment date and time selected is not available, please select another appointment date and/or time!");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                        appointmentProgressBar.setVisibility(View.GONE);
                        return;
                    }
                }

                // check if the account have enough balance or not
                if (convertBalance < 50) { // not enough balance
                    AlertDialog.Builder builder = new AlertDialog.Builder(Appointment.this);
                    builder.setTitle("Attention!");
                    builder.setMessage("Insufficient balance, please reload money before making appointment!");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    appointmentProgressBar.setVisibility(View.GONE);

                } else {
                    convertBalance = convertBalance - 50;
                    String convertTotalBalance = String.format("%.2f", convertBalance);
                    String appointmentID = UUID.randomUUID().toString().substring(0,10);
                    String getPushKey;

                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("balance" , convertTotalBalance);
                    mReference = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    mReference.updateChildren(childUpdates);

                    mReference =  FirebaseDatabase.getInstance().getReference("AppointmentMade").child(userID).push();
                    getPushKey = mReference.getKey();
                    AppointmentMade appointmentMade = new AppointmentMade(name, genderInput, phoneInput, birthDateInput, finalAddress, emailInput, finalSymptoms, finalOther, appointmentDateInput, appointmentTimeInput, status, appointmentID, message, getPushKey, report);
                    mReference.setValue(appointmentMade).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(Appointment.this);
                                builder.setTitle("Attention!");
                                builder.setMessage("Your appointment has been successfully sent, please wait for approval!");
                                builder.setCancelable(false);
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent a = new Intent(Appointment.this, Homepage.class);
                                        a.putExtra("totalBalance", convertTotalBalance);
                                        setResult(RESULT_OK, a);
                                        finish();
                                    }
                                });

                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                                appointmentProgressBar.setVisibility(View.GONE);

                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(Appointment.this);
                                builder.setTitle("Attention!");
                                builder.setMessage("Something went wrong, please try again later!");
                                builder.setCancelable(false);
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                });

                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                                appointmentProgressBar.setVisibility(View.GONE);
                            }
                        }
                    });
                }
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
