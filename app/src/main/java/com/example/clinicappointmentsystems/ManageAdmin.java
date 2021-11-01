package com.example.clinicappointmentsystems;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ManageAdmin extends AppCompatActivity {
    DatabaseReference mReference;
    FirebaseUser user;

    ImageView imgDeleteMyAdmin, imgAddAdmin;
    Button btnMyAdminProfile;
    RecyclerView mRecyclerView;
    SearchView mSearchView;

    ManageAdminAdapter mManageAdminAdapter;
    ArrayList<ManageAdminHistory> adminList;

    private String userID, password, myUID, myEmail, myPassword;
    private static final long countdown = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_admin);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        imgAddAdmin = (ImageView) findViewById(R.id.imgAddAdmin);
        imgAddAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAdmin();
            }
        });
        imgDeleteMyAdmin = (ImageView) findViewById(R.id.imgDeleteMyAdmin);
        imgDeleteMyAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteMyAdmin();
            }
        });
        btnMyAdminProfile = (Button) findViewById(R.id.btnMyAdminProfile);
        btnMyAdminProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewMyAdminProfile();
            }
        });
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerManageAdmin);
        mSearchView = (SearchView) findViewById(R.id.search_admin);
        mSearchView.setQueryHint("Search username...");

        adminList = new ArrayList<>();
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mManageAdminAdapter = new ManageAdminAdapter(this, adminList);
        mRecyclerView.setAdapter(mManageAdminAdapter);

        Intent i = getIntent();
        userID = i.getStringExtra("userID");
        password = i.getStringExtra("password");

        mReference = FirebaseDatabase.getInstance().getReference("Users");
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ManageAdminHistory manageAdminHistory = dataSnapshot.getValue(ManageAdminHistory.class);

                    if (manageAdminHistory.getUserType().equals("admin") && !userID.equals(dataSnapshot.getKey())){
                        for (int i = 0; i < adminList.size(); i++) {
                            if (manageAdminHistory.getEmail().equals(adminList.get(i).getEmail())) {
                                adminList.remove(i);
                            }
                        }

                        adminList.add(manageAdminHistory);
                    }

                }

                mManageAdminAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addAdmin() {
        Intent i = new Intent(ManageAdmin.this, AddAdmin.class);
        startActivity(i);
    }

    private void deleteMyAdmin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ManageAdmin.this);
        builder.setTitle("Warning!");
        builder.setMessage("Are you sure want to delete your account? You will proceed back to login page and not able to recover this account anymore!"
                            + "\n" + "(Note: Email and Password required to delete account!)");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View deleteMyAdmin = inflater.inflate(R.layout.custom_delete_myadmin, null, false);
                final EditText editTextEmail = (EditText) deleteMyAdmin.findViewById(R.id.editTextEmail);
                final EditText editTextPassword = (EditText) deleteMyAdmin.findViewById(R.id.editTextPassword);

                user = FirebaseAuth.getInstance().getCurrentUser();
                myUID = user.getUid();
                myEmail = user.getEmail();
                myPassword = password;

                AlertDialog.Builder builder2 = new AlertDialog.Builder(ManageAdmin.this);
                builder2.setView(deleteMyAdmin);
                builder2.setTitle("Email and Password required to delete account!");
                builder2.setCancelable(false);
                builder2.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String emailInput = editTextEmail.getText().toString().trim();
                        String passwordInput = editTextPassword.getText().toString().trim();

                        if (emailInput.isEmpty()) {
                            editTextEmail.setError("Email must not empty!");
                            editTextEmail.requestFocus();
                            Toast.makeText(ManageAdmin.this, "Email must not empty!", Toast.LENGTH_SHORT).show();

                        } else if (!emailInput.equals(myEmail)) {
                            editTextEmail.setError("Wrong email!");
                            editTextEmail.requestFocus();
                            Toast.makeText(ManageAdmin.this, "Wrong email or password input!", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            editTextEmail.setError(null);
                        }

                        if (passwordInput.isEmpty()) {
                            editTextPassword.setError("Password must not empty!");
                            editTextPassword.requestFocus();
                            Toast.makeText(ManageAdmin.this, "Password must not empty!", Toast.LENGTH_SHORT).show();

                        } else if (!passwordInput.equals(myPassword)) {
                            editTextPassword.setError("Wrong password!");
                            editTextPassword.requestFocus();
                            Toast.makeText(ManageAdmin.this, "Wrong email or password input!", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            editTextPassword.setError(null);
                        }

                        // delete user account
                        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    mReference = FirebaseDatabase.getInstance().getReference("Users");
                                    Query query = mReference.orderByChild("email").equalTo(emailInput);
                                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                                                dataSnapshot.getRef().removeValue(); // remove value
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                    Intent i = new Intent(ManageAdmin.this, Login.class);
                                    startActivity(i);
                                    Toast.makeText(ManageAdmin.this, "Your admin account delete successfully!", Toast.LENGTH_SHORT).show();

                                } else {
                                    Toast.makeText(ManageAdmin.this, "Failed to delete your admin account", Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
                    }
                });

                builder2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

                AlertDialog alertDialog = builder2.create();
                alertDialog.show();

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

    private void viewMyAdminProfile() {
        Intent i = new Intent(ManageAdmin.this, MyAdminProfile.class);
        startActivityForResult(i, 20);
    }

    private void searchAppointment() {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
        ArrayList<ManageAdminHistory> itemList = new ArrayList<>();

        for (ManageAdminHistory item : adminList) {
            if (item.getUsername().toLowerCase().contains(str.toLowerCase())) {
                itemList.add(item);
            }
        }

        ManageAdminAdapter mManageAdminAdapter = new ManageAdminAdapter(this, itemList);
        mRecyclerView.setAdapter(mManageAdminAdapter);
    }

    protected void onStart() {
        super.onStart();

        if (mSearchView != null) {
            searchAppointment();
        }
    }

}