package edu.northeastern.group12_finalproject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class CreatePostActivity extends AppCompatActivity {

    private EditText editTextDescription;
    private Button buttonAddPost;

    private OnPostAddListener listener;
    private static final int REQUEST_CODE_IMAGE_UPLOAD = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        editTextDescription = findViewById(R.id.editTextDescription);
        buttonAddPost = findViewById(R.id.buttonAddPost);
        buttonAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = editTextDescription.getText().toString();

                // Show snackbar with user-entered description
                showToast("Your description is: " + description);
                finish(); // Close the activity
            }
        });

        Button buttonAddImage = findViewById(R.id.addImageButton);
        buttonAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageForPost();
            }
        });
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
}
