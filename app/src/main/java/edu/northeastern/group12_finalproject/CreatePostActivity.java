package edu.northeastern.group12_finalproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class CreatePostActivity extends AppCompatActivity {

    private EditText duration;
    private EditText distance;
    private EditText location;
    private EditText editTextTitle;
    private EditText editTextDescription;

    private Button buttonAddImage;
    private Button buttonUploadPhoto;
    private Button post;
    private OnPostAddListener listener;
    private ImageView uploadedPic;
    private Uri imageUri;
    private Bitmap capturedBitmap;
    private FirebaseDatabase appDB;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private DatabaseReference postsRef;
    private ProgressBar pb;
    private static final int PERMISSION_REQUEST = 0;
    private static final int REQUEST_IMAGE_FROM_GALLERY = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // add progress bar for image upload
        pb = findViewById(R.id.progressBar);
        pb.setVisibility(View.INVISIBLE);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // initialize Firebase app
        FirebaseApp.initializeApp(this);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_nav_new_post);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.bottom_nav_home) {
                    openHomePage();
                } else if (item.getItemId() == R.id.bottom_nav_new_post) {
                    // do nothing, stay on page
                } else if (item.getItemId() == R.id.bottom_nav_profile) {
                    openProfilePage();

                }
                return false;
            }
        });
        duration = findViewById(R.id.duration_edit_text);
        distance = findViewById(R.id.distance_edit_text);
        editTextTitle = findViewById(R.id.post_title_edit_text);
        editTextDescription = findViewById(R.id.description_edit_text);
        location = findViewById(R.id.location_edit_text);

        // changed the text on this button to say "Take Photo". This button calls the ImageUploadActivity to start a camera intent
        buttonAddImage = findViewById(R.id.add_photo_button);
        buttonAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImageForPost();
            }
        });

        // add button to upload a photo that will allow the user to upload a photo from Gallery
        buttonUploadPhoto = findViewById(R.id.uploadPicBtn);
        buttonUploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Request Permissions to access phone's image gallery:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
                } else {
                    // else permission already granted, open gallery to choose picture
                    selectImageFromGallery();
                }
            }
        });

        // get the ImageView for the layout (either captured via camera (bitmap) or uploaded from gallery (uri))
        ImageView imageView = findViewById(R.id.postImageView);
        // Retrieve the bitmap from the intent
        if (getIntent().hasExtra("uploaded_image")) {
            Bitmap bitmap = getIntent().getParcelableExtra("uploaded_image");
            if (bitmap != null) {
                capturedBitmap = bitmap;
                imageView.setImageBitmap(bitmap);
            } else {
                Toast.makeText(this, "Failed to retrieve image", Toast.LENGTH_SHORT).show();
            }
        } else if (getIntent().hasExtra("uploaded_image_uri")) {
            // Retrieve the image URI from the intent
            String imageUriString = getIntent().getStringExtra("uploaded_image_uri");
            Uri imageUri = Uri.parse(imageUriString);
            imageView.setImageURI(imageUri);
        }

        post = findViewById(R.id.post_button);
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPostToDatabase();
            }
        });
    }

    // show Toast if Gallery permissions not granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectImageFromGallery(); // Call method to select image after permission is granted
                } else {
                    Toast.makeText(this, "Gallery permission not granted", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    // Method to select picture from device's gallery
    private void selectImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    private void captureImageForPost() {
        Intent intent = new Intent(CreatePostActivity.this, ImageUploadActivity.class);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    // Handle result: image either uploaded from or photo capture
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                // Check if the intent has a bitmap extra from ImageUploadActivity
                Bitmap capturedBitmap = data.getParcelableExtra("uploaded_image");
                if (capturedBitmap != null) {
                    // Set the captured bitmap to the ImageView
                    ImageView imageView = findViewById(R.id.postImageView);
                    imageView.setImageBitmap(capturedBitmap);
                }
            } else if (requestCode == REQUEST_IMAGE_FROM_GALLERY && data != null && data.getData() != null) {
                // Gallery intent result
                Uri imageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    bitmap = rotateImageIfRequired(bitmap, imageUri);
                    ImageView imageView = findViewById(R.id.postImageView);
                    imageView.setImageBitmap(bitmap);
                    capturedBitmap = bitmap;
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

    /**
     * This logic takes the image that is in the ImageView and adds it to Firebase Storage, as well
     * as adds it to the Realtime Database when all fields of the Post Object are successfully filled out
     */
    private void uploadPostToDatabase() {
        if (imageUri != null || capturedBitmap != null) {
            if (imageUri != null) {
                // Image selected from gallery
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
                                        Post post = new Post("postId", "username", System.currentTimeMillis(), imageUrl, "title", "description", 0, 0.0f);

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
                                                        pb.setVisibility(View.INVISIBLE);
                                                        Toast.makeText(CreatePostActivity.this, "Post saved!", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(CreatePostActivity.this, "Failed to post.", Toast.LENGTH_SHORT).show();
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
            } else if (capturedBitmap != null) {
                // Image captured by the camera
                // Convert capturedBitmap to byte array
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                capturedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                // create random key for imageID
                final String randomKey = UUID.randomUUID().toString();
                StorageReference imageRef = storageRef.child("images/" + randomKey);

                // Upload byte array to Firebase Storage
                UploadTask uploadTask = imageRef.putBytes(data);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get the download URL
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                // create new Post object with image
                                Post post = new Post("postId", "username", System.currentTimeMillis(), imageUrl,"title", "description", 0, 0.0f);

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
                                                pb.setVisibility(View.INVISIBLE);
                                                Toast.makeText(CreatePostActivity.this, "Post saved!", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(CreatePostActivity.this, "Failed to post.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
                        Snackbar.make(findViewById(android.R.id.content), "Image uploaded.", Snackbar.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Upload failed.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please select an image first", Toast.LENGTH_SHORT).show();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public interface OnPostAddListener {
        void onPostAdded(Post post);
    }

    // method to open up home page activity
    // finished current activity to remove from backstack
    private void openHomePage() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    // method to open up profile page activity
    // finished current activity to remove from backstack
    private void openProfilePage() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }
}
