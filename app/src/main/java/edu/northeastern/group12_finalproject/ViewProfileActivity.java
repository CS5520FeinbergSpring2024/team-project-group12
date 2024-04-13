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
    Users currUser;
    Users viewUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDatabaseReference;
    DatabaseReference postsDatabaseReference;

    private int following_count = 0;
    private int followed_count = 0;
    TextView followingCount;
    TextView followedCount;
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
//        currUser = firebaseAuth.getCurrentUser();

        firebaseDatabase = FirebaseDatabase.getInstance();

        RecyclerView recyclerView = findViewById(R.id.profileRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        posts = new ArrayList<>();

        // Get the user that's being viewed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            viewUser = getIntent().getParcelableExtra("intent user", Users.class);
            currUser = getIntent().getParcelableExtra("current user", Users.class);
        }
        else {
            viewUser = getIntent().getParcelableExtra("intent user");
            currUser = getIntent().getParcelableExtra("current user");
        }

        // Progress bar
        progBar = (ProgressBar) findViewById(R.id.profileProgressBar);
        progBar.setVisibility(View.GONE);

        // Retrieve user information.
        retrieveFirebaseInfo();

        // Retrieve user posts.
//        retrievePosts();


        followingCount = findViewById(R.id.tvFollowingNum);
        followedCount = findViewById(R.id.tvFollowerNum);

        // Set up adapter
        adapter = new PostAdapter(posts);
        recyclerView.setAdapter(adapter);

        // Set up follow action
        tvFollow = findViewById(R.id.tvFollow);
        // Tag unfollow.
        tvUnfollow = findViewById(R.id.tvUnfollow);

        isFollowing();
        getFollowerCount();
        getFollowingCount();

        // Tag follow
        tvFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "No clicked following" + currUser.getUsername());;

                // TODO: CHange the following + Follower logic.
                DatabaseReference following = FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.field_following))
                        // Current log in user
                        .child(currUser.getUid())
                        // The user being viewed
                        .child(viewUser.getUid());
                        // Add email to the node
//                        following.child(getString(R.string.field_user_id))
                        // Add it to the node.
                        following.setValue(viewUser);
//                        following.child(getString(R.string.field_email))
//                                .setValue(viewUser.getEmail());
//                        following.child("username")
//                                .setValue(viewUser.getUsername());

//                // Update following count. This approach is too slow to fetch.
//                DatabaseReference following_reference = firebaseDatabase.getReference("Users")
//                        .child(user.getUid())
//                        .child(getString(R.string.field_following));
//
//                int[] result = getUserFollowCount(user.getUid());
//                following_count = result[0];
//                following_count += 1;
//                following_reference.setValue(following_count);

                // Set up firebase follower info.
                DatabaseReference follower = FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.field_follower))
                        .child(viewUser.getUid())
                        .child(currUser.getUid());
                follower.setValue(currUser);
                        // Add email to the node.
//                follower.child(getString(R.string.field_user_id))
//                        // Add it to the node.
//                        .setValue(currUser.getUid());
//                follower.child(getString(R.string.field_email))
//                                .setValue(currUser.getEmail());

                // Update follower count. This method is too slow and fetch is not up to speed.
//                DatabaseReference follower_reference = firebaseDatabase.getReference("Users")
//                        .child(viewUser.getUid())
//                        .child(getString(R.string.field_follower));
//
//                int[] result2 = getUserFollowCount(viewUser.getUid());
//                followed_count = result[1];
//                followed_count += 1;
//                follower_reference.setValue(followed_count);

                // And then increment followed.
                setFollowing();
            }
        });


        tvUnfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "No clicked following" + currUser.getUsername());;

                FirebaseDatabase.getInstance().getReference()
                        .child("following")
                        // Current log in user
                        .child(currUser.getUid())
                        // The user being viewed
                        .child(viewUser.getUid())
                        // Remove value
                        .removeValue();

                FirebaseDatabase.getInstance().getReference()
                        .child("follower")
                        .child(viewUser.getUid())
                        .child(currUser.getUid())
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

    private void getFollowerCount() {
        followed_count = 0;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child("follower")
                .child(viewUser.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
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
                .child(viewUser.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
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

    private void isFollowing() {
        Log.d(TAG, "Is Following: checking if it's following");
        setUnFollow();
        // Current Database reference.
        Query query = FirebaseDatabase.getInstance().getReference().child(getString(R.string.field_following))
                .child(currUser.getUid())
                .orderByChild("uid").equalTo(viewUser.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    setFollowing();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
        Query query = usersDatabaseReference.orderByChild("uid").equalTo(viewUser.getUid());
        // Initial View set up.
        viewProfileName = findViewById(R.id.profileName);
        viewProfileBio = findViewById(R.id.bio);
        displayNameTv = findViewById(R.id.display_name);
        followedCount = findViewById(R.id.tvFollowerNum);
        followingCount = findViewById(R.id.tvFollowingNum);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Users viewingUser = ds.getValue(Users.class);
                    viewProfileName.setText(viewingUser.getEmail());
                    viewProfileBio.setText(viewingUser.getBio());
                    displayNameTv.setText(viewingUser.getUsername());
//                    followedCount.setText(String.valueOf(viewingUser.getFollowed()));
//                    followingCount.setText(String.valueOf(viewingUser.getFollowing()));
                }
//                if (dataSnapshot.exists()) {
//                    String email = dataSnapshot.child("email").getValue(String.class);
//                    String bio = dataSnapshot.child("bio").getValue(String.class);
//                    String username = dataSnapshot.child("username").getValue(String.class);
//                    // Set the retrieved information to the corresponding TextViews
//                    if (email != null) {
//                        viewProfileName.setText(email);
//                    }
//                    if (bio != null) {
//                        viewProfileBio.setText(bio);
//                    }
//                    if (username != null) {
//                        displayNameTv.setText(username);
//                    }
//                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
            }
        });
    }

    // TODO: Not able to retrieve view posts yet.
    private void retrievePosts() {
        // Get reference to the posts database.
        postsDatabaseReference = firebaseDatabase.getReference("posts");

        // Use email as the identifier.
        Query postQuery = usersDatabaseReference.orderByChild("username").equalTo(currUser.getEmail());
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

    // Write the logic to return following and followed count. Index 0: following. Index1: followed.
    private int[] getUserFollowCount(String userID) {
        int[] result = new int[2];
        result[0] = 0;
        result[1] = 0;
        // Get a query based on uid.
        Query query = firebaseDatabase.getReference("Users").orderByChild("uid").equalTo(userID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    String folCount = ds.child("following").getValue().toString();
                    String foedCount = ds.child("followed").getValue().toString();
                    result[0] = Integer.valueOf(folCount);
                    result[1] = Integer.valueOf(foedCount);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return result;
    }

}