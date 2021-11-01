package com.example.clinicappointmentsystems;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SearchView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminMail extends AppCompatActivity {
    DatabaseReference mReference;

    RecyclerView mRecyclerView;
    SearchView mSearchView;

    AdminMailAdapter mAdminMailAdapter;
    ArrayList<AdminMailHistory> mailList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_mail);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerAdminMail);
        mSearchView = (SearchView) findViewById(R.id.search_mail);
        mSearchView.setQueryHint("Mail ID, Title or status...");

        mailList = new ArrayList<>();
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdminMailAdapter = new AdminMailAdapter(this, mailList);
        mRecyclerView.setAdapter(mAdminMailAdapter);

        mReference = FirebaseDatabase.getInstance().getReference("ContactUs");
        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot allUserID : snapshot.getChildren()) {
                    String userID = allUserID.getKey();

                    mReference.child(userID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                AdminMailHistory adminMailHistory = dataSnapshot.getValue(AdminMailHistory.class);

                                for (int i = 0; i < mailList.size(); i++) {
                                    if (adminMailHistory.getMailID().equals(mailList.get(i).getMailID())) {
                                        mailList.remove(i);
                                    }
                                }

                                mailList.add(adminMailHistory);
                            }

                            mAdminMailAdapter.notifyDataSetChanged();
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

    private void searchMail() {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchMail(s);
                return true;
            }
        });
    }

    private void searchMail(String str) {
        ArrayList<AdminMailHistory> itemList = new ArrayList<>();

        for (AdminMailHistory item : mailList) {
            if (item.getTitle().toLowerCase().contains(str.toLowerCase())) {
                itemList.add(item);
            } else if (item.getStatus().toLowerCase().contains(str.toLowerCase())) {
                itemList.add(item);
            } else if (item.getMailID().toLowerCase().contains(str.toLowerCase())) {
                itemList.add(item);
            }
        }

        AdminMailAdapter mAdminMailAdapter = new AdminMailAdapter(this, itemList);
        mRecyclerView.setAdapter(mAdminMailAdapter);
    }

    protected void onStart() {
        super.onStart();

        if (mSearchView != null) {
            searchMail();
        }
    }
}