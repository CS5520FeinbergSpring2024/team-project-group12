package edu.northeastern.group12_finalproject;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
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
    EditText newPassword;
    Button confirmBtn, exitBtn;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        nameET = findViewById(R.id.edit_name);
        newPassword = findViewById(R.id.change_password);
        exitBtn = findViewById(R.id.exit_button);
        confirmBtn = findViewById(R.id.confirm_button);

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int exitCode = retrieveFirebaseInfo();
                if (exitCode == 0) {
                    startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
                    finish();
                }
                else if (exitCode == 1) {
                    Snackbar snackbar = Snackbar.make(v, "Please fill up at least one field before confirming", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                else if (exitCode == 2) {
                    Snackbar snackbar2 = Snackbar.make(v, "New password should be at least 6 characters", Snackbar.LENGTH_LONG);
                    snackbar2.show();
                }
            }
        });
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // Retrieve user info from firebase.
    private int retrieveFirebaseInfo() {
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");

        Log.d(TAG, "Inside retrieve FirebaseInfo");

        // Initial View set up.
        if ((nameET.getText().toString().trim().isEmpty())
                && (newPassword.getText().toString().trim().isEmpty())) {
            return 1;
        }

        if (newPassword.getText().toString().length() < 6) {
//            Toast.makeText(EditProfileActivity.this, "New password should be at least 6 characters", Toast.LENGTH_LONG);
            return 2;
        }

        Query userQuery = databaseReference.orderByChild("email").equalTo(user.getEmail());

        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (!(nameET.getText().toString().isEmpty())) {
                        dataSnapshot.child("username").getRef().setValue(nameET.getText().toString());
                    }
                    if (!(newPassword.getText().toString().isEmpty())) {
                        firebaseAuth.getCurrentUser().updatePassword(newPassword.getText().toString());
                    }
                }
            }
            @Override
            public void onCancelled (@NonNull DatabaseError error){

            }
        });
        return 0;
    }
}