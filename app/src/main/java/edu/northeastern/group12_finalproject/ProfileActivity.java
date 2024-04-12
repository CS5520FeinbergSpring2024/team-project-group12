package edu.northeastern.group12_finalproject;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDatabaseReference;
    DatabaseReference postsDatabaseReference;
    int followed_count;
    int following_count;

    TextView profileName;
    TextView displayNameTv;
    TextView bio;
    TextView editTv;

    TextView followedCount, followingCount;

    PostAdapter adapter;

    private ProgressBar progBar;
    private List<Post> posts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Firebase initialization.
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        firebaseDatabase = FirebaseDatabase.getInstance();

        RecyclerView recyclerView = findViewById(R.id.profileRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        posts = new ArrayList<>();



        // Progress bar
        progBar = (ProgressBar) findViewById(R.id.profileProgressBar);
        progBar.setVisibility(View.GONE);

        // Retrieve user information.
        retrieveFirebaseInfo();

        // Retrieve user posts.
        retrievePosts();

        getFollowerCount();
        getFollowingCount();

        // Set up adapter
        adapter = new PostAdapter(posts);
        recyclerView.setAdapter(adapter);

        // Take you to edit profile page.
        editTv = findViewById(R.id.tvEditProfile);
        editTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
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

    // set up top tool bar
    private void setupToolBar() {
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.profileToolBar);
        setSupportActionBar(toolbar);

//        toolbar.setOnMenuItemClickListener(new androidx.appcompat.widget.Toolbar.OnMenuItemClickListener() {
//            @SuppressLint("NonConstantResourceId")
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                Log.d(TAG, "onMenuItemClick: clicked menu item: " + item);
//                int id = item.getItemId();
//                if (id == R.id.profileMenu) {
//                    Log.d(TAG, "onMenuItemClick: Navigating to Profile Preferences. ");
//                }
//                return false;
//            }
//        });

        ImageView profileMenu = (ImageView) findViewById(R.id.profileMenu);
        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick:navigating to account settings.");
                Intent intent = new Intent(ProfileActivity.this, SignOutActivity.class);
                startActivity(intent);
            }
        });
    }
//    // Override oncreateoptionmenu for top tool bar.
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.profile_menu, menu);
//        return true;
//    }

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
        profileName = findViewById(R.id.profileName);
        bio = findViewById(R.id.bio);
        displayNameTv = findViewById(R.id.display_name);
        followedCount = findViewById(R.id.tvFollowerNum);
        followingCount = findViewById(R.id.tvFollowingNum);

        Query userQuery = usersDatabaseReference.orderByChild("email").equalTo(user.getEmail());

        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check until required data gets updated
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {

                    Users currUser = dataSnapshot.getValue(Users.class);
                    profileName.setText(currUser.getEmail());
                    bio.setText(currUser.getBio());
                    displayNameTv.setText(currUser.getUsername());
//                    followedCount.setText(String.valueOf(currUser.getFollowed()));
//                    followingCount.setText(String.valueOf(currUser.getFollowing()));
//                    // Retrieve data
//                    String name = "" + dataSnapshot.child("username").getValue();
//                    String bioData = "" + dataSnapshot.child("bio").getValue();
//                    String email = "" + dataSnapshot.child("email").getValue();
//
//                    // Set data to textView.
//                    profileName.setText(email);
//                    displayNameTv.setText(name);
//                    bio.setText(bioData);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Retrieve user posts.
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


    private void getFollowerCount() {
        followed_count = 0;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child("followed")
                .child(user.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot sp : snapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found follower:" + sp.getValue());
                    followed_count++;
                }
                followedCount.setText(String.valueOf(followed_count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowingCount() {
        following_count = 0;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child("following")
                .child(user.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot sp : snapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found following:" + sp.getValue());
                    following_count++;
                }
                followingCount.setText(String.valueOf(following_count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}