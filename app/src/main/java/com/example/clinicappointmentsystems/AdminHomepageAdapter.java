package com.example.clinicappointmentsystems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdminHomepageAdapter extends RecyclerView.Adapter<AdminHomepageAdapter.MyViewHolder> {
    Context mContext;
    ArrayList<AdminHomepageHistory> appointmentList;

    public AdminHomepageAdapter(Context context, ArrayList<AdminHomepageHistory> appointmentList) {
        mContext = context;
        this.appointmentList = appointmentList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.admin_homepage_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        AdminHomepageHistory adminHomepageHistory = appointmentList.get(position);
        holder.tvAppointmentID.setText(adminHomepageHistory.getId());
        holder.layout_AID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String appointmentID = adminHomepageHistory.getId();
                String name = adminHomepageHistory.getName();
                String gender = adminHomepageHistory.getGender();
                String symptoms = adminHomepageHistory.getSymptoms();
                String others = adminHomepageHistory.getOthers();

                AdminHomepage.instant_filled_in(appointmentID, name, gender, symptoms, others);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvAppointmentID;
        TableLayout layout_AID;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvAppointmentID = itemView.findViewById(R.id.tvAppointmentID);
            layout_AID = (TableLayout) itemView.findViewById(R.id.layout_AID);
        }
    }
}
