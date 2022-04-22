package com.veselovski.financeapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.analytics){
            Intent intent = new Intent(AboutActivity.this, PieActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.account){
            Intent intent = new Intent(AboutActivity.this, AccountActivity.class);
            startActivity(intent);
        }

        if (item.getItemId() == R.id.control){
            Intent intent = new Intent(AboutActivity.this, MainActivity.class);
            startActivity(intent);
        }


        return super.onOptionsItemSelected(item);
    }
}