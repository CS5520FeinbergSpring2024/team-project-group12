package edu.northeastern.group12_finalproject;

import static android.content.ContentValues.TAG;

import static java.security.AccessController.getContext;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private Bitmap capturedBitmap;

    EditText nameET;
    EditText newPassword;
    Button confirmBtn, exitBtn;
    CircleImageView profileImage;
    TextView  changeImage;
    Uri imageUri;
    FirebaseStorage storage;
    StorageReference storageReference;

    private static final int PERMISSION_REQUEST = 0;
    private static final int PERMISSION_REQUEST_READ_MEDIA_IMAGES = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private static final int PERMISSION_REQUEST_CAMERA = 3;
    private static final int REQUEST_IMAGE_CAPTURE = 4;

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
        retrieveProfilePhoto();
        changeImage = findViewById(R.id.change_profile_image_btn);
        changeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Request Permissions to access phone's image gallery:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);

                } else {
//                 Permission already granted or not required, open gallery to choose picture
                    selectImage();
                }
            }
        });

        // Retrieve the bitmap from the intent
        if (getIntent().hasExtra("uploaded_image")) {
            Bitmap bitmap = getIntent().getParcelableExtra("uploaded_image");
            if (bitmap != null) {
                capturedBitmap = bitmap;
                profileImage.setImageBitmap(bitmap);
            } else {
                Toast.makeText(this, "Failed to retrieve image", Toast.LENGTH_SHORT).show();
            }
        } else if (getIntent().hasExtra("uploaded_image_uri")) {
            // Retrieve the image URI from the intent
            String imageUriString = getIntent().getStringExtra("uploaded_image_uri");
            Uri imageUri = Uri.parse(imageUriString);
            profileImage.setImageURI(imageUri);
        }


    }

    private void selectImage() {
        // Request Permissions to access phone's image gallery:
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
//
//        } else {
            // Permission already granted or not required, open gallery to choose picture
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//
//        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Rachel's code
//        if(requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            imageUri = data.getData();
//            profileImage.setImageURI(imageUri);
//            uploadImage();
//        }

        // Nicole's code
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            // Image selected successfully, handle it here
            imageUri = data.getData();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                bitmap = rotateImageIfRequired(bitmap, imageUri);
                profileImage.setImageBitmap(bitmap);
                uploadImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    // Handle chosen image being uploaded to ImageView sideways:
    private Bitmap rotateImageIfRequired(Bitmap bitmap, Uri selectedImage) throws IOException {
        InputStream input = getContentResolver().openInputStream(selectedImage);
        ExifInterface exif = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (input != null) {
                exif = new ExifInterface(input);
            }
        }
        if (exif == null) {
            return bitmap;
        }
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int rotationInDegrees = exifToDegrees(orientation);
        if (rotationInDegrees == 0) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationInDegrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("PermissionDebug", "Request Code: " + requestCode);
        if (requestCode == PERMISSION_REQUEST_READ_MEDIA_IMAGES || requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, launch gallery
                selectImage();
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Gallery permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Puts the intended image to firebase storage and sync under a profilePhoto node.
     */
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

                        DatabaseReference profileReference = FirebaseDatabase.getInstance().getReference().child("profilePhoto");

//                        // generate unique key for post
//                        post.setPostId(user.getUid());
                        HashMap<String, String> imageMap = new HashMap<>();
                        imageMap.put("profile_photo_Uri", imageUri.toString());
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                        imageMap.put("uid", uid);
                        imageMap.put("email", email);
                        // save the post to the Realtime DB using key
                        profileReference.child(uid).setValue(imageMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
//                                        // Hide progress bar after successful upload
//                                        pb.setVisibility(View.INVISIBLE);
                                        Toast.makeText(EditProfileActivity.this, "Post saved!", Toast.LENGTH_SHORT).show();
                                        // navigate back to the MainFeed
                                        Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
//                                        // Hide progress bar after upload failure
//                                        pb.setVisibility(View.INVISIBLE);
                                        Toast.makeText(EditProfileActivity.this, "Failed to post.", Toast.LENGTH_SHORT).show();
                                    }
                                });
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
                && (newPassword.getText().toString().trim().isEmpty()) && (imageUri == null)) {
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

    private void retrieveProfilePhoto() {
        DatabaseReference profileRf = FirebaseDatabase.getInstance().getReference().child("profilePhoto");
        user = FirebaseAuth.getInstance().getCurrentUser();
        Query query = profileRf.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.exists()) {
                        String profilePhoto = ds.child("profile_photo_Uri").getValue(String.class);
                        // The profile image
//                        circleImageView = findViewById(R.id.profile_image);

                        Picasso.get()
                                .load(profilePhoto)
                                .into(profileImage);
//                        profileImageUri = Uri.parse(profilePhoto);
//
//                        circleImageView.setImageURI(profileImageUri);
                        // Load image using Picasso
//                                Picasso.get()
//                                .load(post.getImageUrl())
//                                .into(holder.postImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}