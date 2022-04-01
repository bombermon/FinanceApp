package com.veselovski.financeapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.anychart.AnyChartView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.MutableDateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;




public class PieActivity extends AppCompatActivity {

    private TextView maintext;

    private FirebaseAuth mAuth;
    private DatabaseReference ref, personalRef;
    private String onlineUserId = "";
    private ProgressDialog loader;
    private AnyChartView anyChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pie);


        getSupportActionBar().setTitle("Операции");

        maintext = findViewById(R.id.totalspent);

        anyChartView = findViewById(R.id.anyChartView);
        mAuth = FirebaseAuth.getInstance();
        onlineUserId = mAuth.getCurrentUser().getUid();
        ref = FirebaseDatabase.getInstance().getReference().child("expenses").child(onlineUserId);
        loader = new ProgressDialog(this);
        personalRef = FirebaseDatabase.getInstance().getReference("personal").child(onlineUserId);




        getTotalMonthEntertainmentExpenses();
        getTotalWeekSpending();

        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {

                    }
                },
                2000
        );

    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.account){
            Intent intent = new Intent(PieActivity.this, MainActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    private void getTotalMonthEntertainmentExpenses(){
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); //Set to Epoch time
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now);

        String itemNmonth = "Развлечение"+months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNmonth").equalTo(itemNmonth);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds :  snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        maintext.setText("Spent: " + totalAmount);
                    }
                    personalRef.child("monthEnt").setValue(totalAmount);
                }else {
                    personalRef.child("monthEnt").setValue(0);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PieActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalWeekSpending(){
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); //Set to Epoch time
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("month").equalTo(months.getMonths());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    int totalAmount = 0;
                    for (DataSnapshot ds :  dataSnapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount+=pTotal;

                    }
                }else {
                    anyChartView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}