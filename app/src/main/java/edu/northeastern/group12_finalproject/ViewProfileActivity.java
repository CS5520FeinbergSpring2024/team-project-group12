package edu.northeastern.group12_finalproject;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

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

public class ViewProfileActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    Users viewUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDatabaseReference;
    DatabaseReference postsDatabaseReference;

    TextView viewProfileName;
    TextView displayNameTv;
    TextView viewProfileBio;
    TextView tvFollow, tvUnfollow;

    PostAdapter adapter;

    private ProgressBar progBar;
    private List<Post> posts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        // Firebase initialization.
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        firebaseDatabase = FirebaseDatabase.getInstance();

        RecyclerView recyclerView = findViewById(R.id.profileRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        posts = new ArrayList<>();

        // Get the user that's being viewed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            viewUser = getIntent().getParcelableExtra("intent user", Users.class);
        }
        else {
            viewUser = getIntent().getParcelableExtra("intent user");
        }

        // Progress bar
        progBar = (ProgressBar) findViewById(R.id.profileProgressBar);
        progBar.setVisibility(View.GONE);

        // Retrieve user information.
        retrieveFirebaseInfo();

        // Retrieve user posts.
        retrievePosts();


        // Set up adapter
        adapter = new PostAdapter(posts);
        recyclerView.setAdapter(adapter);

        // Set up follow action
        tvFollow = findViewById(R.id.tvFollow);
        // Tag follow
        tvFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "No clicked following" + user.getDisplayName());;

                // TODO: CHange the following + Follower logic.
                DatabaseReference following = FirebaseDatabase.getInstance().getReference()
                        .child("following")
                        // Current log in user
                        .child(user.getUid())
                        // The user being viewed
                        .child(viewUser.getUid());
                        // Add email to the node
                        following.child("following_uid")
                        // Add it to the node.
                        .setValue(viewUser.getUid());
                        following.child("email")
                                .setValue(viewUser.getEmail());
//                        following.child("username")
//                                .setValue(viewUser.getUsername());

                // Set up firebase follower info.
                DatabaseReference follower = FirebaseDatabase.getInstance().getReference()
                        .child("follower")
                        .child(viewUser.getUid())
                        .child(user.getUid());
                        // Add email to the node.
                follower.child("follower_uid")
                        // Add it to the node.
                        .setValue(user.getUid());
                follower.child("email")
                                .setValue(user.getEmail());
                setFollowing();
            }
        });

        // Tag unfollow.
        tvUnfollow = findViewById(R.id.tvUnfollow);
        tvUnfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "No clicked following" + user.getDisplayName());;

                FirebaseDatabase.getInstance().getReference()
                        .child("following")
                        // Current log in user
                        .child(user.getUid())
                        // The user being viewed
                        .child(viewUser.getUid())
                        // Remove value
                        .removeValue();

                FirebaseDatabase.getInstance().getReference()
                        .child("follower")
                        .child(viewUser.getUid())
                        .child(user.getUid())
                        .removeValue();
                setUnFollow();
            }
        });

        // set up of bottom nav bar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // ensures profile button is highlighted
        bottomNavigationView.setSelectedItemId(R.id.bottom_nav_profile);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            // if/else for using bottom nav bar
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.bottom_nav_home) {
                    openHomePage();
                } else if (item.getItemId() == R.id.bottom_nav_new_post) {
                    openNewPostPage();
                } else if (item.getItemId() == R.id.bottom_nav_profile) {
                    // do nothing, stay on page
                }
                return false;
            }
        });

        setupToolBar();

    }

    private void isFollowing() {
        Log.d(TAG, "Is Following: checking if it's following");
    }

    private void setFollowing() {
        Log.d(TAG, "Update set following");
        tvFollow.setVisibility(View.GONE);
        tvUnfollow.setVisibility(View.VISIBLE);
    }

    private void setUnFollow() {
        Log.d(TAG, "Update set unfollow");
        tvFollow.setVisibility(View.VISIBLE);
        tvUnfollow.setVisibility(View.GONE);
    }

    private void setupToolBar() {
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.profileToolBar);
        setSupportActionBar(toolbar);
    }
    // method to open up home page activity
    // finished current activity to remove from backstack
    private void openHomePage() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // Close current activity
    }

    // method to open up new post page activity
    // finished current activity to remove from backstack
    private void openNewPostPage() {
        Intent intent = new Intent(this, CreatePostActivity.class);
        startActivity(intent);
        finish(); // Close current activity
    }

    // Retrieve user info from firebase.
    private void retrieveFirebaseInfo() {
        usersDatabaseReference = firebaseDatabase.getReference("Users");

        // Initial View set up.
        viewProfileName = findViewById(R.id.profileName);
        viewProfileBio = findViewById(R.id.bio);
        displayNameTv = findViewById(R.id.display_name);

        viewProfileName.setText(viewUser.getEmail());
        viewProfileBio.setText(viewUser.getBio());
        displayNameTv.setText(viewUser.getUsername());
    }

    // TODO: Not able to retrieve view posts yet.
    private void retrievePosts() {
        // Get reference to the posts database.
        postsDatabaseReference = firebaseDatabase.getReference("posts");

        // Use email as the identifier.
        Query postQuery = usersDatabaseReference.orderByChild("username").equalTo(user.getEmail());
        // Get one child.
        postQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    String postId = dataSnapshot.child("postId").getValue(String.class);
                    String userName = dataSnapshot.child("username").getValue(String.class);
                    String postTitle = dataSnapshot.child("postTitle").getValue(String.class);
                    String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);
                    Float distance = dataSnapshot.child("distance").getValue(Float.class);
                    String description = dataSnapshot.child("description").getValue(String.class);

                    Post newPost = new Post(postId, userName, System.currentTimeMillis(), imageUrl, postTitle, description, 10, distance);
                    posts.add(newPost);
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Data added");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        // Update newly added posts to our posts list recycler view.
        postQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
//                    public Post(String postId, String username, long timestamp, String imageUrl, String postTitle, String description, int active_minutes, float distance) {
                    String postId = dataSnapshot.child("postId").getValue(String.class);
                    String userName = dataSnapshot.child("username").getValue(String.class);
                    String postTitle = dataSnapshot.child("postTitle").getValue(String.class);
                    String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);
                    Float distance = dataSnapshot.child("distance").getValue(Float.class);
                    String description = dataSnapshot.child("description").getValue(String.class);
                    Log.d(TAG, "Data added");
                    Post newPost = new Post(postId, userName, System.currentTimeMillis(), imageUrl, postTitle, description, 10, distance);

                    posts.add(newPost);
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Data added");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}