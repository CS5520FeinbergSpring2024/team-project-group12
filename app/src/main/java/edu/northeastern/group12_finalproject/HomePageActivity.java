package edu.northeastern.group12_finalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.Manifest;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.database.Cursor;
import android.graphics.Bitmap;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class HomePageActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST = 0;
    //private static final int RESULT_LOAD_IMAGE = 1;
    private ImageView uploadedPic;
    private Uri imageUri;
    private FirebaseDatabase appDB;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private DatabaseReference postsRef;
    //private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        uploadedPic = findViewById(R.id.imageView);
        Button uploadBtn = findViewById(R.id.upload);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePicture();
            }
        });

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // initialize Firebase app
        FirebaseApp.initializeApp(this);

        // Request Permissions:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
        }

        Button saveBtn = findViewById(R.id.saveButton);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPicture();
            }
        });
    }

    private void choosePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                bitmap = rotateImageIfRequired(bitmap, imageUri);
                uploadedPic.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // uploadedPic.setImageURI(imageUri);
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

    private void uploadPicture() {
        if (imageUri != null) {
            // create random key for imageID
            final String randomKey = UUID.randomUUID().toString();
            StorageReference imageRef = storageRef.child("images/" + randomKey);

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // get download URL of the uploaded image
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    // create new Post object with image
                                    Post post = new Post("postId", "username", System.currentTimeMillis(), imageUrl, "description", 0, 0.0f);

                                    // get reference to Realtime DB
                                    appDB = FirebaseDatabase.getInstance();
                                    postsRef = appDB.getReference().child("posts");

                                    // generate unique key for post
                                    String postId = postsRef.push().getKey();

                                    // save the post to the Realtime DB using key
                                    postsRef.child(postId).setValue(post)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Toast.makeText(HomePageActivity.this, "Post saved!", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(HomePageActivity.this, "Failed to post.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });
                            Snackbar.make(findViewById(android.R.id.content), "Image uploaded.", Snackbar.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Upload failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "Please select an image first", Toast.LENGTH_SHORT).show();
        }
    }


    //    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_home_page);
//
//        // connect with Firebase
//        appDB = FirebaseDatabase.getInstance();
//        postsRef = appDB.getReference().child("posts");
//        storage = FirebaseStorage.getInstance();
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
//        }
//
//        imageView = findViewById(R.id.imageView);
//        Button uploadBtn = findViewById(R.id.upload);
//
//        uploadBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_PICK,
//                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                startActivityForResult(intent, RESULT_LOAD_IMAGE);
//            }
//        });
//
//        // set up save button to save image to DB
//        Button saveButton = findViewById(R.id.saveButton);
//        saveButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                saveImageToDB();
//            }
//        });
//    }
//
//    // method to save image to Firebase storage and database
//    private void saveImageToDB() {
//        // get image uri
//        Uri imageUri = getImageUri();
//        if (imageUri != null) {
//            uploadImageToStorage(imageUri);
//        } else {
//            Toast.makeText(this, "No image selected.", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    // method to get the Uri of the selected image
//    private Uri getImageUri() {
//        return selectedImageUri;
//    }
//
//    // method to upload image to Firebase storage
//    private void uploadImageToStorage(Uri imageUri) {
//        // get reference to storage location
//        StorageReference storageReference = storage.getReference().child("posts" + System.currentTimeMillis());
//
//        // upload image to firebase
//        UploadTask uploadTask = storageReference.putFile(imageUri);
//        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                // image successfully uploaded, get download URL of image
//                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                    @Override
//                    public void onSuccess(Uri uri) {
//                        String imageUrl = uri.toString();
//                        // save image URL to database
//                        saveImageUrlToDB(imageUrl);
//                    }
//                });
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                // image upload failed
//                Toast.makeText(HomePageActivity.this, "Failed to save image", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    // method to save image URL to database
//    private void saveImageUrlToDB(String imageUrl) {
//        // create new Post object with image URL
//        Post post = new Post("postId", "username", System.currentTimeMillis(), "https://example.com/image.jpg", "description", 0, 0.0f);
//
//        post.setImageUrl(imageUrl);
//
//        // generate unique key for post
//        String postId = postsRef.push().getKey();
//
//        // save post to DB using unique key
//        postsRef.child(postId).setValue(post)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
//                        Toast.makeText(HomePageActivity.this, "Image saved to DB", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(HomePageActivity.this, "Failed to save image.", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    @SuppressLint("MissingSuperCall")
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case PERMISSION_REQUEST:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
//                }
//                break;
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case RESULT_LOAD_IMAGE:
//                if (resultCode == RESULT_OK && data != null) {
//                    Uri selectedImage = data.getData();
//                    // load selected image into ImageView
//                    selectedImageUri = data.getData();
//                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
//                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
//                    if (cursor != null) {
//                        cursor.moveToFirst();
//                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//                        String picturePath = cursor.getString(columnIndex);
//                        cursor.close();
//                        // handle image orientation (stop photo from being uploaded sideways)
//                        Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
//                        try {
//                            bitmap = rotateImageIfRequired(bitmap, selectedImage);
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//                        imageView.setImageBitmap(bitmap);
//                    }
//                }
//                break;
//        }
//    }
//
//    // At first, the image was uploaded sideways - needed to handle case where image's orientation was being incorrectly interpreted
//    private Bitmap rotateImageIfRequired(Bitmap bitmap, Uri selectedImage) throws IOException {
//        try {
//            InputStream input = getContentResolver().openInputStream(selectedImage);
//            ExifInterface ei;
//            if (Build.VERSION.SDK_INT > 23) {
//                ei = new ExifInterface(input);
//            } else {
//                ei = new ExifInterface(selectedImage.getPath());
//            }
//            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
//
//            switch (orientation) {
//                case ExifInterface.ORIENTATION_ROTATE_90:
//                    return rotateImage(bitmap, 90);
//                case ExifInterface.ORIENTATION_ROTATE_180:
//                    return rotateImage(bitmap, 180);
//                case ExifInterface.ORIENTATION_ROTATE_270:
//                    return rotateImage(bitmap, 270);
//                default:
//                    return bitmap;
//            }
//        } catch (IOException e) {
//            // Handle IOException here
//            e.printStackTrace();
//            return bitmap; // return original bitmap if error
//        }
//
//    }
//
//    // rotate image before uploading it so that orientation is shown correctly on the screen
//    private Bitmap rotateImage(Bitmap bitmap, float angle) {
//        Matrix matrix = new Matrix();
//        matrix.postRotate(angle);
//        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//    }

}