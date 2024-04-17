package edu.northeastern.group12_finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Collections;


public class MainActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private PostAdapter adapter;
    ImageButton searchBtn;
    List<String> feedIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // set the adapter:
        adapter = new PostAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference().child("posts");

        // Query the database for the post with the current user's username
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String loggedInUserId = user.getUid();

        // Add search function and button listener.
        searchBtn = findViewById(R.id.search_button);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Query the database for the posts from userIds that the current user is following
        String currentUserId = user.getUid();
        DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference().child("following").child(currentUserId);
        // set listener to retrieve data and store userIds from following node into list
        feedIds = new ArrayList<>();

        followingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        String userId = childSnapshot.getKey(); // get userId from key
                        feedIds.add(userId);
                    }
                    feedIds.add(currentUserId);
                    Log.d("MainActivity", "Following User IDs: " + feedIds);
                    if (!feedIds.isEmpty()) {
                        fetchPostsForFeed(feedIds);
                    } else {
                        Toast.makeText(MainActivity.this, "You are not following anyone yet!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    feedIds.add(currentUserId);
                    fetchPostsForFeed(feedIds);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MainActivity", "Database error: " + error.getMessage());
            }
        });


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_nav_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.bottom_nav_home) {
                    openHomePage(); // working to implement a refresh
                } else if (item.getItemId() == R.id.bottom_nav_new_post) {
                    openNewPostPage();
                } else if (item.getItemId() == R.id.bottom_nav_profile) {
                    openProfilePage();
                }
                return false;
            }
        });
    }

    // method to Fetch all posts
    private void fetchPostsForFeed(List<String> userIds) {
        List<Post> allPosts = new ArrayList<>(); // list to hold all Posts from followed users
        // Initialize Firebase database reference
        AtomicInteger remainingCalls = new AtomicInteger(userIds.size()); // counter for async calls

        for (String userId : userIds) {
            Log.d("MainActivity", "Querying for posts with username: " + userId);
            Query query =  FirebaseDatabase.getInstance().getReference().child("posts")
                    .orderByChild("userID")
                    .equalTo(userId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Post post = postSnapshot.getValue(Post.class);
                            Log.d("MainActivity", "Post fetched: " + post.getPostTitle());
                            allPosts.add(post);
                        }
                    }
                    // check if all queries are done
                    if (remainingCalls.decrementAndGet() == 0) {
                        // Sort posts by timestamp in descending order (to show newest Posts at top of feed)
                        Collections.sort(allPosts, (post1, post2) -> Long.compare(post2.getTimestamp(), post1.getTimestamp()));
                        // ensure that updates to RecyclerView are happening on the main thread
                        runOnUiThread(() -> {
                            Log.d("MainActivity", "Updating RecyclerView with posts");
                            adapter.updateData(allPosts); // Assuming you have a method to update data in your adapter
                            adapter.notifyDataSetChanged(); // Notify the adapter of the dataset change
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("MainActivity", "Database error on post fetch: " + error.getMessage());
                }
            });
        }
    }

    // method to open up home page activity
    // finished current activity to remove from backstack
    private void openHomePage() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // method to open up new post page activity
    // finished current activity to remove from backstack
    private void openNewPostPage() {
        Intent intent = new Intent(this, CreatePostActivity.class);
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
