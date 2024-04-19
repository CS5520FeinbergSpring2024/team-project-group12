package edu.northeastern.group12_finalproject;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
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

public class FollowerActivity extends AppCompatActivity {
    // Widgets.
    private TextView followerTV;
    private ListView nameListView;
    private ImageView exitImage;
    private ProgressBar progressBar;

    // Name list for the searched users.
    private List<Users> myUserList;
    private UserListAdapter listAdapter;
//    Handler mainHandler = new Handler();

    FirebaseUser user;

    List<String> followingIDs;
    private Users currentUser;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follower);

        followerTV = findViewById(R.id.followerTV);
        nameListView = (ListView) findViewById(R.id.lvfollower);
        progressBar = findViewById(R.id.followerProgressBar);
        exitImage = findViewById(R.id.exit);

        user = FirebaseAuth.getInstance().getCurrentUser();
        // Get the user that's being viewed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            currentUser = getIntent().getParcelableExtra("current user", Users.class);
        }
        else {
            currentUser = getIntent().getParcelableExtra("current user");
        }

        myUserList = new ArrayList<>();

        followingIDs = new ArrayList<>();
        showFollowing();
        progressBar.setVisibility(View.GONE);

        exitImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FollowerActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        Log.d(TAG, "FOLLOWING! outside thread " + followingIDs);



        // set up of bottom nav bar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // ensures profile button is highlighted
        bottomNavigationView.setSelectedItemId(R.id.bottom_nav_profile);

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

    private void updateUserList() {
        listAdapter = new UserListAdapter(FollowerActivity.this, R.layout.layout_user_listitem, myUserList);

        nameListView.setAdapter(listAdapter);

        nameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Selected user navigate to viewProfile activity.
                Intent intent = new Intent(FollowerActivity.this, ViewProfileActivity.class);
                intent.putExtra("calling activity", "Follower Activity");
                intent.putExtra("intent user", (Parcelable) myUserList.get(position));
                intent.putExtra("current user", (Parcelable) currentUser);
                startActivity(intent);
            }
        });
    }

    private void showFollowing() {
        Log.d(TAG, "Show following");
        updateUserList();


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child("follower")
                .child(user.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot sp : snapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found following:" + sp.getValue());
                    String uid = sp.child("uid").getValue().toString();
                    retrieveUsers(uid);
//                    String profileU = returnProfileUrl();
                    // Add to the myUserList.
//                    Users currUser = sp.getValue(Users.class);
//                    currUser.setProfileImageUrl(profileU);

//                    currUser.setProfileImageUrl();


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void retrieveUsers(String uid) {

        Query q = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("uid").equalTo(uid);

        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    Users currUser = ds.getValue(Users.class);
                    myUserList.add(currUser);
                    updateUserList();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
    private void openProfilePage() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }
}