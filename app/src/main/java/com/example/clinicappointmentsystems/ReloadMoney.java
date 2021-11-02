package com.example.clinicappointmentsystems;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ReloadMoney extends AppCompatActivity {
    private DatabaseReference mReference;

    private EditText etAmount, etPassword;
    private Button btnReload;
    private ProgressBar reloadMoneyProgressBar;

    private String userID, phone, password, balance, verifyID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reload_money);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        Intent i = getIntent();
        userID = i.getStringExtra("userID");
        phone = i.getStringExtra("phone");
        password = i.getStringExtra("password");
        balance = i.getStringExtra("balance");

        etAmount = (EditText) findViewById(R.id.editTextAmount);
        etPassword = (EditText) findViewById(R.id.editTextPassword);
        reloadMoneyProgressBar = (ProgressBar) findViewById(R.id.reloadMoneyProgressBar);
        btnReload = (Button) findViewById(R.id.btnReload);
        btnReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reloadMoney();
            }
        });
    }

    private void reloadMoney() {
        String amountInput = etAmount.getText().toString().trim();
        String passwordInput = etPassword.getText().toString().trim();

        if (amountInput.isEmpty()){
            etAmount.setError("This field cannot be empty!");
            etAmount.requestFocus();
            return;
        } else if (Integer.parseInt(amountInput) < 10){
            etAmount.setError("Please input amount at least RM10!");
            etAmount.requestFocus();
            return;
        } else if (Integer.parseInt(amountInput) > 1000){
            etAmount.setError("Please input amount at most RM1000!");
            etAmount.requestFocus();
            return;
        } else {
            etAmount.setError(null);
        }

        if (passwordInput.isEmpty()){
            etPassword.setError("This field cannot be empty!");
            etPassword.requestFocus();
            return;
        } else if (passwordInput.length() < 8){
            etPassword.setError("Password must contain at least 8 character!");
            etPassword.requestFocus();
            return;
        } else if (!passwordInput.equals(password)){
            etPassword.setError("Invalid password input!");
            etPassword.requestFocus();
            return;
        } else {
            etPassword.setError(null);
        }

        reloadMoneyProgressBar.setVisibility(View.VISIBLE);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+60" + phone,
                10,
                TimeUnit.SECONDS,
                ReloadMoney.this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        reloadMoneyProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        reloadMoneyProgressBar.setVisibility(View.GONE);
                        Toast.makeText(ReloadMoney.this, "Something went wrong! Please try again!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationID, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        reloadMoneyProgressBar.setVisibility(View.GONE);
                        verifyID = verificationID;
                        verificationIDInput();
                    }
                }
        );
    }

    private void verificationIDInput() {
        final EditText etVerificationID = new EditText(ReloadMoney.this);
        etVerificationID.setInputType(InputType.TYPE_CLASS_NUMBER);
        etVerificationID.setPadding(100, 100, 100, 50);

        AlertDialog.Builder builder = new AlertDialog.Builder(ReloadMoney.this);
        builder.setTitle("Please enter the verification number!");
        builder.setView(etVerificationID);
        builder.setCancelable(false);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(
                        verifyID, etVerificationID.getText().toString().trim());

                FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                reloadMoneyProgressBar.setVisibility(View.GONE);

                                if (task.isSuccessful()) {
                                    String amountInput = etAmount.getText().toString().trim();
                                    Float convertBalance = Float.valueOf(balance);
                                    Float convertAmount = Float.valueOf(amountInput);
                                    Float totalBalance = convertBalance + convertAmount;
                                    String convertTotalBalance = String.format("%.2f", totalBalance);

                                    Map<String, Object> childUpdates = new HashMap<>();
                                    childUpdates.put("balance" , convertTotalBalance);
                                    mReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
                                    mReference.updateChildren(childUpdates);

                                    AlertDialog.Builder builder = new AlertDialog.Builder(ReloadMoney.this);
                                    builder.setTitle("Reload money");
                                    builder.setMessage("Total Balance: " + convertTotalBalance);
                                    builder.setCancelable(false);
                                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Intent a = new Intent(ReloadMoney.this, Homepage.class);
                                            a.putExtra("totalBalance", convertTotalBalance);
                                            setResult(RESULT_OK, a);
                                            finish();
                                        }
                                    });

                                    AlertDialog alertDialog = builder.create();
                                    alertDialog.show();
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ReloadMoney.this);
                                    builder.setTitle("Attention!");
                                    builder.setMessage("Failed to reload money, please try again later!");
                                    builder.setCancelable(false);
                                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.cancel();
                                        }
                                    });

                                    AlertDialog alertDialog = builder.create();
                                    alertDialog.show();
                                }
                            }
                        });
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}