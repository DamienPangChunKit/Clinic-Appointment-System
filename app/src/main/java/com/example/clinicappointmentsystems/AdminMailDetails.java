package com.example.clinicappointmentsystems;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AdminMailDetails extends AppCompatActivity {
    private DatabaseReference mReference;

    private ImageView imgMailStatus;
    private TextView tvUserID, tvName, tvEmail, tvPhone, tvTitle, tvMessage, tvDateTime, tvStatus, tvMailID;
    private Button btnDone;

    private String userID, name, email, phone, title, message, dateTime, status, mailID, pushKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_mail_details);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        imgMailStatus = (ImageView) findViewById(R.id.imgMailStatus);
        tvMailID = (TextView) findViewById(R.id.tvMailID);
        tvUserID = (TextView) findViewById(R.id.tvUserID);
        tvName = (TextView) findViewById(R.id.tvName);
        tvEmail = (TextView) findViewById(R.id.tvEmail);
        tvPhone = (TextView) findViewById(R.id.tvPhone);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvMessage = (TextView) findViewById(R.id.tvMessage);
        tvDateTime = (TextView) findViewById(R.id.tvDateTime);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        btnDone = (Button) findViewById(R.id.btnDoneStatus);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doneMail();
            }
        });

        Intent i = getIntent();
        name = i.getStringExtra("name");
        email = i.getStringExtra("email");
        phone = i.getStringExtra("phone");
        title = i.getStringExtra("title");
        message = i.getStringExtra("message");
        dateTime = i.getStringExtra("dateTime");
        status = i.getStringExtra("status");
        mailID = i.getStringExtra("mailID");

        tvMailID.setText("#" + mailID);
        tvName.setText(name);
        tvEmail.setText(email);
        tvPhone.setText(phone);
        tvTitle.setText(title);
        tvMessage.setText(message);
        tvDateTime.setText(dateTime);
        tvStatus.setText(status);

        loadDatabase(mailID);

        if (status.equals("pending")) {
            imgMailStatus.setColorFilter(Color.rgb(255, 0, 0));
            tvStatus.setTextColor(Color.parseColor("#FF0000"));
        } else {
            imgMailStatus.setColorFilter(Color.rgb(0, 175, 65));
            tvStatus.setTextColor(Color.parseColor("#00AF41"));
        }

        if (status.equals("done")) {
            btnDone.setEnabled(false);
            btnDone.setVisibility(View.INVISIBLE);
        }
    }

    private void doneMail() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AdminMailDetails.this);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure this action done?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Map<String, Object> childUpdates = new HashMap<>(); // update all info
                childUpdates.put("status" , "done");
                mReference = FirebaseDatabase.getInstance().getReference().child("ContactUs").child(userID).child(pushKey);
                mReference.updateChildren(childUpdates);

                tvStatus.setText("done");
                imgMailStatus.setColorFilter(Color.rgb(0, 175, 65));
                tvStatus.setTextColor(Color.parseColor("#00AF41"));

                Toast.makeText(AdminMailDetails.this, "Action has been set to done!", Toast.LENGTH_SHORT).show();
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

    private void loadDatabase(String mailID) {
        mReference = FirebaseDatabase.getInstance().getReference("ContactUs");
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot allUserID : snapshot.getChildren()) {
                    String allID = allUserID.getKey();

                    mReference.child(allID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                AdminMailHistory adminMailHistory = dataSnapshot.getValue(AdminMailHistory.class);

                                if (adminMailHistory != null && adminMailHistory.getMailID().equals(mailID)) {
                                    userID = allID;
                                    tvUserID.setText(userID);
                                    pushKey = adminMailHistory.getPushKey();
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
                Toast.makeText(AdminMailDetails.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}