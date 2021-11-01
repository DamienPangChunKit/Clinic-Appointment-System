package com.example.clinicappointmentsystems;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MedicalReport extends AppCompatActivity {
    DatabaseReference mReference;

    private TextView tvAppointmentID, tvDoctor, tvIllness, tvName, tvInstruction, tvDoctorSignature, tvDate;
    private CheckBox cbMale, cbFemale;
    private ImageView imgGenerate, imgUpload;

    private String appointmentID, temp, doctor, illness, name, gender, instruction;

    private Uri imageUri;
    // layout
    private ConstraintLayout layout_medicalReport;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_report);

        layout_medicalReport = (ConstraintLayout) findViewById(R.id.layout_medicalReport);

        tvAppointmentID = (TextView) findViewById(R.id.tvAppointmentID);
        tvDoctor = (TextView) findViewById(R.id.tvDoctor);
        tvIllness = (TextView) findViewById(R.id.tvSymptomsAndOthers);
        tvName = (TextView) findViewById(R.id.tvName);
        tvInstruction = (TextView) findViewById(R.id.tvInstruction);
        tvDoctorSignature = (TextView) findViewById(R.id.tvDoctorSignature);
        tvDate = (TextView) findViewById(R.id.tvDate);
        cbMale = (CheckBox) findViewById(R.id.checkMale);
        cbFemale = (CheckBox) findViewById(R.id.checkFemale);
        imgGenerate = (ImageView) findViewById(R.id.imgGenerate);
        imgGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateReport();
            }
        });
        imgUpload = (ImageView) findViewById(R.id.imgUpload);
        imgUpload.setEnabled(false);
        imgUpload.setVisibility(View.INVISIBLE);
        imgUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePDF();
            }
        });

        Intent i = getIntent();
        appointmentID = i.getStringExtra("appointmentIDInput");
        temp = i.getStringExtra("tempInput");
        doctor = i.getStringExtra("doctorInput");
        illness = i.getStringExtra("illnessInput");
        name = i.getStringExtra("nameInput");
        gender = i.getStringExtra("genderInput");
        instruction = i.getStringExtra("instructionInput");

        tvAppointmentID.setText("#" + appointmentID);
        tvDoctor.setText(doctor);
        tvIllness.setText(illness);
        tvName.setText(name);
        tvInstruction.setText(instruction);

        if (gender.toLowerCase().equals("male")) {
            cbMale.setChecked(true);
            cbFemale.setChecked(false);
            cbFemale.setEnabled(false);

        } else if (gender.toLowerCase().equals("female")) {
            cbFemale.setChecked(true);
            cbMale.setChecked(false);
            cbMale.setEnabled(false);

        }

        // Symptoms field
        if (illness.contains("\n")) {
            illness = illness.replace("\n", "\n- ");
        }
        illness = illness.replace(", ", "\n- ");
        if (Double.parseDouble(temp) >= 36.1 && Double.parseDouble(temp) <= 37.2) {
            tvIllness.setText("- " + temp + "°C (normal body temperature)\n" + "- " + illness);
        } else {
            tvIllness.setText("- " + temp + "°C (abnormal body temperature)\n" + "- " + illness);
        }
        tvIllness.setMovementMethod(new ScrollingMovementMethod());


        // Instruction field
        if (instruction.contains("\n")) {
            instruction = instruction.replace("\n", "\n- ");
        }
        instruction = instruction.replace(", ", "\n- ");
        tvInstruction.setText("- " + instruction);
        tvInstruction.setMovementMethod(new ScrollingMovementMethod());

        if (doctor.equals("Dr.Damien")) {
            tvDoctorSignature.setText("Damien");
        } else if (doctor.equals("Dr.Koh")) {
            tvDoctorSignature.setText("Koh");
        }


        // get today date to display in report
        getTodayDate();
    }

    private void generateReport() {
        imgGenerate.setEnabled(false);
        imgGenerate.setVisibility(View.INVISIBLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(MedicalReport.this);
        builder.setTitle("Are you sure want to generate this report?");
        builder.setMessage("This report will be store in a folder with PDF format!");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                printReport();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                imgGenerate.setEnabled(true);
                imgGenerate.setVisibility(View.VISIBLE);

                dialogInterface.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void printReport() {
        mBitmap = loadBitmap(layout_medicalReport, layout_medicalReport.getWidth(), layout_medicalReport.getHeight());
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        float width = displayMetrics.widthPixels;
        float height = displayMetrics.heightPixels;
        int convertWidth = (int) width;
        int convertHeight = (int) height;

        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(convertWidth, convertHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        canvas.drawPaint(paint);
        mBitmap = Bitmap.createScaledBitmap(mBitmap, convertWidth, convertHeight, true);

        canvas.drawBitmap(mBitmap, 0, 0, null);
        pdfDocument.finishPage(page);


        // generate report to the file
        String ramdomID = "" + System.currentTimeMillis();
        String fileName = ramdomID + ".pdf";
        String path = "/storage/emulated/0/Android/data/com.example.clinicappointmentsystems/files/" + fileName;
        File file = new File(path);

        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            pdfDocument.close();
            Toast.makeText(MedicalReport.this, "Download successfully!", Toast.LENGTH_SHORT).show();
            openPDF(file);

            imgUpload.setEnabled(true);
            imgUpload.setVisibility(View.VISIBLE);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MedicalReport.this, "Something went wrong when download!", Toast.LENGTH_SHORT).show();

        }
    }

    private Bitmap loadBitmap(View view, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    private void openPDF(File filepath) {
        File file = new File(String.valueOf(filepath));

        if (file.exists()) {
            Intent a = new Intent(Intent.ACTION_VIEW);
            Uri pdfURI = FileProvider.getUriForFile(MedicalReport.this, BuildConfig.APPLICATION_ID + ".provider", file);
            a.setDataAndType(pdfURI, "application/pdf");
            a.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            a.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            AlertDialog.Builder builder = new AlertDialog.Builder(MedicalReport.this);
            builder.setTitle("File path");
            builder.setMessage(file.toString());
            builder.setCancelable(false);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            try {
                startActivity(a);

            } catch (ActivityNotFoundException e) {
                Toast.makeText(MedicalReport.this, "Something went wrong when view pdf!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void choosePDF() {
        String sPath = Environment.getExternalStorageDirectory() + "/storage/emulated/0/Android/data/com.example.clinicappointmentsystems/files/";
        Uri uri = Uri.parse(sPath);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(uri, "*/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) { // upload pdf
            imageUri = data.getData();
            uploadToDatabase();
        }
    }

    private void uploadToDatabase() {
        final String ramdomID = "" + System.currentTimeMillis();
        final ProgressDialog uploadPDFDialog = new ProgressDialog(this);
        uploadPDFDialog.setTitle("Uploading PDF, please wait...");
        uploadPDFDialog.show();

        // upload pdf to firebase
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference filepath = storageReference.child("pdf/" + ramdomID + ".pdf");
        filepath.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        uploadPDFDialog.dismiss();
                        Snackbar.make(findViewById(android.R.id.content), "PDF upload successfully!.", Snackbar.LENGTH_SHORT).show();

                        Map<String, Object> childUpdates = new HashMap<>();
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // check for the specific appointment and rewrite the report
                                mReference = FirebaseDatabase.getInstance().getReference().child("AppointmentMade");
                                mReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot allUserID : snapshot.getChildren()) {
                                            String userID = allUserID.getKey();

                                            mReference.child(userID).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    for (DataSnapshot allPushKey : snapshot.getChildren()) {
                                                        String appointmentPushKey = allPushKey.getKey();

                                                        mReference.child(userID).child(appointmentPushKey).addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                AdminHomepageHistory adminHomepageHistory = snapshot.getValue(AdminHomepageHistory.class);

                                                                if (adminHomepageHistory.getId().equals(appointmentID)) {
                                                                    //final String downloadUrl = uri.toString();
                                                                    childUpdates.put("report" , ramdomID + ".pdf");
                                                                    mReference.child(userID).child(appointmentPushKey).updateChildren(childUpdates);
                                                                    Toast.makeText(getApplicationContext(), "Upload success!", Toast.LENGTH_SHORT).show();

                                                                    Intent i = new Intent(MedicalReport.this, AdminHomepage.class);
                                                                    startActivity(i);
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

                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        uploadPDFDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Failed to upload image!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progressPercentage = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        uploadPDFDialog.setMessage("Progress: " + (int)progressPercentage + "%");
                    }
                });
    }

    private void getTodayDate() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String formateDate = simpleDateFormat.format(date);

        tvDate.setText(formateDate);
    }
}