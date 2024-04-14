package edu.northeastern.group12_finalproject;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    EditText nameET;
    EditText newPassword;
    Button confirmBtn, exitBtn;
    CircleImageView profileImage;
    TextView  changeImage;
    Uri imageUri;
    FirebaseStorage storage;
    StorageReference storageReference;

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
                    Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
                    intent.putExtra("imageUri", imageUri.toString());
                    startActivity(intent);
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
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        profileImage = findViewById(R.id.profile_image_change);
        changeImage = findViewById(R.id.change_profile_image_btn);
        changeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
            uploadImage();
        }
    }

    private void uploadImage() {
        final String randomKey = UUID.randomUUID().toString();
        StorageReference riverRef = storageReference.child("profiles/" + randomKey);
        riverRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Image uploaded successfully, now get the download URL
                        riverRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {
                                // Save the download URL to Firebase Realtime Database
                                saveImageUrlToDatabase(downloadUri.toString());
                            }
                        });
                        Snackbar.make(findViewById(android.R.id.content), "Image uploaded", Snackbar.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to Upload", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveImageUrlToDatabase(String imageUrl) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            userRef.child("profileImageUrl").setValue(imageUrl);
        }
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
                    // Upload profile image if imageUri is not null
                    if (imageUri != null) {
                        uploadImage();
                    } else {
                        // If no image is selected, finish the activity
                        finish();
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