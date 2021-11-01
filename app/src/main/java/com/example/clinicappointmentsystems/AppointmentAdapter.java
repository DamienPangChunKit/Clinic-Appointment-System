package com.example.clinicappointmentsystems;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.MyViewHolder> {
    Context mContext;
    ArrayList<AppointmentHistory> historyList;

    public AppointmentAdapter(Context context, ArrayList<AppointmentHistory> historyList) {
        mContext = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.appointment_history_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        AppointmentHistory appointmentHistory = historyList.get(position);
        holder.tvAppointmentID.setText(appointmentHistory.getId());
        holder.tvDate.setText(appointmentHistory.getAppointmentDate());
        holder.tvStatus.setText(appointmentHistory.getStatus());
        holder.mTableLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(),AppointmentDetails.class);
                i.putExtra("id", appointmentHistory.getId());
                i.putExtra("name", appointmentHistory.getName());
                i.putExtra("gender", appointmentHistory.getGender());
                i.putExtra("phone", appointmentHistory.getPhone());
                i.putExtra("birthDate", appointmentHistory.getBirthDate());
                i.putExtra("address", appointmentHistory.getAddress());
                i.putExtra("email", appointmentHistory.getEmail());
                i.putExtra("symptoms", appointmentHistory.getSymptoms());
                i.putExtra("others", appointmentHistory.getOthers());
                i.putExtra("appointmentDate", appointmentHistory.getAppointmentDate());
                i.putExtra("appointmentTime", appointmentHistory.getAppointmentTime());
                i.putExtra("status", appointmentHistory.getStatus());
                i.putExtra("price", appointmentHistory.getPrice());
                i.putExtra("message", appointmentHistory.getMessage());
                view.getContext().startActivity(i);
            }
        });

        if (appointmentHistory.getStatus().equals("waiting for approval")) {
            holder.tvStatus.setTextColor(Color.parseColor("#D500FF"));
        } else if (appointmentHistory.getStatus().equals("approve")) {
            holder.tvStatus.setTextColor(Color.parseColor("#00AF41"));
        } else if (appointmentHistory.getStatus().equals("disapprove")) {
            holder.tvStatus.setTextColor(Color.parseColor("#FF8B00"));
        } else if (appointmentHistory.getStatus().equals("not paid")) {
            holder.tvStatus.setTextColor(Color.parseColor("#FF0000"));
        } else if (appointmentHistory.getStatus().equals("paid")) {
            holder.tvStatus.setTextColor(Color.parseColor("#7DFF00"));
        }

        if (appointmentHistory.getStatus().equals("paid") && !appointmentHistory.getReport().isEmpty()) {
            holder.imgReportGenerated.setVisibility(View.VISIBLE);
        } else {
            holder.imgReportGenerated.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvAppointmentID, tvDate, tvStatus;
        TableLayout mTableLayout;
        ImageView imgReportGenerated;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvAppointmentID = itemView.findViewById(R.id.tvAppointmentID);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            mTableLayout = (TableLayout) itemView.findViewById(R.id.layout_table);
            imgReportGenerated = (ImageView) itemView.findViewById(R.id.imgReportGenerated);
        }
    }
}
