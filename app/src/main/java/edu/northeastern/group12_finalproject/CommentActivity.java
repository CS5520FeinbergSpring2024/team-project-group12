package edu.northeastern.group12_finalproject;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.icu.text.DateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CommentActivity extends AppCompatActivity {

    DatabaseReference databaseReference;
    TextView titleTextView;
    TextView descriptionTextView;
    TextView usernameTextView;
    TextView activeMinutesTextView;
    TextView distanceTextView;
    ImageView postImage;
    TextView timestamp;
    EditText newCommentEditText;
    private Post post;
    private List<Comment> comments = new ArrayList<>();
    private CommentAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        titleTextView = findViewById(R.id.ptitletv);
        descriptionTextView = findViewById(R.id.descript);
        usernameTextView = findViewById(R.id.username_text_view);
        activeMinutesTextView = findViewById(R.id.active_minutes_text_view);
        distanceTextView = findViewById(R.id.distance_text_view);
        postImage = findViewById(R.id.image_view);
        timestamp = findViewById(R.id.time_text_view);
        newCommentEditText = findViewById(R.id.commentInputEditText);


        // Retrieve data from Intent
        // Retrieve data from Intent
        Intent intent = getIntent();
        String postId = intent.getStringExtra("postId");
        Log.d("CommentActivity", "Received postId: " + postId);
        String postTitle = intent.getStringExtra("postTitle");
        String postDescription = intent.getStringExtra("description");
        String imageUrl = intent.getStringExtra("imageUrl");
        String username = intent.getStringExtra("username");
        int activeMinutes = intent.getIntExtra("activeMinutes", 0);
        float distance = intent.getFloatExtra("distance", 0.0f);
        long postTimestamp = intent.getLongExtra("timestamp", 0);
        comments = intent.getParcelableArrayListExtra("comments");
        if (comments == null) {
            comments = new ArrayList<>(); // Initialize an empty list if comments are null
        }
        // Set up recycler view
        RecyclerView commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        commentAdapter = new CommentAdapter(comments);
        commentsRecyclerView.setAdapter(commentAdapter);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set the data to the corresponding UI elements
        titleTextView.setText(postTitle);
        descriptionTextView.setText(postDescription);
        usernameTextView.setText(username);
        activeMinutesTextView.setText("Active Minutes: " + String.valueOf(activeMinutes));
        distanceTextView.setText("Distance: " + String.valueOf(distance));
        timestamp.setText(getTimestampDifference(postTimestamp)); // Implement getTimestampDifference method

        // Load image using Picasso or any other image loading library
        Picasso.get()
                .load(imageUrl)
                .into(postImage);


        // Handle Adding New Comments
        Button addCommentButton = findViewById(R.id.addCommentButton);
        addCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newCommentText = newCommentEditText.getText().toString().trim();
                if (!newCommentText.isEmpty()) {
                    // Create a new Comment object with the current user's username and timestamp
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    FirebaseUser user = auth.getCurrentUser();
                    String username = user.getEmail(); // Use email as username for now
                    long timestamp = System.currentTimeMillis(); // Current timestamp
                    String commentId = FirebaseDatabase.getInstance().getReference("posts")
                            .child(postId)
                            .child("comments")
                            .push()
                            .getKey();
                    Comment newComment = new Comment(commentId, username, timestamp, newCommentText);
                    // Update the Firebase database with the new comment
                    DatabaseReference commentsRef = FirebaseDatabase.getInstance()
                            .getReference("posts")
                            .child(postId)
                            .child("comments")
                            .child(commentId); // Reference to the comment using its id
                    commentsRef.setValue(newComment)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "New comment added to database");
                                    // Add the new comment locally to the list of comments
                                    comments.add(newComment);
                                    // Notify the adapter that the dataset has changed
                                    commentAdapter.notifyDataSetChanged();
                                    // Clear the EditText after adding the comment
                                    newCommentEditText.getText().clear();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "Failed to add comment to database", e);
                                    // Handle failure to add comment to database
                                }
                            });
                    retrievePostFromDatabase(postId);
                }
            }
        });


    }

    // Method to calculate the timestamp difference
    private String getTimestampDifference(long timestamp) {
        long now = System.currentTimeMillis();
        long difference = Math.abs(now - timestamp) / 1000; // Difference in seconds

        if (difference < 60) {
            return difference + " seconds ago";
        } else if (difference < 60 * 60) {
            long minutes = difference / 60;
            return minutes + " minutes ago";
        } else if (difference < 60 * 60 * 24) {
            long hours = difference / (60 * 60);
            return hours + " hours ago";
        } else if (difference < 60 * 60 * 24 * 365) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            return calendar.get(Calendar.DAY_OF_MONTH) + " " +
                    new DateFormatSymbols().getMonths()[calendar.get(Calendar.MONTH)] + " at " +
                    DateFormat.getTimeInstance().format(calendar.getTime());
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            return calendar.get(Calendar.DAY_OF_MONTH) + " " +
                    new DateFormatSymbols().getMonths()[calendar.get(Calendar.MONTH)] + ", " +
                    calendar.get(Calendar.YEAR) + " at " +
                    DateFormat.getTimeInstance().format(calendar.getTime());
        }
    }

    private void retrievePostFromDatabase(String postId) {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts").child(postId);
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("RetrievePost", "Post with the given postId exists in the database");
                    // Retrieve the post object
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post != null) {
                        // Retrieve the comments for the post
                        HashMap<String, Comment> commentsMap = post.getComments();
                        if (commentsMap != null && !commentsMap.isEmpty()) {
                            List<Comment> comments = new ArrayList<>(commentsMap.values());
                            // Update the adapter with the new list of comments
                            commentAdapter.updateComments(comments);
                        } else {
                            Log.d("RetrievePost", "No comments found for the post");
                        }
                    }
                } else {
                    Log.d("RetrievePost", "Post with the given postId does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish(); // Finish the CommentActivity
        // Start MainActivity again
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}