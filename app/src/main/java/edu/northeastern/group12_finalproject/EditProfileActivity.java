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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    EditText oldPassword;
    EditText newPassword;
    Button confirmBtn, exitBtn;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        nameET = findViewById(R.id.edit_name);
        oldPassword = findViewById(R.id.old_password);
        newPassword = findViewById(R.id.change_password);
        exitBtn = findViewById(R.id.exit_button);
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

        // Initial View set up.
        if ((nameET.getText().toString().isEmpty()) && (oldPassword.getText().toString().isEmpty())
                && (newPassword.getText().toString().isEmpty())) {
            Toast.makeText(this, "Please fill up at least one field before confirming", Toast.LENGTH_LONG);
            return 0;
        }
        if (oldPassword.getText().toString().isEmpty() && !(newPassword.getText().toString().isEmpty())) {
            Toast.makeText(this, "Please fill up new password before confirming", Toast.LENGTH_LONG);
            return 0;
        }
        if (!(oldPassword.getText().toString().isEmpty() && (newPassword.getText().toString().isEmpty()))) {
            Toast.makeText(this, "Please fill up new password before confirming", Toast.LENGTH_LONG);
            return 0;
        }

        Query userQuery = databaseReference.orderByChild("email").equalTo(user.getEmail());

        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (!(nameET.getText().toString().isEmpty())) {
                        dataSnapshot.child("username").getRef().setValue(nameET.getText().toString());
                    }
                    if (!(oldPassword.getText().toString().isEmpty()) && !(newPassword.getText().toString().isEmpty())) {
                        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                        firebaseAuth.signInWithEmailAndPassword(userEmail, oldPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    firebaseAuth.getCurrentUser().updatePassword(newPassword.getText().toString());
                                } else {
                                    String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                    switch (errorCode) {

                                        case "ERROR_INVALID_CUSTOM_TOKEN":
                                            Toast.makeText(EditProfileActivity.this, "The custom token format is incorrect. Please check the documentation.", Toast.LENGTH_LONG).show();
                                            break;

                                        case "ERROR_CUSTOM_TOKEN_MISMATCH":
                                            Toast.makeText(EditProfileActivity.this, "The custom token corresponds to a different audience.", Toast.LENGTH_LONG).show();
                                            break;

                                        case "ERROR_INVALID_CREDENTIAL":
                                            Toast.makeText(EditProfileActivity.this, "The supplied auth credential is malformed or has expired.", Toast.LENGTH_LONG).show();
                                            break;

                                        case "ERROR_INVALID_EMAIL":
                                            Toast.makeText(EditProfileActivity.this, "The email address is badly formatted.", Toast.LENGTH_LONG).show();
//                                            etEmail.setError("The email address is badly formatted.");
//                                            etEmail.requestFocus();
                                            break;

                                        case "ERROR_WRONG_PASSWORD":
                                            Toast.makeText(EditProfileActivity.this, "The password is invalid or the user does not have a password.", Toast.LENGTH_LONG).show();
//                                            etPassword.setError("password is incorrect ");
//                                            etPassword.requestFocus();
//                                            etPassword.setText("");
                                            break;

                                        case "ERROR_USER_MISMATCH":
                                            Toast.makeText(EditProfileActivity.this, "The supplied credentials do not correspond to the previously signed in user.", Toast.LENGTH_LONG).show();
                                            break;

                                        case "ERROR_REQUIRES_RECENT_LOGIN":
                                            Toast.makeText(EditProfileActivity.this, "This operation is sensitive and requires recent authentication. Log in again before retrying this request.", Toast.LENGTH_LONG).show();
                                            break;

                                        case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                                            Toast.makeText(EditProfileActivity.this, "An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address.", Toast.LENGTH_LONG).show();
                                            break;

                                        case "ERROR_EMAIL_ALREADY_IN_USE":
                                            Toast.makeText(EditProfileActivity.this, "The email address is already in use by another account.   ", Toast.LENGTH_LONG).show();
//                                            etEmail.setError("The email address is already in use by another account.");
//                                            etEmail.requestFocus();
                                            break;

                                        case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                                            Toast.makeText(EditProfileActivity.this, "This credential is already associated with a different user account.", Toast.LENGTH_LONG).show();
                                            break;

                                        case "ERROR_USER_DISABLED":
                                            Toast.makeText(EditProfileActivity.this, "The user account has been disabled by an administrator.", Toast.LENGTH_LONG).show();
                                            break;

                                        case "ERROR_USER_TOKEN_EXPIRED":
                                            Toast.makeText(EditProfileActivity.this, "The user\\'s credential is no longer valid. The user must sign in again.", Toast.LENGTH_LONG).show();
                                            break;

                                        case "ERROR_USER_NOT_FOUND":
                                            Toast.makeText(EditProfileActivity.this, "There is no user record corresponding to this identifier. The user may have been deleted.", Toast.LENGTH_LONG).show();
                                            break;

                                        case "ERROR_INVALID_USER_TOKEN":
                                            Toast.makeText(EditProfileActivity.this, "The user\\'s credential is no longer valid. The user must sign in again.", Toast.LENGTH_LONG).show();
                                            break;

                                        case "ERROR_OPERATION_NOT_ALLOWED":
                                            Toast.makeText(EditProfileActivity.this, "This operation is not allowed. You must enable this service in the console.", Toast.LENGTH_LONG).show();
                                            break;

                                        case "ERROR_WEAK_PASSWORD":
                                            Toast.makeText(EditProfileActivity.this, "The given password is invalid.", Toast.LENGTH_LONG).show();

                                            break;
                                    }


                                }
                            }
                        });
                    }
                }
            }
            @Override
            public void onCancelled (@NonNull DatabaseError error){

            }
        });


        return 1;
    }
}