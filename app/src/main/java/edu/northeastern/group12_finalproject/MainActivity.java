package edu.northeastern.group12_finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // test home button to test home page activity
        Button homeBtn = findViewById(R.id.button);
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { openHomePage(); }
        });
    }

    private void openHomePage() {
        Intent intent = new Intent(this, HomePageActivity.class);
        startActivity(intent);
    }
}