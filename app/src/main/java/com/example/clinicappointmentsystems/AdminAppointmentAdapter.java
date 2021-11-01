package com.example.clinicappointmentsystems;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdminAppointmentAdapter extends RecyclerView.Adapter<AdminAppointmentAdapter.MyViewHolder> {
    Context mContext;
    ArrayList<AdminAppointmentHistory> historyList;

    DatabaseReference mReference, reference2;
    String thisAppointmentUID;

    public AdminAppointmentAdapter(Context context, ArrayList<AdminAppointmentHistory> historyList) {
        mContext = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.admin_appointment_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        AdminAppointmentHistory adminAppointmentHistory = historyList.get(position);
        holder.tvAppointmentID.setText(adminAppointmentHistory.getId());
        holder.tvDate.setText(adminAppointmentHistory.getAppointmentDate());
        holder.tvStatus.setText(adminAppointmentHistory.getStatus());
        holder.mTableLayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(),AdminAppointmentDetails.class);
                i.putExtra("name", adminAppointmentHistory.getName());
                i.putExtra("gender", adminAppointmentHistory.getGender());
                i.putExtra("phone", adminAppointmentHistory.getPhone());
                i.putExtra("birthDate", adminAppointmentHistory.getBirthDate());
                i.putExtra("address", adminAppointmentHistory.getAddress());
                i.putExtra("email", adminAppointmentHistory.getEmail());
                i.putExtra("symptoms", adminAppointmentHistory.getSymptoms());
                i.putExtra("others", adminAppointmentHistory.getOthers());
                i.putExtra("appointmentDate", adminAppointmentHistory.getAppointmentDate());
                i.putExtra("appointmentTime", adminAppointmentHistory.getAppointmentTime());
                i.putExtra("status", adminAppointmentHistory.getStatus());
                i.putExtra("message", adminAppointmentHistory.getMessage());
                i.putExtra("appointmentID", adminAppointmentHistory.getId());
                i.putExtra("pushKey", adminAppointmentHistory.getPushKey());
                i.putExtra("price", adminAppointmentHistory.getPrice());
                view.getContext().startActivity(i);
            }
        });

        holder.mTableLayout2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                displayDeleteAndPaid(view, adminAppointmentHistory.getPushKey(), adminAppointmentHistory);
                displayHoldDialog(view, adminAppointmentHistory.getPushKey(), adminAppointmentHistory, holder);
                return false;
            }
        });

        if (adminAppointmentHistory.getStatus().equals("waiting for approval")) {
            holder.tvStatus.setTextColor(Color.parseColor("#D500FF"));
        } else if (adminAppointmentHistory.getStatus().equals("approve")) {
            holder.tvStatus.setTextColor(Color.parseColor("#00AF41"));
        } else if (adminAppointmentHistory.getStatus().equals("disapprove")) {
            holder.tvStatus.setTextColor(Color.parseColor("#FF8B00"));
        } else if (adminAppointmentHistory.getStatus().equals("not paid")) {
            holder.tvStatus.setTextColor(Color.parseColor("#FF0000"));
        } else if (adminAppointmentHistory.getStatus().equals("paid")) {
            holder.tvStatus.setTextColor(Color.parseColor("#7DFF00"));
        }

        if (adminAppointmentHistory.getStatus().equals("paid") && !adminAppointmentHistory.getReport().isEmpty()) {
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
        TableLayout mTableLayout2;
        ImageView imgReportGenerated;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvAppointmentID = itemView.findViewById(R.id.tvAppointmentID);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            mTableLayout2 = itemView.findViewById(R.id.layout_table_2);
            imgReportGenerated = itemView.findViewById(R.id.imgReportGenerated);

        }
    }

    private void displayDeleteAndPaid(View view, String pushKey, AdminAppointmentHistory adminAppointmentHistory) {
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
                                AppointmentMade appointmentMade = dataSnapshot.getValue(AppointmentMade.class);

                                if (appointmentMade != null && appointmentMade.getPushKey().equals(pushKey)) {
                                    thisAppointmentUID = userID;
                                }
                            }
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

    private void displayHoldDialog(View view, String pushKey, AdminAppointmentHistory adminAppointmentHistory, MyViewHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Action!");
        builder.setMessage("Choose 1 below action to perform task");
        builder.setCancelable(false);
        builder.setPositiveButton("Checkout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doneMedicalTreatment(view, pushKey, adminAppointmentHistory);
            }
        });

        builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteAppointment(view, pushKey, adminAppointmentHistory, holder);
            }
        });

        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void doneMedicalTreatment(View view, String pushKey, AdminAppointmentHistory adminAppointmentHistory) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Attention!");
        builder.setMessage("Are you sure this appointment " + "\"" + adminAppointmentHistory.getId() + "\"" + " has done medical treatment? It will set this appointment into not paid status...");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AlertDialog.Builder builder2 = new AlertDialog.Builder(view.getContext());
                builder2.setTitle("Total Price");
                builder2.setCancelable(false);

                final EditText etTotalPrice = new EditText(view.getContext());
                etTotalPrice.setInputType(InputType.TYPE_CLASS_NUMBER);
                etTotalPrice.setPadding(80, 80, 80, 40);
                etTotalPrice.setHint("RM");
                builder2.setView(etTotalPrice);
                builder2.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String messageNotPaid = "Please paid medical fee as soon as possible, thank you!";
                        String getPrice = etTotalPrice.getText().toString().trim();
                        Float convertPrice = Float.valueOf(getPrice);

                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("status" , "not paid");
                        childUpdates.put("message" , messageNotPaid);
                        childUpdates.put("price", String.format("%.2f", convertPrice));
                        mReference = FirebaseDatabase.getInstance().getReference().child("AppointmentMade").child(thisAppointmentUID).child(pushKey);
                        mReference.updateChildren(childUpdates);

                        Toast.makeText(view.getContext(), "Not paid status set!", Toast.LENGTH_SHORT).show();
                    }
                });
                builder2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

                AlertDialog alertDialog2 = builder2.create();
                alertDialog2.show();
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

    private void deleteAppointment(View view, String pushKey, AdminAppointmentHistory adminAppointmentHistory, MyViewHolder holder) {
        String appointmentID = adminAppointmentHistory.getId();

        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Attention!");
        builder.setMessage("Are you sure want to delete this appointment " + "\"" + appointmentID + "\"" + " ");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                reference2 = FirebaseDatabase.getInstance().getReference("AppointmentMade").child(thisAppointmentUID).child(pushKey);
                reference2.removeValue();

                historyList.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());

                Toast.makeText(view.getContext(), "Appointment: " + appointmentID + "has been delete successfully!" , Toast.LENGTH_SHORT).show();
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
}
