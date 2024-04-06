package edu.northeastern.group12_finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class EditProfileActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    EditText nameET;
    EditText bioET;
    Button confirmBtn;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        nameET = findViewById(R.id.edit_name);
        bioET = findViewById(R.id.edit_bio);
        confirmBtn = findViewById(R.id.confirm_button);

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int exitCode = retrieveFirebaseInfo();
                if (exitCode == 1) {
                    startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
                    finish();
                }
            }
        });
    }

    // Retrieve user info from firebase.
    private int retrieveFirebaseInfo() {
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");

        // Initial View set up.
        if ((nameET.getText().toString().isEmpty()) && (bioET.getText().toString().isEmpty())) {
            Toast.makeText(this, "Name and bio cannot be null", Toast.LENGTH_LONG);
            return 0;
        }


        if (!(nameET.getText().toString().isEmpty())) {
            databaseReference.child(user.getUid()).child("username").setValue(nameET.getText().toString());
        }
        if (!(bioET.getText().toString().isEmpty())) {
            databaseReference.child(user.getUid()).child("bio").setValue(bioET.getText().toString());
        }
        return 1;



    }
}