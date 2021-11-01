package com.example.clinicappointmentsystems;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
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
import com.tomer.fadingtextview.FadingTextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Homepage extends AppCompatActivity implements View.OnClickListener {
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private ImageView profilePicture, imgCopyUserID, imgMoreCovidInfo;
    private Uri imageURI;
    private TextView displayUsername, displayUserID, tvBalance, tvDailyCovidCases;
    private FadingTextView fadingHealthTips;
    private CircleImageView btnAppointment, btnDoctor, btnReloadMoney, btnHistory, btnProfile, btnLogout;

    private FirebaseUser user;
    private DatabaseReference mReference;
    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;
    
    private String userID, username, phone, email, password, birthDate, gender, address, balance, noCases;

    NavigationView navigationView;
    View headerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();

        drawerLayout = findViewById(R.id.layout_homepage); // drawer layout instance to toggle the menu icon to open drawer and back button to close drawer
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle); // pass the Open and Close toggle for the drawer layout listener to toggle the button
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // to make the Navigation drawer icon always appear on the action bar

        navigationView = (NavigationView) findViewById(R.id.homeNavigation);
        headerLayout = navigationView.getHeaderView(0);
        profilePicture = (ImageView) headerLayout.findViewById(R.id.profileImage);
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePicture();
            }
        });

        imgCopyUserID = (ImageView) headerLayout.findViewById(R.id.imgCopy);
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

        displayUsername = (TextView) headerLayout.findViewById(R.id.tvUsername);
        displayUserID = (TextView) headerLayout.findViewById(R.id.tvUserID);
        tvBalance = (TextView) headerLayout.findViewById(R.id.tvBalance);
        tvDailyCovidCases = (TextView) findViewById(R.id.tvDailyCovidCases);
        updateDailyCovidCases();

        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();

        mReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID).child("image");
        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String imageLink = snapshot.getValue(String.class);
                Picasso.get().load(imageLink).into(profilePicture);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Homepage.this, "Error loading profile picture!", Toast.LENGTH_SHORT).show();
            }
        });

        fadingHealthTips = (FadingTextView) findViewById(R.id.fadingHealthTips);
        setHealthTipsGradientColor();
        fadingHealthTips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=Health+Tips&oq=Health+Tips&aqs=chrome.0.69i59l2j0i67l3j0i512l5.4520j0j7&sourceid=chrome&ie=UTF-8")));
            }
        });

        btnAppointment = (CircleImageView) findViewById(R.id.btnAppointment);
        btnAppointment.setOnClickListener(this);
        btnDoctor = (CircleImageView) findViewById(R.id.btnDoctor);
        btnDoctor.setOnClickListener(this);
        btnReloadMoney = (CircleImageView) findViewById(R.id.btnReloadMoney);
        btnReloadMoney.setOnClickListener(this);
        btnHistory = (CircleImageView) findViewById(R.id.btnHistory);
        btnHistory.setOnClickListener(this);
        btnProfile = (CircleImageView) findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(this);
        btnLogout = (CircleImageView) findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(this);

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
                Toast.makeText(Homepage.this, "Profile information not retrieved!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAppointment:
                Intent a = new Intent(Homepage.this, Appointment.class);
                a.putExtra("userID", userID);
                a.putExtra("balance", balance);
                startActivityForResult(a, 3);
                break;

            case R.id.btnDoctor:
                Intent b = new Intent(Homepage.this, NavigationDoctor.class);
                startActivity(b);
                break;

            case R.id.btnReloadMoney:
                Intent c = new Intent(Homepage.this, ReloadMoney.class);
                c.putExtra("userID", userID);
                c.putExtra("phone", phone);
                c.putExtra("password", password);
                c.putExtra("balance", balance);
                startActivityForResult(c, 2);
                break;

            case R.id.btnHistory:
                Intent d = new Intent(Homepage.this, History.class);
                d.putExtra("userID", userID);
                startActivity(d);
                break;

            case R.id.btnProfile:
                Intent e = new Intent(Homepage.this, Profile.class);
                e.putExtra("userID", userID);
                e.putExtra("username", username);
                e.putExtra("phone", phone);
                e.putExtra("email", email);
                e.putExtra("gender", gender);
                e.putExtra("birthDate", birthDate);
                e.putExtra("address", address);
                startActivity(e);
                break;

            case R.id.btnLogout:
                FirebaseAuth.getInstance().signOut();
                Intent f = new Intent(Homepage.this, Login.class);
                startActivity(f);
                break;

            default:
                break;
        }
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

    private void setHealthTipsGradientColor() {
        TextPaint paint = fadingHealthTips.getPaint();
        float width = paint.measureText(fadingHealthTips.getText().toString());

        Shader shader = new LinearGradient(0, 0, width, fadingHealthTips.getTextSize(),
                new int[]{
                        Color.parseColor("#FF0000"),
                        Color.parseColor("#F8FF00"),
                }, null, Shader.TileMode.CLAMP);
        fadingHealthTips.getPaint().setShader(shader);
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    private void copyUserID() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Copy User ID", displayUserID.getText().toString());
        clipboardManager.setPrimaryClip(clipData);
        clipData.getDescription();

        Toast.makeText(Homepage.this, "User ID copied!", Toast.LENGTH_SHORT).show();
    }

    private void choosePicture() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) { // upload image
            imageURI = data.getData();
            profilePicture.setImageURI(imageURI);
            uploadImage();
        } else if (requestCode == 2 && resultCode == RESULT_OK) { // reload money
            String totalBalance = data.getStringExtra("totalBalance");
            tvBalance.setText(totalBalance);
        } else if (requestCode == 3 && resultCode == RESULT_OK) { // appointment
            String totalBalance = data.getStringExtra("totalBalance");
            tvBalance.setText(totalBalance);
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

    private void navigationAction(String username, String phone, String email) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                int id = menuItem.getItemId();

                switch (id) {
                    case R.id.nav_doctor:
                        Intent a = new Intent(Homepage.this, NavigationDoctor.class);
                        startActivity(a);
                        break;

                    case R.id.nav_contactUs:
                        Intent b = new Intent(Homepage.this, NavigationContactUs.class);
                        b.putExtra("phone", phone);
                        b.putExtra("email", email);
                        startActivity(b);
                        break;

                    case R.id.nav_aboutUs:
                        Intent c = new Intent(Homepage.this, NavigationAboutUs.class);
                        startActivity(c);
                        break;

                    case R.id.nav_account:
                        Intent d = new Intent(Homepage.this, Profile.class);
                        d.putExtra("userID", userID);
                        d.putExtra("username", username);
                        d.putExtra("phone", phone);
                        d.putExtra("email", email);
                        d.putExtra("gender", gender);
                        d.putExtra("birthDate", birthDate);
                        d.putExtra("address", address);
                        startActivity(d);
                        break;

                    case R.id.nav_appointment:
                        Intent e = new Intent(Homepage.this, Appointment.class);
                        startActivity(e);
                        break;

                    case R.id.nav_history:
                        Intent f = new Intent(Homepage.this, History.class);
                        f.putExtra("userID", userID);
                        startActivity(f);
                        break;

                    case R.id.nav_changePassword:
                        Intent g = new Intent(Homepage.this, ChangePassword.class);
                        g.putExtra("userID", userID);
                        startActivity(g);
                        break;

                    case R.id.nav_logout:
                        FirebaseAuth.getInstance().signOut();
                        Intent h = new Intent(Homepage.this, Login.class);
                        startActivity(h);
                        break;

                    default:
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}