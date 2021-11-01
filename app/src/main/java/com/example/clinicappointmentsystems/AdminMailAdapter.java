package com.example.clinicappointmentsystems;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdminMailAdapter extends RecyclerView.Adapter<AdminMailAdapter.MyViewHolder> {
    Context mContext;
    ArrayList<AdminMailHistory> mailList;

    public AdminMailAdapter(Context context, ArrayList<AdminMailHistory> mailList) {
        mContext = context;
        this.mailList = mailList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.manage_mail_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        AdminMailHistory adminMailHistory = mailList.get(position);

        int number = position + 1;
        holder.tvNo.setText(number + "");
        holder.tvTitle.setText(adminMailHistory.getTitle());
        holder.tvStatus.setText(adminMailHistory.getStatus());
        holder.mTableLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(),AdminMailDetails.class);
                i.putExtra("name", adminMailHistory.getName());
                i.putExtra("email", adminMailHistory.getEmail());
                i.putExtra("phone", adminMailHistory.getPhone());
                i.putExtra("title", adminMailHistory.getTitle());
                i.putExtra("message", adminMailHistory.getMessage());
                i.putExtra("dateTime", adminMailHistory.getDateTime());
                i.putExtra("status", adminMailHistory.getStatus());
                i.putExtra("mailID", adminMailHistory.getMailID());
                view.getContext().startActivity(i);
            }
        });

        if (adminMailHistory.getStatus().equals("pending")) {
            holder.tvStatus.setTextColor(Color.parseColor("#FF0000"));
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#00AF41"));
        }
    }

    @Override
    public int getItemCount() {
        return mailList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvNo, tvTitle, tvStatus;
        TableLayout mTableLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNo = itemView.findViewById(R.id.tvNo);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            mTableLayout = (TableLayout) itemView.findViewById(R.id.layout_table_4);
        }
    }
}
