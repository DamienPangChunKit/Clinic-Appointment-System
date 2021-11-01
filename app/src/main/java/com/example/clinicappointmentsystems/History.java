package com.example.clinicappointmentsystems;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.SearchView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class History extends AppCompatActivity {
    DatabaseReference mReference;

    SearchView mSearchView;
    RecyclerView mRecyclerView;
    AppointmentAdapter mAppointmentAdapter;

    ArrayList<AppointmentHistory> historyList;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerHistory);
        mSearchView = (SearchView) findViewById(R.id.search_view);
        mSearchView.setQueryHint("Search status...");

        Intent i = getIntent();
        userID = i.getStringExtra("userID");

        historyList = new ArrayList<>();
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAppointmentAdapter = new AppointmentAdapter(this, historyList);
        mRecyclerView.setAdapter(mAppointmentAdapter);

        mReference = FirebaseDatabase.getInstance().getReference("AppointmentMade").child(userID);
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    AppointmentHistory appointmentHistory = dataSnapshot.getValue(AppointmentHistory.class);

                    for (int i = 0; i < historyList.size(); i++) {
                        if (appointmentHistory.getId().equals(historyList.get(i).getId())) {
                            historyList.remove(i);
                        }
                    }

                    historyList.add(appointmentHistory);
                }

                mAppointmentAdapter.notifyDataSetChanged();
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
        ArrayList<AppointmentHistory> itemList = new ArrayList<>();

        for (AppointmentHistory item : historyList) {
            if (item.getStatus().toLowerCase().contains(str.toLowerCase())) {
                itemList.add(item);
            }
        }

        AppointmentAdapter mAppointmentAdapter = new AppointmentAdapter(this, itemList);
        mRecyclerView.setAdapter(mAppointmentAdapter);
    }

    protected void onStart() {
        super.onStart();

        if (mSearchView != null) {
            searchAppointment();
        }
    }
}