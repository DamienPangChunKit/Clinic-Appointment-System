package com.example.clinicappointmentsystems;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AdminHomepage extends AppCompatActivity implements View.OnClickListener {
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    public NavigationView navigationView;
    private TextView displayUsername, displayUserID, tvBalance, tvDailyCovidCases;
    private ImageView profilePicture, imgCopyUserID, imgMoreCovidInfo, imgMoney;
    private CircleImageView btnAppointment, btnManageAdmin, btnLogout;
    private View headerLayout;
    private Uri imageURI;

    AdminHomepage activity;

    // quick report
    public static EditText etAppointmentID, etTemp, etName, etInstruction, etOthersIll;
    Spinner spinnerDoctor;
    SearchView search_userID;
    RecyclerView mRecyclerView;
    public static CheckBox check1, check2, check3, check4, check5, check6, check7;
    RadioGroup mRadioGroup;
    RadioButton mRadioButton;
    public static RadioButton radioMale, radioFemale;
    Button btnSend;

    AdminHomepageAdapter mAdminHomepageAdapter;
    ArrayList<AdminHomepageHistory> appointmentList;

    private DatabaseReference mReference, reference2;
    private FirebaseUser user;
    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;

    private String userID, username, phone, email, password, birthDate, gender, address, balance, noCases;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_homepage);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();

        btnAppointment = (CircleImageView) findViewById(R.id.btnAppointment);
        btnAppointment.setOnClickListener(this);
        btnManageAdmin = (CircleImageView) findViewById(R.id.btnManageAdmin);
        btnManageAdmin.setOnClickListener(this);
        btnLogout = (CircleImageView) findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(this);


        // quick report

        etAppointmentID = (EditText) findViewById(R.id.etAppointmentID);
        etTemp = (EditText) findViewById(R.id.etTemp);
        etName = (EditText) findViewById(R.id.etName);
        etInstruction = (EditText) findViewById(R.id.etInstruction);
        etOthersIll = (EditText) findViewById(R.id.etOthersIll);

        if (!etTemp.getText().toString().trim().isEmpty()) {
            float checkETTemp = Float.parseFloat(etTemp.getText().toString());

            etTemp.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (checkETTemp < 35 ) {
                        etTemp.setText("35.0");
                    } else if (checkETTemp > 40) {
                        etTemp.setText("40.0");
                    } else {
                        etTemp.setText(String.format("%.1f", checkETTemp));
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (checkETTemp < 35 ) {
                        etTemp.setText("35.0");
                    } else if (checkETTemp > 40) {
                        etTemp.setText("40.0");
                    } else {
                        etTemp.setText(String.format("%.1f", checkETTemp));
                    }
                }
            });
        }

        spinnerDoctor = (Spinner) findViewById(R.id.spinnerDoctor);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.doctor, R.layout.spinner_doctor);
        adapter.setDropDownViewResource(R.layout.custom_drop_down_spinner);
        spinnerDoctor.setPadding(15, 0, 15, 0);
        spinnerDoctor.setAdapter(adapter);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerSearchAID);
        search_userID = (SearchView) findViewById(R.id.search_userID);
        search_userID.setQueryHint("AID, UID"); // can search appointment or user ID

        appointmentList = new ArrayList<>();
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdminHomepageAdapter = new AdminHomepageAdapter(this, appointmentList);
        mRecyclerView.setAdapter(mAdminHomepageAdapter);

        loadAppointmentID();

        check1 = (CheckBox) findViewById(R.id.checkbox1);
        check2 = (CheckBox) findViewById(R.id.checkbox2);
        check3 = (CheckBox) findViewById(R.id.checkbox3);
        check4 = (CheckBox) findViewById(R.id.checkbox4);
        check5 = (CheckBox) findViewById(R.id.checkbox5);
        check6 = (CheckBox) findViewById(R.id.checkbox6);
        check7 = (CheckBox) findViewById(R.id.checkbox7);

        etOthersIll.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (etOthersIll.getText().toString().trim().isEmpty()) {
                    check7.setChecked(false);
                } else {
                    check7.setChecked(true);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                check7.setChecked(true);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (etOthersIll.getText().toString().trim().isEmpty()) {
                    check7.setChecked(false);
                } else {
                    check7.setChecked(true);
                }
            }
        });

        check7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!etOthersIll.getText().toString().trim().isEmpty()) {
                    check7.setChecked(true);
                }
            }
        });

        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroupGender);
        radioMale = (RadioButton) findViewById(R.id.radio1);
        radioFemale = (RadioButton) findViewById(R.id.radio2);

        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendQuickReport();
            }
        });

        // quick report


        drawerLayout = findViewById(R.id.layout_homepage_2); // drawer layout instance to toggle the menu icon to open drawer and back button to close drawer
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle); // pass the Open and Close toggle for the drawer layout listener to toggle the button
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // to make the Navigation drawer icon always appear on the action bar

        navigationView = (NavigationView) findViewById(R.id.adminHomeNavigation);
        headerLayout = navigationView.getHeaderView(0);
        displayUsername = (TextView) headerLayout.findViewById(R.id.tvUsername);
        displayUserID = (TextView) headerLayout.findViewById(R.id.tvUserID);
        tvBalance = (TextView) headerLayout.findViewById(R.id.tvBalance);
        tvDailyCovidCases = (TextView) findViewById(R.id.tvDailyCovidCases);
        profilePicture = (ImageView) headerLayout.findViewById(R.id.profileImage);
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePicture();
            }
        });

        imgCopyUserID = (ImageView) headerLayout.findViewById(R.id.imgCopy);
        imgCopyUserID.setColorFilter(Color.rgb(0, 0, 0));
        imgCopyUserID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copyUserID();
            }
        });

        imgMoreCovidInfo = (ImageView) findViewById(R.id.imgMoreCovidInfo);
        imgMoreCovidInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.worldometers.info/coronavirus/country/malaysia/")));
            }
        });

        updateDailyCovidCases();

        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();
        fetchAdminImage(userID);

        mReference = FirebaseDatabase.getInstance().getReference("Users");
        mReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);

                if (userProfile != null) {
                    username = userProfile.username;
                    phone = userProfile.phone;
                    email = userProfile.email;
                    password = userProfile.password;
                    birthDate = userProfile.birthDate;
                    gender = userProfile.gender;
                    address = userProfile.address;
                    balance = userProfile.balance;

                    displayUsername.setText(username);
                    displayUserID.setText(userID);
                    tvBalance.setText(balance);

                    navigationAction(username, phone, email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        imgMoney = (ImageView) headerLayout.findViewById(R.id.imgMoney);
        imgMoney.setColorFilter(Color.rgb(34, 137, 0));
    }

    public static void instant_filled_in(String getAID, String getName, String getGender, String getSymptoms, String getOthers) {
        // clear all checked field
        check1.setChecked(false);
        check2.setChecked(false);
        check3.setChecked(false);
        check4.setChecked(false);
        check5.setChecked(false);
        check6.setChecked(false);
        check7.setChecked(false);

        // insert information
        try {
            etAppointmentID.setText(getAID);
            etName.setText(getName);

            if (getGender.equals("male")) {
                radioMale.setChecked(true);
            } else {
                radioFemale.setChecked(true);
            }

            if (getSymptoms.isEmpty()) {
                check1.setChecked(false);
                check2.setChecked(false);
                check3.setChecked(false);
                check4.setChecked(false);
                check5.setChecked(false);
                check6.setChecked(false);
                check7.setChecked(true);

                etOthersIll.setText(getOthers);

            } else if (getSymptoms.toLowerCase().contains("sick")) {
                check1.setChecked(true);

                if (!getOthers.isEmpty()) {
                    check7.setChecked(true);
                    etOthersIll.setText(getOthers);
                }

            } else if (getSymptoms.toLowerCase().contains("cold") || getSymptoms.toLowerCase().contains("flu")) {
                check2.setChecked(true);

                if (!getOthers.isEmpty()) {
                    check7.setChecked(true);
                    etOthersIll.setText(getOthers);
                }

            } else if (getSymptoms.toLowerCase().contains("cough")) {
                check3.setChecked(true);

                if (!getOthers.isEmpty()) {
                    check7.setChecked(true);
                    etOthersIll.setText(getOthers);
                }

            } else if (getSymptoms.toLowerCase().contains("allergic")) {
                check4.setChecked(true);

                if (!getOthers.isEmpty()) {
                    check7.setChecked(true);
                    etOthersIll.setText(getOthers);
                }

            } else if (getSymptoms.toLowerCase().contains("headaches") || getSymptoms.toLowerCase().contains("head pain")) {
                check5.setChecked(true);

                if (!getOthers.isEmpty()) {
                    check7.setChecked(true);
                    etOthersIll.setText(getOthers);
                }

            } else if (getSymptoms.toLowerCase().contains("stomach pain")) {
                check6.setChecked(true);

                if (!getOthers.isEmpty()) {
                    check7.setChecked(true);
                    etOthersIll.setText(getOthers);
                }

            } else {
                check7.setChecked(true);

                if (getOthers.isEmpty()) {
                    etOthersIll.setText(getSymptoms);
                } else {
                    etOthersIll.setText(getSymptoms + ", " + getOthers);
                }
            }

        } catch (Exception e) {
            Log.d("Exception","Exception of type"+e.getMessage());
        }
    }

    private void loadAppointmentID() {
        reference2 = FirebaseDatabase.getInstance().getReference("AppointmentMade");
        reference2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot allUserID : snapshot.getChildren()) {
                    String allUserIDKey = allUserID.getKey();

                    reference2.child(allUserIDKey).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                AdminHomepageHistory adminHomepageHistory = dataSnapshot.getValue(AdminHomepageHistory.class);

                                if (adminHomepageHistory != null && adminHomepageHistory.getStatus().equals("paid")) {
                                    for (int i = 0; i < appointmentList.size(); i++) {
                                        if (adminHomepageHistory.getId().equals(appointmentList.get(i).getId())) {
                                            appointmentList.remove(i);
                                        }
                                    }

                                    appointmentList.add(adminHomepageHistory);
                                }
                            }

                            mAdminHomepageAdapter.notifyDataSetChanged();
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

    private void sendQuickReport() {
        // edit text
        String appointmentIDInput = etAppointmentID.getText().toString().trim();
        String tempInput = etTemp.getText().toString().trim();
        String nameInput = etName.getText().toString().trim();
        String instructionInput = etInstruction.getText().toString().trim();
        String othersInput = etOthersIll.getText().toString().trim();
        float checkETTemp;

        try { // if admin accidentally clicked send without input temperature field since empty string cannot convert
            checkETTemp = Float.parseFloat(tempInput);
        } catch (Exception e) {
            checkETTemp = 0f;
        }

        // spinner
        String doctorInput = spinnerDoctor.getSelectedItem().toString().trim();

        // radio group
        int radioID = mRadioGroup.getCheckedRadioButtonId();
        mRadioButton = findViewById(radioID);

        // checkbox
        String checkIllness = "";
        String finalIllness = "";
        String getSick, getFlu, getCough, getAllergic, getHeadaches, getStomachPain, getOthers;

        if (appointmentIDInput.isEmpty()) {
            etAppointmentID.setError("This field cannot be empty!");
            etAppointmentID.requestFocus();
            return;
        } else {
            etAppointmentID.setError(null);
        }

        if (tempInput.isEmpty()) {
            etTemp.setError("This field cannot be empty!");
            etTemp.requestFocus();
            return;
        } else if (checkETTemp < 36) {
            etTemp.setError("Temperature too low!");
            etTemp.requestFocus();
            return;
        } else if (checkETTemp > 40) {
            etTemp.setError("Temperature too high!");
            etTemp.requestFocus();
            return;
        } else {
            etTemp.setText(String.format("%.1f", checkETTemp));
            etTemp.setError(null);
        }

        if (nameInput.isEmpty()) {
            etName.setError("This field cannot be empty!");
            etName.requestFocus();
            return;
        } else {
            etName.setError(null);
        }

        if (instructionInput.isEmpty()) {
            etInstruction.setError("This field cannot be empty!");
            etInstruction.requestFocus();
            return;
        } else {
            etInstruction.setError(null);
        }

        if (!check1.isChecked() &&
            !check2.isChecked() &&
            !check3.isChecked() &&
            !check4.isChecked() &&
            !check5.isChecked() &&
            !check6.isChecked() &&
            !check7.isChecked()) {
            Toast.makeText(AdminHomepage.this, "Please select illness!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (check1.isChecked()) {
            checkIllness += check1.getText().toString().trim() + ", ";
        }

        if (check2.isChecked()) {
            checkIllness += check2.getText().toString().trim() + ", ";
        }

        if (check3.isChecked()) {
            checkIllness += check3.getText().toString().trim() + ", ";
        }

        if (check4.isChecked()) {
            checkIllness += check4.getText().toString().trim() + ", ";
        }

        if (check5.isChecked()) {
            checkIllness += check5.getText().toString().trim() + ", ";
        }

        if (check6.isChecked()) {
            checkIllness += check6.getText().toString().trim() + ", ";
        }

        if (check7.isChecked() && othersInput.isEmpty()) {
            etOthersIll.setError("This field cannot be empty!");
            etOthersIll.requestFocus();
            return;
        } else {
            checkIllness += othersInput;
            etOthersIll.setError(null);
        }

        if (checkIllness.substring(checkIllness.length() - 2).equals(", ")) {
            StringBuffer sb = new StringBuffer(checkIllness);
            finalIllness = String.valueOf(sb.deleteCharAt(sb.length() - 2));
        } else {
            finalIllness = checkIllness;
        }

        if (!radioMale.isChecked() && !radioFemale.isChecked()) {
            Toast.makeText(AdminHomepage.this, "Please select gender!", Toast.LENGTH_SHORT).show();
            return;
        }

        String tempInputFinal = etTemp.getText().toString().trim();
        String genderInput = mRadioButton.getText().toString().trim();

        Intent i = new Intent(AdminHomepage.this, MedicalReport.class);
        i.putExtra("appointmentIDInput", appointmentIDInput);
        i.putExtra("tempInput", tempInputFinal);
        i.putExtra("doctorInput", doctorInput);
        i.putExtra("illnessInput", finalIllness);
        i.putExtra("nameInput", nameInput);
        i.putExtra("genderInput", genderInput);
        i.putExtra("instructionInput", instructionInput);
        startActivity(i);
    }

    private void fetchAdminImage(String UID) {
        mReference = FirebaseDatabase.getInstance().getReference().child("Users").child(UID).child("image");
        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String imageLink = snapshot.getValue(String.class);
                Picasso.get().load(imageLink).into(profilePicture);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminHomepage.this, "Error loading profile picture!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void choosePicture() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, 1);
    }

    private void copyUserID() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Copy User ID", displayUserID.getText().toString());
        clipboardManager.setPrimaryClip(clipData);
        clipData.getDescription();

        Toast.makeText(AdminHomepage.this, "User ID copied!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) { // upload image
            imageURI = data.getData();
            profilePicture.setImageURI(imageURI);
            uploadAdminImage();
        }
    }

    private void uploadAdminImage() {
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

    private void navigationAction(String username, String phone, String email) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                int id = menuItem.getItemId();

                switch (id) {
                    case R.id.nav_appointment:
                        Intent a = new Intent(AdminHomepage.this, AdminAppointment.class);
                        startActivity(a);
                        break;

                    case R.id.nav_mail:
                        Intent b = new Intent(AdminHomepage.this, AdminMail.class);
                        startActivity(b);
                        break;

//                    case R.id.nav_generateReport:
//                        Intent c = new Intent(AdminHomepage.this, AdminGenerateReport.class);
//                        startActivity(c);
//                        break;

                    case R.id.nav_admin:
                        Intent d = new Intent(AdminHomepage.this, ManageAdmin.class);
                        d.putExtra("userID", userID);
                        d.putExtra("password", password);
                        startActivity(d);
                        break;

                    case R.id.nav_doctor:
                        Intent f = new Intent(AdminHomepage.this, NavigationDoctor.class);
                        startActivity(f);
                        break;

                    case R.id.nav_logout:
                        FirebaseAuth.getInstance().signOut();
                        Intent g = new Intent(AdminHomepage.this, Login.class);
                        startActivity(g);
                        break;

                    default:
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateDailyCovidCases() {
        int startNum = Integer.parseInt(getString(R.string.startNum));
        int endNum = Integer.parseInt(getString(R.string.endNum));

        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();

                try {
                    String url="https://www.worldometers.info/coronavirus/country/malaysia/";
                    Document doc = Jsoup.connect(url).get();

                    Element body = doc.body();
                    builder.append(body.text());
                    noCases = builder.toString().substring(startNum, endNum);

                } catch (Exception e) {
                    builder.append("Error : ").append(e.getMessage()).append("\n");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvDailyCovidCases.setText(noCases);
                    }
                });
            }
        }).start();
    }

    private void searchAppointment() {
        search_userID.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchFunction(s);
                return true;
            }
        });
    }

    private void searchFunction(String str) {
        ArrayList<AdminHomepageHistory> itemList = new ArrayList<>();

        for (AdminHomepageHistory item : appointmentList) {
            if (item.getStatus().toLowerCase().contains(str.toLowerCase())) {
                itemList.add(item);
            } else if (item.getAppointmentDate().toLowerCase().contains(str.toLowerCase())) {
                itemList.add(item);
            } else if (item.getId().toLowerCase().contains(str.toLowerCase())) {
                itemList.add(item);
            }
        }

        AdminHomepageAdapter mAdminHomepageAdapter = new AdminHomepageAdapter(this, itemList);
        mRecyclerView.setAdapter(mAdminHomepageAdapter);
    }

    protected void onStart() {
        super.onStart();

        if (search_userID != null) {
            searchAppointment();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnAppointment:
                Intent a = new Intent(AdminHomepage.this, AdminAppointment.class);
                startActivity(a);
                break;

            case R.id.btnManageAdmin:
                Intent b = new Intent(AdminHomepage.this, ManageAdmin.class);
                b.putExtra("userID", userID);
                b.putExtra("password", password);
                startActivity(b);
                break;

            case R.id.btnLogout:
                FirebaseAuth.getInstance().signOut();
                Intent c = new Intent(AdminHomepage.this, Login.class);
                startActivity(c);
                break;
        }
    }
}