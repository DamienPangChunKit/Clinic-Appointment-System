package com.example.clinicappointmentsystems;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditAdminProfile extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private DatabaseReference mReference;
    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;

    private Spinner spinnerGender, spinnerRole;
    private EditText etAdminUsername, etAdminBirthDate, etAdminAddress, etAdminPhone;
    private Button btnSaveAdminProfile;
    private ProgressBar editAdminProfileProgressBar;
    private ImageView imgAdminPicture, imgLoadPhoto;
    public Uri imageURI;

    private String address, balance, birthDate, email, gender, image, password, phone, userType, username, role, getThisUserID, getThisUsername, getThisRole;
    private String getAddress, getGender, getBirthDate, getBalance, getEmail, getUserID, getImage, getPassword, getPhone, getUserType, getUsername;
    public static String toRecipentEmail, bySenderUsername, bySenderRole;
    ArrayList<String> userInfo = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_admin_profile);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        loadDatabase();

        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();

        Intent i = getIntent();
        address = i.getStringExtra("address");
        balance = i.getStringExtra("balance");
        birthDate = i.getStringExtra("birthDate");
        email = i.getStringExtra("email");
        gender = i.getStringExtra("gender");
        image = i.getStringExtra("image");
        password = i.getStringExtra("password");
        phone = i.getStringExtra("phone");
        userType = i.getStringExtra("userType");
        username = i.getStringExtra("username");
        role = i.getStringExtra("role");

        spinnerGender = (Spinner) findViewById(R.id.spinnerGender);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gender, R.layout.spinner_text);
        adapter.setDropDownViewResource(R.layout.custom_drop_down_spinner);
        spinnerGender.setPadding(10, 10, 10, 10);
        spinnerGender.setAdapter(adapter);
        spinnerGender.setSelection(adapter.getPosition(gender));
        spinnerGender.setOnItemSelectedListener(this);

        spinnerRole = (Spinner) findViewById(R.id.spinnerRole);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.admin_role, R.layout.spinner_text);
        adapter2.setDropDownViewResource(R.layout.custom_drop_down_spinner);
        spinnerRole.setPadding(10, 10, 10, 10);
        spinnerRole.setAdapter(adapter2);
        spinnerRole.setSelection(adapter2.getPosition(role));
        spinnerRole.setOnItemSelectedListener(this);

        etAdminUsername = (EditText) findViewById(R.id.etAdminUsername);
        etAdminBirthDate = (EditText) findViewById(R.id.etAdminDateBirth);
        etAdminAddress = (EditText) findViewById(R.id.etAdminAddress);
        etAdminPhone = (EditText) findViewById(R.id.etAdminPhone);

        etAdminUsername.setText(username);
        etAdminBirthDate.setText(birthDate);
        etAdminAddress.setText(address);
        etAdminPhone.setText(phone);

        etAdminBirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayBirthDateCalendar();
            }
        });

        btnSaveAdminProfile = (Button) findViewById(R.id.btnSaveAdminProfile);
        btnSaveAdminProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAdminProfile();
            }
        });

        editAdminProfileProgressBar = (ProgressBar) findViewById(R.id.editAdminProfileProgressBar);
        imgAdminPicture = (ImageView) findViewById(R.id.imgAdminPicture);
        imgAdminPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePicture();
            }
        });

        imgLoadPhoto = (ImageView) findViewById(R.id.imgLoadPhoto);
        imgLoadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadCurrentAdminPicture();
            }
        });
    }

    private void loadCurrentAdminPicture() {
        mReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userInfo.get(10)).child("image");
        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String imageLink = snapshot.getValue(String.class);
                Picasso.get().load(imageLink).into(imgAdminPicture);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditAdminProfile.this, "Error loading profile picture!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) { // upload image
            imageURI = data.getData();
            imgAdminPicture.setImageURI(imageURI);
            uploadImage();
        }
    }

    private void choosePicture() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, 1);
    }

    private void uploadImage() {
        final ProgressDialog uploadImgDialog = new ProgressDialog(this);
        uploadImgDialog.setTitle("Uploading image, please wait...");
        uploadImgDialog.show();

        final String randomKey = UUID.randomUUID().toString();
        StorageReference stoRef = mStorageReference.child("images/" + randomKey);

        stoRef.putFile(imageURI)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        uploadImgDialog.dismiss();
                        Snackbar.make(findViewById(android.R.id.content), "Image upload successfully!.", Snackbar.LENGTH_SHORT).show();

                        Map<String, Object> childUpdates = new HashMap<>();
                        stoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();
                                childUpdates.put("image" , downloadUrl);
                                mReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userInfo.get(10));
                                mReference.updateChildren(childUpdates);
                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        uploadImgDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Failed to upload image!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progressPercentage = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        uploadImgDialog.setMessage("Progress: " + (int)progressPercentage + "%");
                    }
                });
    }

    private void loadDatabase() {
        getThisUserID = FirebaseAuth.getInstance().getCurrentUser().getUid(); // this uid is current account who login
        mReference = FirebaseDatabase.getInstance().getReference("Users");
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot allUserID : snapshot.getChildren()) {
                    String allID = allUserID.getKey();

                    mReference.child(allID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ManageAdminHistory manageAdminHistory = snapshot.getValue(ManageAdminHistory.class);

                            if (manageAdminHistory != null && manageAdminHistory.email.equals(email)) {
                                getUsername = manageAdminHistory.username;
                                getPhone = manageAdminHistory.phone;
                                getEmail = manageAdminHistory.email;
                                getPassword = manageAdminHistory.password;
                                getBirthDate = manageAdminHistory.birthDate;
                                getGender = manageAdminHistory.gender;
                                getAddress = manageAdminHistory.address;
                                getBalance = manageAdminHistory.balance;
                                getUserType = manageAdminHistory.userType;
                                getImage = manageAdminHistory.image;

                                userInfo.add(0, getUsername);
                                userInfo.add(1, getPhone);
                                userInfo.add(2, getEmail);
                                userInfo.add(3, getPassword);
                                userInfo.add(4, getBirthDate);
                                userInfo.add(5, getGender);
                                userInfo.add(6, getAddress);
                                userInfo.add(7, getBalance);
                                userInfo.add(8, getUserType);
                                userInfo.add(9, getImage);
                                userInfo.add(10, allID);

                            }

                            if (manageAdminHistory != null && allID.equals(getThisUserID)) {
                                getThisUsername = manageAdminHistory.username;
                                getThisRole = manageAdminHistory.getRole();
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
                Toast.makeText(EditAdminProfile.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayBirthDateCalendar() {
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                EditAdminProfile.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month = month + 1;
                String date = day + "/" + month + "/" + year;
                etAdminBirthDate.setText(date);
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    private void saveAdminProfile() {
        String usernameInput = etAdminUsername.getText().toString().trim();
        String roleInput = spinnerRole.getSelectedItem().toString().trim();
        String genderInput = spinnerGender.getSelectedItem().toString().trim();
        String birthDateInput = etAdminBirthDate.getText().toString().trim();
        String addressInput = etAdminAddress.getText().toString().trim();
        String phoneInput = etAdminPhone.getText().toString().trim();
        toRecipentEmail = userInfo.get(2); // store the admin email
        bySenderUsername = getThisUsername; // store this login account username
        bySenderRole = getThisRole; // store this login account role

        // check validation
        if (usernameInput.isEmpty()){
            etAdminUsername.setError("This field cannot be empty!");
            etAdminUsername.requestFocus();
            return;
        } else if (usernameInput.length() < 6){
            etAdminUsername.setError("Username must contain at least 6 character!");
            etAdminUsername.requestFocus();
            return;
        } else if (usernameInput.length() > 15){
            etAdminUsername.setError("Username too long!");
            etAdminUsername.requestFocus();
            return;
        } else {
            etAdminUsername.setError(null);
        }

        if (birthDateInput.equals("Date of Birth") || birthDateInput.isEmpty()){
            etAdminBirthDate.setError("Please select date of birth!");
            etAdminBirthDate.requestFocus();
            return;
        } else {
            etAdminBirthDate.setError(null);
        }

        if (addressInput.isEmpty()){
            etAdminAddress.setError("This field cannot be empty!");
            etAdminAddress.requestFocus();
            return;
        } else {
            etAdminAddress.setError(null);
        }

        if (phoneInput.isEmpty()){
            etAdminPhone.setError("This field cannot be empty!");
            etAdminPhone.requestFocus();
            return;
        } else if (phoneInput.charAt(0) != '0' |
                phoneInput.charAt(1) != '1' |
                phoneInput.length() < 10 |
                phoneInput.length() > 11) {
            etAdminPhone.setError("Please input phone number format as 0123456789");
            etAdminPhone.requestFocus();
            return;
        } else {
            etAdminPhone.setError(null);
        }

        SendEmailOnUpdate sendEmailOnUpdate = new SendEmailOnUpdate(this); // java mail api

        AlertDialog.Builder builder = new AlertDialog.Builder(EditAdminProfile.this);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure want to edit admin profile?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Map<String, Object> childUpdates = new HashMap<>(); // update all info
                childUpdates.put("username" , usernameInput);
                childUpdates.put("role" , roleInput);
                childUpdates.put("gender" , genderInput);
                childUpdates.put("birthDate" , birthDateInput);
                childUpdates.put("address" , addressInput);
                childUpdates.put("phone" , phoneInput);
                mReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userInfo.get(10));
                mReference.updateChildren(childUpdates);

                sendEmailOnUpdate.execute(); // starting to send email

                finish();
                Toast.makeText(EditAdminProfile.this, "Edit admin profile successfully!", Toast.LENGTH_SHORT).show();
                editAdminProfileProgressBar.setVisibility(View.GONE);
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