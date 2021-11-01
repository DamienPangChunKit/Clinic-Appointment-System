package com.example.clinicappointmentsystems;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MyAdminProfile extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private FirebaseUser user;
    private DatabaseReference mReference;
    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;

    private EditText etAdminUsername, etAdminBirthDate, etAdminAddress, etAdminPhone, etAdminEmail, etAdminNewPassword;
    private Button btnSave;
    private Spinner spinnerGender, spinnerRole;
    private ImageView imgAdminPicture;
    private ProgressBar editMyAdminProfileProgressBar;
    public Uri imageURI;

    private String userID, username, phone, email, password, birthDate, gender, address, role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_admin_profile);

        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();

        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();

        editMyAdminProfileProgressBar = (ProgressBar) findViewById(R.id.editMyAdminProfileProgressBar);
        etAdminUsername = (EditText) findViewById(R.id.etAdminUsername);
        etAdminBirthDate = (EditText) findViewById(R.id.etAdminDateBirth);
        etAdminAddress = (EditText) findViewById(R.id.etAdminAddress);
        etAdminPhone = (EditText) findViewById(R.id.etAdminPhone);
        etAdminEmail = (EditText) findViewById(R.id.etAdminEmail);
        etAdminNewPassword = (EditText) findViewById(R.id.etAdminNewPassword);
        btnSave = (Button) findViewById(R.id.btnSaveAdminProfile);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAdminProfile();
            }
        });

        mReference = FirebaseDatabase.getInstance().getReference("Users");
        mReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ManageAdminHistory adminProfile = snapshot.getValue(ManageAdminHistory.class);

                if (adminProfile != null) {
                    username = adminProfile.username;
                    phone = adminProfile.phone;
                    email = adminProfile.email;
                    password = adminProfile.password;
                    birthDate = adminProfile.birthDate;
                    gender = adminProfile.gender;
                    address = adminProfile.address;
                    role = adminProfile.role;

                    displayAllInfo(username, phone, email, birthDate, gender, address, role);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyAdminProfile.this, "Admin Profile information not retrieved!", Toast.LENGTH_SHORT).show();
            }
        });

        // load admin profile picture
        mReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID).child("image");
        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String imageLink = snapshot.getValue(String.class);
                Picasso.get().load(imageLink).into(imgAdminPicture);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyAdminProfile.this, "Error loading profile picture!", Toast.LENGTH_SHORT).show();
            }
        });

        // upload picture
        imgAdminPicture = (ImageView) findViewById(R.id.imgAdminPicture);
        imgAdminPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePicture();
            }
        });
    }

    private void displayAllInfo(String username, String phone, String email, String birthDate, String gender, String address, String role) {
        // set text admin info
        etAdminUsername.setText(username);
        etAdminBirthDate.setText(birthDate);
        etAdminAddress.setText(address);
        etAdminPhone.setText(phone);
        etAdminEmail.setText(email);

        // drop down list gender
        spinnerGender = (Spinner) findViewById(R.id.spinnerGender);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gender, R.layout.spinner_text);
        adapter.setDropDownViewResource(R.layout.custom_drop_down_spinner);
        spinnerGender.setPadding(10, 10, 10, 10);
        spinnerGender.setAdapter(adapter);
        spinnerGender.setSelection(adapter.getPosition(gender));
        spinnerGender.setOnItemSelectedListener(this);

        // drop down list role
        spinnerRole = (Spinner) findViewById(R.id.spinnerRole);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.admin_role, R.layout.spinner_text);
        adapter2.setDropDownViewResource(R.layout.custom_drop_down_spinner);
        spinnerRole.setPadding(10, 10, 10, 10);
        spinnerRole.setAdapter(adapter2);
        spinnerRole.setSelection(adapter2.getPosition(role));
        spinnerRole.setOnItemSelectedListener(this);
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
                                mReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
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

    private void choosePicture() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, 1);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void saveAdminProfile() {
        String usernameInput = etAdminUsername.getText().toString().trim();
        String roleInput = spinnerRole.getSelectedItem().toString().trim();
        String genderInput = spinnerGender.getSelectedItem().toString().trim();
        String birthDateInput = etAdminBirthDate.getText().toString().trim();
        String addressInput = etAdminAddress.getText().toString().trim();
        String phoneInput = etAdminPhone.getText().toString().trim();
        String emailInput = etAdminEmail.getText().toString().trim();
        String passwordInput = etAdminNewPassword.getText().toString().trim();

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

        if (emailInput.isEmpty()){
            etAdminEmail.setError("This field cannot be empty!");
            etAdminEmail.requestFocus();
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()){
            etAdminEmail.setError("Invalid format! Example email: jack123@gmail.com");
            etAdminEmail.requestFocus();
            return;
        } else {
            etAdminEmail.setError(null);
        }

        if (passwordInput.length() < 8 && !passwordInput.isEmpty()){
            etAdminNewPassword.setError("Password must contain at least 8 character!");
            etAdminNewPassword.requestFocus();
            return;
        } else if (passwordInput.equals(password)){
            etAdminNewPassword.setError("Old and new password must not be same!");
            etAdminNewPassword.requestFocus();
            return;
        } else {
            etAdminNewPassword.setError(null);
        }

        if (!passwordInput.isEmpty()){ // password changed
            final EditText etCheckPassword = new EditText(MyAdminProfile.this);
            etCheckPassword.setInputType(InputType.TYPE_CLASS_NUMBER);
            etCheckPassword.setPadding(100, 100, 100, 50);

            AlertDialog.Builder builder = new AlertDialog.Builder(MyAdminProfile.this);
            builder.setTitle("Please enter your old password!");
            builder.setView(etCheckPassword);
            builder.setCancelable(false);
            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(MyAdminProfile.this, "Old password input matched!", Toast.LENGTH_SHORT).show();

                    if (password.equals(etCheckPassword.getText().toString().trim())) { // password matched

                        if (emailInput.equals(email)) { // no email change
                            saveWithNoEmailPassword(usernameInput, roleInput, genderInput, birthDateInput, addressInput, phoneInput, emailInput, passwordInput);

                        } else { // email changed
                            saveWithEmailPassword(usernameInput, roleInput, genderInput, birthDateInput, addressInput, phoneInput, emailInput, passwordInput);
                        }

                    } else { // password not matched
                        Toast.makeText(MyAdminProfile.this, "Old password input does not matched!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        } else { // no password change
            if (emailInput.equals(email)) { // email not changed
                saveWithNoEmailNoPassword(usernameInput, roleInput, genderInput, birthDateInput, addressInput, phoneInput, emailInput, passwordInput);

            } else { // email changed
                saveWithEmailNoPassword(usernameInput, roleInput, genderInput, birthDateInput, addressInput, phoneInput, emailInput, passwordInput);
            }

        }

    }

    private void saveWithNoEmailPassword(String usernameInput, String roleInput, String genderInput, String birthDateInput, String addressInput, String phoneInput, String emailInput, String passwordInput) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MyAdminProfile.this);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure want to edit your admin profile?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                user.updatePassword(passwordInput).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("username" , usernameInput);
                        childUpdates.put("role" , roleInput);
                        childUpdates.put("gender" , genderInput);
                        childUpdates.put("birthDate" , birthDateInput);
                        childUpdates.put("address" , addressInput);
                        childUpdates.put("phone" , phoneInput);
                        childUpdates.put("password" , passwordInput);
                        mReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
                        mReference.updateChildren(childUpdates);

                        Intent z = new Intent(MyAdminProfile.this, AdminHomepage.class);
                        startActivity(z);
                        Toast.makeText(MyAdminProfile.this, "Admin edit profile successfully!", Toast.LENGTH_SHORT).show();
                        editMyAdminProfileProgressBar.setVisibility(View.GONE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MyAdminProfile.this, "Failed to change password!", Toast.LENGTH_SHORT).show();
                        editMyAdminProfileProgressBar.setVisibility(View.GONE);
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

    private void saveWithEmailPassword(String usernameInput, String roleInput, String genderInput, String birthDateInput, String addressInput, String phoneInput, String emailInput, String passwordInput) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MyAdminProfile.this);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure want to edit your admin profile?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                user.updatePassword(passwordInput).addOnSuccessListener(new OnSuccessListener<Void>() { // update password
                    @Override
                    public void onSuccess(Void unused) {
                        user.updateEmail(emailInput).addOnSuccessListener(new OnSuccessListener<Void>() { // update email
                            @Override
                            public void onSuccess(Void unused) {
                                FirebaseUser verifyEmail = FirebaseAuth.getInstance().getCurrentUser();
                                verifyEmail.sendEmailVerification();

                                Map<String, Object> childUpdates = new HashMap<>();
                                childUpdates.put("username" , usernameInput);
                                childUpdates.put("role" , roleInput);
                                childUpdates.put("gender" , genderInput);
                                childUpdates.put("birthDate" , birthDateInput);
                                childUpdates.put("phone" , phoneInput);
                                childUpdates.put("email" , emailInput);
                                childUpdates.put("address" , addressInput);
                                childUpdates.put("password" , passwordInput);
                                mReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
                                mReference.updateChildren(childUpdates);

                                Intent z = new Intent(MyAdminProfile.this, AdminHomepage.class);
                                startActivity(z);
                                Toast.makeText(MyAdminProfile.this, "Admin edit profile successfully!", Toast.LENGTH_SHORT).show();
                                editMyAdminProfileProgressBar.setVisibility(View.GONE);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MyAdminProfile.this, "Failed to edit profile!", Toast.LENGTH_SHORT).show();
                                editMyAdminProfileProgressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MyAdminProfile.this, "Failed to change password!", Toast.LENGTH_SHORT).show();
                        editMyAdminProfileProgressBar.setVisibility(View.GONE);
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

    private void saveWithNoEmailNoPassword(String usernameInput, String roleInput, String genderInput, String birthDateInput, String addressInput, String phoneInput, String emailInput, String passwordInput) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MyAdminProfile.this);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure want to edit your admin profile?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("username" , usernameInput);
                childUpdates.put("role" , roleInput);
                childUpdates.put("gender" , genderInput);
                childUpdates.put("birthDate" , birthDateInput);
                childUpdates.put("phone" , phoneInput);
                childUpdates.put("address" , addressInput);
                mReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
                mReference.updateChildren(childUpdates);

                Intent z = new Intent(MyAdminProfile.this, AdminHomepage.class);
                startActivity(z);
                Toast.makeText(MyAdminProfile.this, "Admin edit profile successfully!", Toast.LENGTH_SHORT).show();
                editMyAdminProfileProgressBar.setVisibility(View.GONE);
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

    private void saveWithEmailNoPassword(String usernameInput, String roleInput, String genderInput, String birthDateInput, String addressInput, String phoneInput, String emailInput, String passwordInput) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MyAdminProfile.this);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure want to edit your admin profile?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                user.updateEmail(emailInput).addOnSuccessListener(new OnSuccessListener<Void>() { // update email
                    @Override
                    public void onSuccess(Void unused) {
                        FirebaseUser verifyEmail = FirebaseAuth.getInstance().getCurrentUser();
                        verifyEmail.sendEmailVerification();

                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("username" , usernameInput);
                        childUpdates.put("role" , roleInput);
                        childUpdates.put("gender" , genderInput);
                        childUpdates.put("birthDate" , birthDateInput);
                        childUpdates.put("phone" , phoneInput);
                        childUpdates.put("email" , emailInput);
                        childUpdates.put("address" , addressInput);
                        mReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
                        mReference.updateChildren(childUpdates);

                        Intent z = new Intent(MyAdminProfile.this, AdminHomepage.class);
                        startActivity(z);
                        Toast.makeText(MyAdminProfile.this, "Admin edit profile successfully!", Toast.LENGTH_SHORT).show();
                        editMyAdminProfileProgressBar.setVisibility(View.GONE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MyAdminProfile.this, "Failed to edit profile!", Toast.LENGTH_SHORT).show();
                        editMyAdminProfileProgressBar.setVisibility(View.GONE);
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