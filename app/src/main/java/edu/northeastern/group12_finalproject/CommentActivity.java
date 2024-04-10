package edu.northeastern.group12_finalproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.icu.text.DateFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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


        // Retrieve data from Intent
        // Retrieve data from Intent
        Intent intent = getIntent();
        String postId = intent.getStringExtra("postId");
        String postTitle = intent.getStringExtra("postTitle");
        String postDescription = intent.getStringExtra("description");
        String imageUrl = intent.getStringExtra("imageUrl");
        String username = intent.getStringExtra("username");
        int activeMinutes = intent.getIntExtra("activeMinutes", 0);
        int distance = intent.getIntExtra("distance", 0);
        long postTimestamp = intent.getLongExtra("timestamp", 0);
        ArrayList<Comment> comments = intent.getParcelableArrayListExtra("comments");
        if (comments == null) {
            comments = new ArrayList<>(); // Initialize an empty list if comments are null
        }
        // Set up recycler view
        RecyclerView commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        CommentAdapter commentAdapter = new CommentAdapter(comments);
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
                // Add comment functionality
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
}