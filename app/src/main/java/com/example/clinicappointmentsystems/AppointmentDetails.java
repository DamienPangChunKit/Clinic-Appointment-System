package com.example.clinicappointmentsystems;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tomer.fadingtextview.FadingTextView;

import java.util.HashMap;
import java.util.Map;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class AppointmentDetails extends AppCompatActivity {
    private DatabaseReference mReference, reference2, reference3, reference4;
    private StorageReference mStorageReference;
    private FirebaseUser user;

    private TextView tvAppointmentID, tvName, tvGender, tvPhone, tvBirthDate, tvAddress, tvEmail, tvSymptoms, tvAppointmentDate, tvAppointmentTime, tvStatus, tvReason, tvMessage, tvTotalPrice, tvDisplayPrice;
    private FadingTextView fading1;
    private Button btnPaidGenerateReport;

    private String userID, pushKey, appointmentID, name, gender, phone, birthDate, address, email, symptoms, appointmentDate, appointmentTime, status, price, message, balance;
    private String[] stringArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_details);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

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
        fading1 = (FadingTextView) findViewById(R.id.tvFadingText1);
        btnPaidGenerateReport = (Button) findViewById(R.id.btnPaidGenerateReport);
        btnPaidGenerateReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnPaidGenerateReport.getText().toString().toLowerCase().equals("contact now")) {
                    phoneCall();

                } else {
                    payOrReport();
                }

            }
        });

        tvReason.setVisibility(View.INVISIBLE);
        tvMessage.setVisibility(View.INVISIBLE);
        tvTotalPrice.setVisibility(View.INVISIBLE);
        tvDisplayPrice.setVisibility(View.INVISIBLE);

        Intent i = getIntent();
        appointmentID = i.getStringExtra("id");
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
        price = i.getStringExtra("price");
        message = i.getStringExtra("message");

        loadDatabase();

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

        changePaymentWarning();
        changeStatusColor();

    }

    private void loadDatabase() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();

        // load current user to get balance
        mReference = FirebaseDatabase.getInstance().getReference("Users").child(userID);
        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);

                balance = userProfile.getBalance();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        // load appointment to get pushkey
        mReference = FirebaseDatabase.getInstance().getReference("AppointmentMade").child(userID);
        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    AppointmentMade appointmentMade = dataSnapshot.getValue(AppointmentMade.class);

                    if (appointmentMade != null && appointmentMade.id.equals(appointmentID)) {
                        pushKey = appointmentMade.getPushKey();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AppointmentDetails.this, "Appointment made information not retrieved!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void phoneCall() {
        String mobileNumber = "0179946732";
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel: " + mobileNumber));
        startActivity(intent);
    }

    private void changeStatusColor() {
        switch (status) {
            case "waiting for approval":
                btnPaidGenerateReport.setVisibility(View.INVISIBLE);
                btnPaidGenerateReport.setEnabled(false);
                tvStatus.setTextColor(Color.parseColor("#D500FF"));
                break;

            case "approve":
                tvReason.setVisibility(View.VISIBLE);
                tvMessage.setVisibility(View.VISIBLE);
                tvMessage.setText(message);
                btnPaidGenerateReport.setVisibility(View.INVISIBLE);
                btnPaidGenerateReport.setEnabled(false);
                tvStatus.setTextColor(Color.parseColor("#00AF41"));
                break;

            case "disapprove":
                tvReason.setVisibility(View.VISIBLE);
                tvMessage.setVisibility(View.VISIBLE);
                tvMessage.setText(message);
                btnPaidGenerateReport.setText("contact now");
                tvStatus.setTextColor(Color.parseColor("#FF8B00"));
                break;

            case "not paid":
                tvTotalPrice.setVisibility(View.VISIBLE);
                tvDisplayPrice.setVisibility(View.VISIBLE);
                tvTotalPrice.setTextColor(Color.parseColor("#FF0000"));
                tvTotalPrice.setText("RM" + price);
                tvStatus.setTextColor(Color.parseColor("#FF0000"));
                break;

            case "paid":
                tvTotalPrice.setVisibility(View.VISIBLE);
                tvDisplayPrice.setVisibility(View.VISIBLE);
                tvTotalPrice.setTextColor(Color.parseColor("#7DFF00"));
                tvTotalPrice.setText("RM" + price);
                btnPaidGenerateReport.setText("generate report");
                tvStatus.setTextColor(Color.parseColor("#7DFF00"));
                break;
        }

        tvStatus.setText(status);
    }

    private void changePaymentWarning() {
        stringArray = getResources().getStringArray(R.array.appointment_details_payment);

        switch (status) {
            case "waiting for approval":
                stringArray[0] = "Please wait for this appointment to get approval!";
                fading1.setTextColor(Color.parseColor("#D500FF"));
                break;
            case "approve":
                stringArray[0] = "Your appointment has been approved!";
                fading1.setTextColor(Color.parseColor("#DDFF00"));
                break;
            case "disapprove":
                stringArray[0] = "Your appointment has been disapproved, please contact admin for more information!";
                fading1.setTextColor(Color.parseColor("#FF8B00"));
                break;
            case "not paid":
                stringArray[0] = "Your medical fee not paid yet!";
                fading1.setTextColor(Color.parseColor("#FF0000"));
                break;
            case "paid":
                stringArray[0] = "Your medical fee has been paid, thank you!";
                fading1.setTextColor(Color.parseColor("#7DFF00"));
                break;
        }

        fading1.setTexts(new String[]{stringArray[0]});
    }

    private void payOrReport() {
        String getBtnText = btnPaidGenerateReport.getText().toString().trim();

        if (getBtnText.toLowerCase().equals("pay now")) {
            payBill();
        } else if (getBtnText.toLowerCase().equals("generate report")) {
            generateReport();
        }

    }

    private void payBill() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AppointmentDetails.this);
        builder.setTitle("Attention!");
        builder.setMessage("Are you sure want to pay this medical fee?" + "\n" + "Total Price: RM" + price);
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Float convertBalance = Float.valueOf(balance);
                Float convertPrice = Float.valueOf(price);
                Float remainingBalance = convertBalance - convertPrice;

                if (remainingBalance < 0) {
                    Snackbar.make(findViewById(android.R.id.content), "Please reload money before paying the bill.", Snackbar.LENGTH_SHORT).show();

                } else {
                    remainingBalance = remainingBalance + 50; // deposit fee return
                    String convertTotalBalance = String.format("%.2f", remainingBalance);

                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("balance" , convertTotalBalance);
                    reference2 = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
                    reference2.updateChildren(childUpdates);

                    Map<String, Object> childUpdates2 = new HashMap<>();
                    childUpdates2.put("message" , "Payment done!");
                    childUpdates2.put("status" , "paid");
                    reference3 = FirebaseDatabase.getInstance().getReference().child("AppointmentMade").child(userID).child(pushKey);
                    reference3.updateChildren(childUpdates2);

                    tvStatus.setTextColor(Color.parseColor("#7DFF00"));
                    tvStatus.setText("paid");
                    tvTotalPrice.setTextColor(Color.parseColor("#7DFF00"));
                    tvTotalPrice.setText("RM" + price);
                    btnPaidGenerateReport.setText("generate report");

                    Snackbar.make(findViewById(android.R.id.content), "Pay medical fee successfully!", Snackbar.LENGTH_SHORT).show();

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

    private void generateReport() {
        reference4 = FirebaseDatabase.getInstance().getReference().child("AppointmentMade").child(userID);
        reference4.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    AppointmentMade appointmentMade = dataSnapshot.getValue(AppointmentMade.class);

                    if (appointmentMade != null && !appointmentMade.getReport().isEmpty() && appointmentMade.getId().equals(appointmentID)) {
                        mStorageReference = FirebaseStorage.getInstance().getReference().child("pdf/" + appointmentMade.getReport());
                        mStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String pdfURI = uri.toString();
                                String filename = appointmentMade.getReport();

                                downloadPDF(AppointmentDetails.this, filename, DIRECTORY_DOWNLOADS, pdfURI ); // standard directory that downloaded by user but in system file
                            }

                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(findViewById(android.R.id.content), "Failed to fetch pdf file name!", Snackbar.LENGTH_SHORT).show();
                            }
                        });

                    } else if (appointmentMade != null && appointmentMade.getReport().isEmpty() && appointmentMade.getId().equals(appointmentID)) {
                        Snackbar.make(findViewById(android.R.id.content), "Please wait for the doctor to generate the report!", Snackbar.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AppointmentDetails.this, "Appointment made information not retrieved!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadPDF(Context context, String filename, String destinationDirectory, String pdfURI) {
        Uri uri = Uri.parse(pdfURI);
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, destinationDirectory, filename);

        downloadManager.enqueue(request);

        AlertDialog.Builder builder = new AlertDialog.Builder(AppointmentDetails.this);
        builder.setTitle("File path");
        builder.setMessage(destinationDirectory + " file of the system!");
        builder.setCancelable(false);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}