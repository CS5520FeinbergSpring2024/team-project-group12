package edu.northeastern.group12_finalproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CreatePostActivity extends AppCompatActivity {

    private EditText duration;
    private EditText distance;
    private EditText location;
    private EditText editTextTitle;
    private EditText editTextDescription;

    private Button buttonAddImage;
    private Button post;
    private OnPostAddListener listener;
    private static final int REQUEST_CODE_IMAGE_UPLOAD = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

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
        buttonAddImage = findViewById(R.id.upload_photo_button);
        post = findViewById(R.id.post_button);
        buttonAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageForPost();
            }
        });

        // get the ImageView from the layout
        ImageView imageView = findViewById(R.id.postImageView);

        // Retrieve the bitmap from the intent
        // This works for CAPTURED images, not those uploaded from gallery. If uploaded from gallery, app crashes.
        if (getIntent().hasExtra("uploaded_image")) {
            Bitmap bitmap = getIntent().getParcelableExtra("uploaded_image");
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                Toast.makeText(this, "Failed to retrieve image", Toast.LENGTH_SHORT).show();
            }
        } else if (getIntent().hasExtra("uploaded_image_uri")) {
            // retrieve the image from the intent
            String imageUriString = getIntent().getStringExtra("uploaded_image_uri");
            Uri imageUri = Uri.parse(imageUriString);
            imageView.setImageURI(imageUri);
        } else {
            Toast.makeText(this, "No image found", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectImageForPost() {
        Intent intent = new Intent(CreatePostActivity.this, ImageUploadActivity.class);
        startActivityForResult(intent, REQUEST_CODE_IMAGE_UPLOAD);
    }

    // This method will be called when the ImageUploadActivity finishes
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMAGE_UPLOAD && resultCode == RESULT_OK && data != null) {
            // get the imageURI from data intent
            Uri imageUri = data.getData();

            // now load the image into an ImageView
            ImageView imageView = findViewById(R.id.postImageView);
            imageView.setImageURI(imageUri);
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
