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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.anychart.enums.Align;
import com.anychart.enums.LegendLayout;
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
import java.util.Locale;
import java.util.Map;




public class PieActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private DatabaseReference ref, personalRef;
    private String onlineUserId = "";
    private ProgressDialog loader;
    private AnyChartView anyChartView;
    private Integer countermonth = 0 ; // отвечает за минусовку месяцев
    private Button minus;
    private Button plus;
    private Pie pie;
    public static boolean pieflag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pie);





        anyChartView = findViewById(R.id.anyChartView);
        mAuth = FirebaseAuth.getInstance();
        onlineUserId = mAuth.getCurrentUser().getUid();
        ref = FirebaseDatabase.getInstance().getReference().child("expenses").child(onlineUserId);
        loader = new ProgressDialog(this);
        personalRef = FirebaseDatabase.getInstance().getReference("personal").child(onlineUserId);
        pie = AnyChart.pie();
        minus = findViewById(R.id.minus);
        plus = findViewById(R.id.plus);


        Integer minusmonth = 0;
        getTotalMonthExpenses("Еда", "monthFood", minusmonth);
        getTotalMonthExpenses("Транспорт", "monthTran", minusmonth);
        getTotalMonthExpenses("Развлечения", "monthEnt", minusmonth);
        getTotalMonthExpenses("Интернет", "monthInt", minusmonth);
        getTotalMonthExpenses("Путешествия", "monthTravel", minusmonth);
        getTotalMonthExpenses("Одежда", "monthClo", minusmonth);
        getTotalMonthExpenses("Техника", "monthTech", minusmonth);
        getTotalMonthExpenses("Другое", "monthOth", minusmonth);

        getTotalMonthSpending(minusmonth);
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        loadGraph();
                    }
                },
                2000
        );


        anyChartView.setChart(pie);



        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countermonth ++;

                Toast.makeText(PieActivity.this, "Загрузка...", Toast.LENGTH_SHORT).show();
                getTotalMonthExpenses("Еда", "monthFood", countermonth);
                getTotalMonthExpenses("Транспорт", "monthTran", countermonth);
                getTotalMonthExpenses("Развлечения", "monthEnt", countermonth);
                getTotalMonthExpenses("Интернет", "monthInt", countermonth);
                getTotalMonthExpenses("Путешествия", "monthTravel", countermonth);
                getTotalMonthExpenses("Одежда", "monthClo", countermonth);
                getTotalMonthExpenses("Техника", "monthTech", countermonth);
                getTotalMonthExpenses("Другое", "monthOth", countermonth);

                getTotalMonthSpending(countermonth);
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                reloadGraph(countermonth);
                            }
                        },
                        2000
                );
                anyChartView.setChart(pie);

            }
        });

        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countermonth --;
                Toast.makeText(PieActivity.this, "Загрузка...", Toast.LENGTH_LONG).show();

                getTotalMonthExpenses("Еда", "monthFood", countermonth);
                getTotalMonthExpenses("Транспорт", "monthTran", countermonth);
                getTotalMonthExpenses("Развлечения", "monthEnt", countermonth);
                getTotalMonthExpenses("Интернет", "monthInt", countermonth);
                getTotalMonthExpenses("Путешествия", "monthTravel", countermonth);
                getTotalMonthExpenses("Одежда", "monthClo", countermonth);
                getTotalMonthExpenses("Техника", "monthTech", countermonth);
                getTotalMonthExpenses("Другое", "monthOth", countermonth);

                getTotalMonthSpending(countermonth);
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                reloadGraph(countermonth);
                            }
                        },
                        2000
                );
                anyChartView.setChart(pie);
            }
        });



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


    private void getTotalMonthExpenses(String name, String addition, Integer minus){
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime().minusMonths(minus);
        Months months = Months.monthsBetween(epoch, now);



        String itemNmonth = name+months.getMonths();

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
                    }
                    personalRef.child(addition).setValue(totalAmount);
                }else {
                    personalRef.child(addition).setValue(0);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PieActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void getTotalMonthSpending(Integer minus){
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); //Set to Epoch time
        DateTime now = new DateTime().minusMonths(minus);
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
                    Toast.makeText(PieActivity.this,"За этот месяц нет расходов", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void loadGraph(){
        personalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (pieflag) {
                if (snapshot.exists()) {

                    int traTotal;
                    if (snapshot.hasChild("monthTran")) {
                        traTotal = Integer.parseInt(snapshot.child("monthTran").getValue().toString());
                    } else {
                        traTotal = 0;
                    }

                    int foodTotal;
                    if (snapshot.hasChild("monthFood")) {
                        foodTotal = Integer.parseInt(snapshot.child("monthFood").getValue().toString());
                    } else {
                        foodTotal = 0;
                    }

                    int entTotal;
                    if (snapshot.hasChild("monthEnt")) {
                        entTotal = Integer.parseInt(snapshot.child("monthEnt").getValue().toString());
                    } else {
                        entTotal = 0;
                    }

                    int intTotal;
                    if (snapshot.hasChild("monthInt")) {
                        intTotal = Integer.parseInt(snapshot.child("monthInt").getValue().toString());
                    } else {
                        intTotal = 0;
                    }

                    int TravTotal;
                    if (snapshot.hasChild("monthTravel")) {
                        TravTotal = Integer.parseInt(snapshot.child("monthTravel").getValue().toString());
                    } else {
                        TravTotal = 0;
                    }


                    int CloTotal;
                    if (snapshot.hasChild("monthClo")) {
                        CloTotal = Integer.parseInt(snapshot.child("monthClo").getValue().toString());
                    } else {
                        CloTotal = 0;
                    }

                    int TechTotal;
                    if (snapshot.hasChild("monthTech")) {
                        TechTotal = Integer.parseInt(snapshot.child("monthTech").getValue().toString());
                    } else {
                        TechTotal = 0;
                    }

                    int othTotal;
                    if (snapshot.hasChild("monthOth")) {
                        othTotal = Integer.parseInt(snapshot.child("monthOth").getValue().toString());
                    } else {
                        othTotal = 0;
                    }


                    List<DataEntry> data = new ArrayList<>();
                    data.add(new ValueDataEntry("Транспорт", traTotal));
                    data.add(new ValueDataEntry("Еда", foodTotal));
                    data.add(new ValueDataEntry("Развлечение", entTotal));
                    data.add(new ValueDataEntry("Интернет", intTotal));
                    data.add(new ValueDataEntry("Путешествия", TravTotal));
                    data.add(new ValueDataEntry("Одежда", CloTotal));
                    data.add(new ValueDataEntry("Техника", TechTotal));
                    data.add(new ValueDataEntry("Другое", othTotal));


                        pie.data(data);

                        pie.title("Аналитика текущего месяца");

                        pie.labels().position("inside");

                        pie.legend().title().enabled(true);
                        pie.legend().title()
                                .text("Категории")
                                .padding(0d, 0d, 10d, 0d);

                        pie.legend()
                                .position("center-bottom")
                                .itemsLayout(LegendLayout.HORIZONTAL)
                                .align(Align.CENTER);
                        pieflag = false;

                } else {
                    Toast.makeText(PieActivity.this, "Child does not exist", Toast.LENGTH_SHORT).show();
                }
            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void reloadGraph(int minus){
        personalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){

                    int traTotal;
                    if (snapshot.hasChild("monthTran")){
                        traTotal = Integer.parseInt(snapshot.child("monthTran").getValue().toString());
                    }else {
                        traTotal = 0;
                    }

                    int foodTotal;
                    if (snapshot.hasChild("monthFood")){
                        foodTotal = Integer.parseInt(snapshot.child("monthFood").getValue().toString());
                    }else {
                        foodTotal = 0;
                    }

                    int entTotal;
                    if (snapshot.hasChild("monthEnt")){
                        entTotal = Integer.parseInt(snapshot.child("monthEnt").getValue().toString());
                    }else {
                        entTotal=0;
                    }

                    int intTotal;
                    if (snapshot.hasChild("monthInt")){
                        intTotal = Integer.parseInt(snapshot.child("monthInt").getValue().toString());
                    }else {
                        intTotal=0;
                    }

                    int TravTotal;
                    if (snapshot.hasChild("monthTravel")){
                        TravTotal = Integer.parseInt(snapshot.child("monthTravel").getValue().toString());
                    }else {
                        TravTotal=0;
                    }


                    int CloTotal;
                    if (snapshot.hasChild("monthClo")){
                        CloTotal = Integer.parseInt(snapshot.child("monthClo").getValue().toString());
                    }else {
                        CloTotal=0;
                    }

                    int TechTotal;
                    if (snapshot.hasChild("monthTech")){
                        TechTotal = Integer.parseInt(snapshot.child("monthTech").getValue().toString());
                    }else {
                        TechTotal=0;
                    }

                    int othTotal;
                    if (snapshot.hasChild("monthOth")){
                        othTotal = Integer.parseInt(snapshot.child("monthOth").getValue().toString());
                    }else {
                        othTotal = 0;
                    }


                    List<DataEntry> data = new ArrayList<>();
                    data.add(new ValueDataEntry("Транспорт", traTotal));
                    data.add(new ValueDataEntry("Еда", foodTotal));
                    data.add(new ValueDataEntry("Развлечение", entTotal));
                    data.add(new ValueDataEntry("Интернет", intTotal));
                    data.add(new ValueDataEntry("Путешествия", TravTotal));
                    data.add(new ValueDataEntry("Одежда", CloTotal));
                    data.add(new ValueDataEntry("Техника", TechTotal));
                    data.add(new ValueDataEntry("Другое", othTotal));


                    pie.data(data);

                    DateTime now = new DateTime().minusMonths(minus);
                    String titlemonth = now.toString("MMMM", new Locale("ru"));
                    pie.title("Аналитика " + titlemonth);

                    pie.labels().position("inside");

                    pie.legend().title().enabled(true);
                    pie.legend().title()
                            .text("Категории")
                            .padding(0d, 0d, 10d, 0d);

                    pie.legend()
                            .position("center-bottom")
                            .itemsLayout(LegendLayout.HORIZONTAL)
                            .align(Align.CENTER);



                }
                else {
                    Toast.makeText(PieActivity.this,"Child does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}