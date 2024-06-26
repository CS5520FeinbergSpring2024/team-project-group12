//package edu.northeastern.group12_finalproject;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.annotation.SuppressLint;
//import android.app.ProgressDialog;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.BitmapFactory;
//import android.graphics.Canvas;
//import android.graphics.Matrix;
//import android.graphics.drawable.BitmapDrawable;
//import android.graphics.drawable.Drawable;
//import android.media.ExifInterface;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.Manifest;
//import android.widget.ProgressBar;
//import android.widget.Toast;
//import android.graphics.Bitmap;
//
//
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.material.snackbar.Snackbar;
//import com.google.firebase.FirebaseApp;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;
//import com.google.firebase.storage.UploadTask;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.UUID;
//
//public class ImageUploadActivity extends AppCompatActivity {
//
//    private static final int PERMISSION_REQUEST = 0;
//    private static final int PERMISSION_REQUEST_CAMERA = 2;
//    private static final int REQUEST_IMAGE_CAPTURE = 3;
//    private ImageView uploadedPic;
//    private Uri imageUri;
//    private Bitmap capturedBitmap;
////    private FirebaseDatabase appDB;
////    private FirebaseStorage storage;
////    private StorageReference storageRef;
////    private DatabaseReference postsRef;
////    private ProgressBar pb;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_image_upload);
//        uploadedPic = findViewById(R.id.imageView);
//
////        // add progress bar for image upload
////        pb = findViewById(R.id.progressBar);
////        pb.setVisibility(View.INVISIBLE);
////
////        storage = FirebaseStorage.getInstance();
////        storageRef = storage.getReference();
////
////        // initialize Firebase app
////        FirebaseApp.initializeApp(this);
//        // automatically launch camera intent once Activity launched
//        startCameraIntent();
//
////        Button cameraBtn = findViewById(R.id.cameraBtn);
////        cameraBtn.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                startCameraIntent();
////            }
////        });
//
//        /**
//         * This method takes the image captured from the camera and sets it into the ImageView in the
//         * CreatePostActivity to be shown with other Post input values
//         */
////        Button addToPostBtn = findViewById(R.id.addToPost);
////        addToPostBtn.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                // Check if the uploadedPic ImageView is not null and has a drawable (when captured via camera):
////                if (uploadedPic.getDrawable() != null) {
////                    Drawable drawable = uploadedPic.getDrawable();
////                    if (drawable instanceof BitmapDrawable) {
////                        // If the drawable is a bitmap, pass it directly
////                        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
////                        Intent intent = new Intent(ImageUploadActivity.this, CreatePostActivity.class);
////                        intent.putExtra("uploaded_image", bitmap);
////                        startActivity(intent);
////                    }
////                } else {
////                    Toast.makeText(ImageUploadActivity.this, "No image found", Toast.LENGTH_SHORT).show();
////                }
////            }
////        });
//    }
//
//    private void startCameraIntent() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
//                return;
//            }
//        }
//        // if camera permission is granted, start camera intent
//        dispatchTakePictureIntent();
//    }
//
//    // method to start camera intent
//    private void dispatchTakePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//        } else {
//            // This Toast appears on my emulator when I try to take picture with camera
//            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    // Show message if permissions not granted
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case PERMISSION_REQUEST_CAMERA:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    dispatchTakePictureIntent();
//                } else {
//                    Toast.makeText(this, "Camera permission not granted", Toast.LENGTH_SHORT).show();
//                }
//                break;
//            case PERMISSION_REQUEST:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    return;
//                    // choosePicture();
//                } else {
//                    Toast.makeText(this, "Gallery permission not granted", Toast.LENGTH_LONG).show();
//                }
//                break;
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK) {
//            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
//                // Camera intent result
//                Bundle extras = data.getExtras();
//                if (extras != null && extras.containsKey("data")) {
//                    Bitmap imageBitmap = (Bitmap) extras.get("data");
//                    if (imageBitmap != null) {
//                        // set captured image to ImageView
//                        uploadedPic.setImageBitmap(imageBitmap);
//                        // set capturedBitmap variable
//                        capturedBitmap = imageBitmap;
//                        // set imageUri to null because image captured by camera doesn't have uri
//                        imageUri = null;
//
//                        // Automatically return the image to CreatePostActivity
//                        returnCapturedImage(imageBitmap);
//                    }
//                }
//            }
//        }
//    }
//
//    // Method to return captured image back to CreatePostActivity
//    private void returnCapturedImage(Bitmap bitmap) {
//        if (bitmap != null) {
//            Intent intent = new Intent();
//            intent.putExtra("uploaded_image", bitmap);
//            setResult(RESULT_OK, intent);
//            finish();
//        } else {
//            Toast.makeText(this, "No image captured", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    /**
//     * This logic below needs to be moved to CreatePostActivity to save the image as a Post object to the database
//     * only when all input values have been entered (and validated) by the user.
//     */
//
//    // Once the image is in the ImageView, upload it to the Database.
//    // Method to upload image to the realtime database as a Post object and upload the image to Firebase Storage (just to test that the image is uploaded correctly)
////    private void uploadPicture() {
////        if (imageUri != null || capturedBitmap != null) {
////            if (imageUri != null) {
////                // Image selected from gallery
////                // create random key for imageID
////                final String randomKey = UUID.randomUUID().toString();
////                StorageReference imageRef = storageRef.child("images/" + randomKey);
////
////                imageRef.putFile(imageUri)
////                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
////                            @Override
////                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
////                                // get download URL of the uploaded image
////                                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
////                                    @Override
////                                    public void onSuccess(Uri uri) {
////                                        String imageUrl = uri.toString();
////                                        // create new Post object with image
////                                        Post post = new Post("postId", "username", System.currentTimeMillis(), imageUrl, "title", "description", 0, 0.0f);
////
////                                        // get reference to Realtime DB
////                                        appDB = FirebaseDatabase.getInstance();
////                                        postsRef = appDB.getReference().child("posts");
////
////                                        // generate unique key for post
////                                        String postId = postsRef.push().getKey();
////
////                                        // save the post to the Realtime DB using key
////                                        postsRef.child(postId).setValue(post)
////                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
////                                                    @Override
////                                                    public void onSuccess(Void unused) {
////                                                        pb.setVisibility(View.INVISIBLE);
////                                                        Toast.makeText(ImageUploadActivity.this, "Post saved!", Toast.LENGTH_SHORT).show();
////                                                    }
////                                                })
////                                                .addOnFailureListener(new OnFailureListener() {
////                                                    @Override
////                                                    public void onFailure(@NonNull Exception e) {
////                                                        Toast.makeText(ImageUploadActivity.this, "Failed to post.", Toast.LENGTH_SHORT).show();
////                                                    }
////                                                });
////                                    }
////                                });
////                                Snackbar.make(findViewById(android.R.id.content), "Image uploaded.", Snackbar.LENGTH_SHORT).show();
////                            }
////                        })
////                        .addOnFailureListener(new OnFailureListener() {
////                            @Override
////                            public void onFailure(@NonNull Exception e) {
////                                Toast.makeText(getApplicationContext(), "Upload failed.", Toast.LENGTH_SHORT).show();
////                            }
////                        });
////            } else if (capturedBitmap != null) {
////                // Image captured by the camera
////                // Convert capturedBitmap to byte array
////                ByteArrayOutputStream baos = new ByteArrayOutputStream();
////                capturedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
////                byte[] data = baos.toByteArray();
////
////                // create random key for imageID
////                final String randomKey = UUID.randomUUID().toString();
////                StorageReference imageRef = storageRef.child("images/" + randomKey);
////
////                // Upload byte array to Firebase Storage
////                UploadTask uploadTask = imageRef.putBytes(data);
////                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
////                    @Override
////                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
////                        // Get the download URL
////                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
////                            @Override
////                            public void onSuccess(Uri uri) {
////                                String imageUrl = uri.toString();
////                                // create new Post object with image
////                                Post post = new Post("postId", "username", System.currentTimeMillis(), imageUrl,"title", "description", 0, 0.0f);
////
////                                // get reference to Realtime DB
////                                appDB = FirebaseDatabase.getInstance();
////                                postsRef = appDB.getReference().child("posts");
////
////                                // generate unique key for post
////                                String postId = postsRef.push().getKey();
////
////                                // save the post to the Realtime DB using key
////                                postsRef.child(postId).setValue(post)
////                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
////                                            @Override
////                                            public void onSuccess(Void unused) {
////                                                pb.setVisibility(View.INVISIBLE);
////                                                Toast.makeText(ImageUploadActivity.this, "Post saved!", Toast.LENGTH_SHORT).show();
////                                            }
////                                        })
////                                        .addOnFailureListener(new OnFailureListener() {
////                                            @Override
////                                            public void onFailure(@NonNull Exception e) {
////                                                Toast.makeText(ImageUploadActivity.this, "Failed to post.", Toast.LENGTH_SHORT).show();
////                                            }
////                                        });
////                            }
////                        });
////                        Snackbar.make(findViewById(android.R.id.content), "Image uploaded.", Snackbar.LENGTH_SHORT).show();
////                    }
////                }).addOnFailureListener(new OnFailureListener() {
////                    @Override
////                    public void onFailure(@NonNull Exception e) {
////                        Toast.makeText(getApplicationContext(), "Upload failed.", Toast.LENGTH_SHORT).show();
////                    }
////                });
////            }
////        } else {
////            Toast.makeText(getApplicationContext(), "Please select an image first", Toast.LENGTH_SHORT).show();
////        }
////    }
//
//}