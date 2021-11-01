package com.example.clinicappointmentsystems;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AdminAppointmentDetails extends AppCompatActivity implements View.OnClickListener {
    private DatabaseReference mReference;

    private TextView tvAppointmentID, tvName, tvGender, tvPhone, tvBirthDate, tvAddress, tvEmail, tvSymptoms, tvAppointmentDate, tvAppointmentTime, tvStatus, tvReason, tvMessage, tvTotalPrice, tvDisplayPrice;
    private Button btnApprove, btnDisapprove;

    private String name, gender, phone, birthDate, address, email, symptoms, appointmentDate, appointmentTime, status, appointmentID, thisAppointmentUID, message, price, getPushKey; // fetch from database
    private String getMessage = "Your appointment has been approve, please make sure you come to the clinic in time!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_appointment_details);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        loadDatabase(); // load current appointment info (user ID)

        tvAppointmentID = (TextView) findViewById(R.id.tvAppointmentID);
        tvName = (TextView) findViewById(R.id.tvName);
        tvGender = (TextView) findViewById(R.id.tvGender);
        tvPhone = (TextView) findViewById(R.id.tvPhone);
        tvBirthDate = (TextView) findViewById(R.id.tvBirthDate);
        tvAddress = (TextView) findViewById(R.id.tvAddress);
        tvEmail = (TextView) findViewById(R.id.tvEmail);
        tvSymptoms = (TextView) findViewById(R.id.tvSymptoms);
        tvAppointmentDate = (TextView) findViewById(R.id.tvAppointmentDate);
        tvAppointmentTime = (TextView) findViewById(R.id.tvAppointmentTime);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        tvReason = (TextView) findViewById(R.id.tvReason);
        tvMessage = (TextView) findViewById(R.id.tvMessage);
        tvTotalPrice = (TextView) findViewById(R.id.tvTotalPrice);
        tvDisplayPrice = (TextView) findViewById(R.id.tvDisplayPrice);

        btnApprove = (Button) findViewById(R.id.btnApprove);
        btnApprove.setOnClickListener(this);
        btnDisapprove = (Button) findViewById(R.id.btnDisapprove);
        btnDisapprove.setOnClickListener(this);


        Intent i = getIntent();
        name = i.getStringExtra("name");
        gender = i.getStringExtra("gender");
        phone = i.getStringExtra("phone");
        birthDate = i.getStringExtra("birthDate");
        address = i.getStringExtra("address");
        email = i.getStringExtra("email");
        symptoms = i.getStringExtra("symptoms");
        appointmentDate = i.getStringExtra("appointmentDate");
        appointmentTime = i.getStringExtra("appointmentTime");
        status = i.getStringExtra("status");
        appointmentID = i.getStringExtra("appointmentID");
        message = i.getStringExtra("message");
        price = i.getStringExtra("price");
        getPushKey = i.getStringExtra("pushKey");

        tvAppointmentID.setTextColor(Color.parseColor("#005FFF"));
        tvAppointmentID.setText("# " + appointmentID + " ");
        tvName.setText(name);
        tvGender.setText(gender);
        tvPhone.setText(phone);
        tvBirthDate.setText(birthDate);
        tvAddress.setText(address);
        tvEmail.setText(email);
        tvSymptoms.setText(symptoms);
        tvAppointmentDate.setText(appointmentDate);
        tvAppointmentTime.setText(appointmentTime);

        tvReason.setVisibility(View.INVISIBLE);
        tvMessage.setVisibility(View.INVISIBLE);
        tvTotalPrice.setVisibility(View.INVISIBLE);
        tvDisplayPrice.setVisibility(View.INVISIBLE);
        changeStatusColor(); // change text color and visibility button based on status

    }

    private void changeStatusColor() {
        switch (status) {
            case "waiting for approval":
                tvStatus.setTextColor(Color.parseColor("#D500FF"));
                break;

            case "approve":
                tvReason.setVisibility(View.VISIBLE);
                tvMessage.setVisibility(View.VISIBLE);
                tvMessage.setText(message);
                btnApprove.setEnabled(false);
                tvStatus.setTextColor(Color.parseColor("#00AF41"));
                break;

            case "disapprove":
                tvReason.setVisibility(View.VISIBLE);
                tvMessage.setVisibility(View.VISIBLE);
                tvMessage.setText(message);
                btnDisapprove.setEnabled(false);
                tvStatus.setTextColor(Color.parseColor("#FF8B00"));
                break;

            case "not paid":
                btnApprove.setEnabled(false);
                btnDisapprove.setEnabled(false);
                tvTotalPrice.setVisibility(View.VISIBLE);
                tvDisplayPrice.setVisibility(View.VISIBLE);
                tvTotalPrice.setTextColor(Color.parseColor("#FF0000"));
                tvTotalPrice.setText("RM" + price);
                tvStatus.setTextColor(Color.parseColor("#FF0000"));
                break;

            case "paid":
                btnApprove.setEnabled(false);
                btnDisapprove.setEnabled(false);
                tvTotalPrice.setVisibility(View.VISIBLE);
                tvDisplayPrice.setVisibility(View.VISIBLE);
                tvTotalPrice.setTextColor(Color.parseColor("#7DFF00"));
                tvTotalPrice.setText("RM" + price);
                tvStatus.setTextColor(Color.parseColor("#7DFF00"));
                break;
        }

        tvStatus.setText(status);
    }

    private void loadDatabase() {
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

                                if (appointmentMade != null && appointmentMade.id.equals(appointmentID)) {
                                    thisAppointmentUID = userID;
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
                Toast.makeText(AdminAppointmentDetails.this, "Appointment made information not retrieved!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnApprove:
                runApprove();
                break;

            case R.id.btnDisapprove:
                runDisapprove();
                break;

            default:
                break;
        }
    }

    private void runApprove() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AdminAppointmentDetails.this);
        builder.setTitle("Attention!");
        builder.setMessage("Are you sure want to approve this appointment?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("status" , "approve");
                childUpdates.put("message" , getMessage);
                mReference = FirebaseDatabase.getInstance().getReference().child("AppointmentMade").child(thisAppointmentUID).child(getPushKey);
                mReference.updateChildren(childUpdates);

                tvReason.setVisibility(View.VISIBLE);
                tvMessage.setVisibility(View.VISIBLE);
                tvMessage.setText(getMessage);
                btnDisapprove.setEnabled(true);
                btnApprove.setEnabled(false);
                tvStatus.setText("approve");
                tvStatus.setTextColor(Color.parseColor("#00AF41"));
                Toast.makeText(AdminAppointmentDetails.this, "This appointment has been approve successfully!", Toast.LENGTH_SHORT).show();
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

    private void runDisapprove() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View disapproveView = inflater.inflate(R.layout.custom_radio_disapprove, null, false);
        final RadioGroup radioGroupDisapprove = (RadioGroup) disapproveView.findViewById(R.id.radioGroupDisapprove);

        AlertDialog.Builder builder3 = new AlertDialog.Builder(AdminAppointmentDetails.this);
        builder3.setView(disapproveView);
        builder3.setTitle("Select one reject message to disapprove this appointment!");
        builder3.setCancelable(false);
        builder3.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try { // will cause crash if admin not select any radio button
                    int selectedID = radioGroupDisapprove.getCheckedRadioButtonId();
                    RadioButton selectedRadioBtn = (RadioButton) disapproveView.findViewById(selectedID);
                    getMessage = selectedRadioBtn.getText().toString().trim();

                } catch (Exception e) {
                    getMessage = "";
                }


                if (getMessage.isEmpty()) {
                    Toast.makeText(AdminAppointmentDetails.this, "Select one reject message before disapproving this appointment!", Toast.LENGTH_SHORT).show();

                } else {
                    Map<String, Object> childUpdates = new HashMap<>(); // change status with message
                    childUpdates.put("status" , "disapprove");
                    childUpdates.put("message" , getMessage);
                    mReference = FirebaseDatabase.getInstance().getReference().child("AppointmentMade").child(thisAppointmentUID).child(getPushKey);
                    mReference.updateChildren(childUpdates);

                    tvReason.setVisibility(View.VISIBLE);
                    tvMessage.setVisibility(View.VISIBLE);
                    tvMessage.setText(getMessage);
                    btnDisapprove.setEnabled(false);
                    btnApprove.setEnabled(true);
                    tvStatus.setText("disapprove");
                    tvStatus.setTextColor(Color.parseColor("#FF8B00"));
                    Toast.makeText(AdminAppointmentDetails.this, "This appointment has been disapprove successfully!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder3.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        AlertDialog alertDialog3 = builder3.create();
        alertDialog3.show();
    }
}