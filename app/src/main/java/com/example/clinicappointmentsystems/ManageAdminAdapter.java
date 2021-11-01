package com.example.clinicappointmentsystems;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ManageAdminAdapter extends RecyclerView.Adapter<ManageAdminAdapter.MyViewHolder> {
    Context mContext;
    ArrayList<ManageAdminHistory> adminList;

    DatabaseReference mReference;

    public ManageAdminAdapter(Context context, ArrayList<ManageAdminHistory> adminList) {
        mContext = context;
        this.adminList = adminList;
    }

    @NonNull
    @Override
    public ManageAdminAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.manage_admin_item, parent, false);
        return new ManageAdminAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ManageAdminAdapter.MyViewHolder holder, int position) {
        ManageAdminHistory manageAdminHistory = adminList.get(position);

        int number = position + 1;
        holder.tvNo.setText(number + "");
        holder.tvUsername.setText(manageAdminHistory.getUsername());

        if (manageAdminHistory.getRole().equals("Administrative Medical Assistant")) {
            holder.tvRole.setText("AMA");
            holder.tvRole.setTextColor(Color.parseColor("#FF0000"));
        } else if (manageAdminHistory.getRole().equals("Medical Records Specialist")) {
            holder.tvRole.setText("MRS");
            holder.tvRole.setTextColor(Color.parseColor("#FFBD00"));
        } else if (manageAdminHistory.getRole().equals("Medical Billing Specialist")) {
            holder.tvRole.setText("MBS");
            holder.tvRole.setTextColor(Color.parseColor("#009B0D"));
        } else if (manageAdminHistory.getRole().equals("Scheduler")) {
            holder.tvRole.setText("S");
            holder.tvRole.setTextColor(Color.parseColor("#006AA1"));
        } else if (manageAdminHistory.getRole().equals("Cleaner")) {
            holder.tvRole.setText("C");
            holder.tvRole.setTextColor(Color.parseColor("#A36EFF"));
        } else if (manageAdminHistory.getRole().equals("Security Guard")) {
            holder.tvRole.setText("SG");
            holder.tvRole.setTextColor(Color.parseColor("#EF4CFF"));
        }

        holder.imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(),EditAdminProfile.class);
                i.putExtra("address", manageAdminHistory.getAddress());
                i.putExtra("balance", manageAdminHistory.getBalance());
                i.putExtra("birthDate", manageAdminHistory.getBirthDate());
                i.putExtra("email", manageAdminHistory.getEmail());
                i.putExtra("gender", manageAdminHistory.getGender());
                i.putExtra("image", manageAdminHistory.getImage());
                i.putExtra("password", manageAdminHistory.getPassword());
                i.putExtra("phone", manageAdminHistory.getPhone());
                i.putExtra("userType", manageAdminHistory.getUserType());
                i.putExtra("username", manageAdminHistory.getUsername());
                i.putExtra("role", manageAdminHistory.getRole());
                view.getContext().startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return adminList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvNo, tvUsername, tvRole;
        TableLayout mTableLayout3;
        ImageView imgEdit;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNo = itemView.findViewById(R.id.tvNo);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            mTableLayout3 = (TableLayout) itemView.findViewById(R.id.layout_table_2);
            imgEdit = itemView.findViewById(R.id.imgEdit);
            tvRole = itemView.findViewById(R.id.tvRole);
        }
    }

    private void deleteAdmin(String adminUsername, View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Delete admin");
        builder.setMessage("Are you sure want to delete this admin?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Query query = mReference.orderByChild("username").equalTo(adminUsername);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) { // loop all the data
                            dataSnapshot.getRef().removeValue(); // delete data if username from table is same as database

                            Toast.makeText(view.getContext(), "Admin delete successfully!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(view.getContext(), "Admin in the list was not delete, something went wrong!", Toast.LENGTH_SHORT).show();
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

        builder.create().show();
    }
}
