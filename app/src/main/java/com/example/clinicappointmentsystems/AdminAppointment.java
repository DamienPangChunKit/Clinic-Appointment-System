package com.example.clinicappointmentsystems;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.SearchView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminAppointment extends AppCompatActivity {
    DatabaseReference mReference;

    RecyclerView mRecyclerView;
    SearchView mSearchView;

    AdminAppointmentAdapter mAdminAppointmentAdapter;
    ArrayList<AdminAppointmentHistory> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_appointment);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerAdminAppointment);
        mSearchView = (SearchView) findViewById(R.id.search_view);
        mSearchView.setQueryHint("id, date or status...");

        historyList = new ArrayList<>();
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdminAppointmentAdapter = new AdminAppointmentAdapter(this, historyList);
        mRecyclerView.setAdapter(mAdminAppointmentAdapter);

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
                                AdminAppointmentHistory adminAppointmentHistory = dataSnapshot.getValue(AdminAppointmentHistory.class);

                                for (int i = 0; i < historyList.size(); i++) {
                                    if (adminAppointmentHistory.getId().equals(historyList.get(i).getId())) {
                                        historyList.remove(i);
                                    }
                                }

                                historyList.add(adminAppointmentHistory);

                            }

                            mAdminAppointmentAdapter.notifyDataSetChanged();
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
        ArrayList<AdminAppointmentHistory> itemList = new ArrayList<>();

        for (AdminAppointmentHistory item : historyList) {
            if (item.getStatus().toLowerCase().contains(str.toLowerCase())) {
                itemList.add(item);
            } else if (item.getAppointmentDate().toLowerCase().contains(str.toLowerCase())) {
                itemList.add(item);
            } else if (item.getId().toLowerCase().contains(str.toLowerCase())) {
                itemList.add(item);
            }
        }

        AdminAppointmentAdapter mAdminAppointmentAdapter = new AdminAppointmentAdapter(this, itemList);
        mRecyclerView.setAdapter(mAdminAppointmentAdapter);
    }

    protected void onStart() {
        super.onStart();

        if (mSearchView != null) {
            searchAppointment();
        }
    }
}