package edu.northeastern.group12_finalproject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class CreatePostActivity extends AppCompatActivity {

    private EditText editTextDescription;
    private Button buttonAddPost;

    private OnPostAddListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_create_post);

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
        Intent intent1 = new Intent(CreatePostActivity.this, ImageUploadActivity.class);
        startActivity(intent1);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public interface OnPostAddListener {
        void onPostAdded(Post post);
    }
}
