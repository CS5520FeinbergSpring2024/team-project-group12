package edu.northeastern.group12_finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class HomePageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        Button postBtn = findViewById(R.id.postButton);
        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewPost();
            }
        });
    }

    private void createNewPost() {
        Intent intent = new Intent(this, ImageUploadActivity.class);
        startActivity(intent);
    }

}