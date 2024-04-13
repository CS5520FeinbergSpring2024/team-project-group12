package edu.northeastern.group12_finalproject;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
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

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

public class FollowingActivity extends AppCompatActivity {
    // Widgets.
    private TextView followingTV;
    private ListView nameListView;
    private ProgressBar progressBar;

    // Name list for the searched users.
    private List<Users> myUserList;
    private UserListAdapter listAdapter;
    Handler mainHandler = new Handler();

    FirebaseUser user;

    List<String> followingIDs;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);

        followingTV = findViewById(R.id.followerTV);
        nameListView = (ListView) findViewById(R.id.lvfollowing);
        progressBar = findViewById(R.id.followingProgressBar);
        user = FirebaseAuth.getInstance().getCurrentUser();

        myUserList = new ArrayList<>();

        followingIDs = new ArrayList<>();
        showFollowing();

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
        listAdapter = new UserListAdapter(FollowingActivity.this, R.layout.layout_user_listitem, myUserList);

        nameListView.setAdapter(listAdapter);

        nameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Selected user navigate to viewProfile activity.
                Intent intent = new Intent(FollowingActivity.this, ViewProfileActivity.class);
                intent.putExtra("calling activity", "Search Activity");
                intent.putExtra("intent user", (Parcelable) myUserList.get(position));
                startActivity(intent);
            }
        });
    }

    private void showFollowing() {
        Log.d(TAG, "Show following");
        updateUserList();
//        List<String> result;
//        result = new ArrayList<>();


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child("following")
                .child(user.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot sp : snapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found following:" + sp.getValue());
                    String uid = sp.child("user_id").getValue().toString();
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            followingIDs.add(uid);
                            Log.d(TAG, "FOLLOWING! " + followingIDs);
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.field_following));
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                myUserList.clear();
//                for (DataSnapshot ds: snapshot.getChildren()) {
//                    Log.d(TAG, "The children" + ds.getValue());
//                    Users user = ds.getValue(Users.class);
//                    myUserList.add(user);
//                    updateUserList();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }

    private void firebaseNewData(String newUserId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        Query userQuery = reference.orderByChild("uid").equalTo(newUserId);
        userQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            Users user = ds.getValue(Users.class);
                            Log.d(TAG, "User " + user.getEmail() + user.getUid());

                            if (newUserId.equals(user.getUid())) {
                                Log.d(TAG, "FOLLOWING in side firebase! " + followingIDs);
                                Log.d(TAG, "User " + user.getEmail() + user.getUid());
                                if (!myUserList.contains(user)) {
                                    mainHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            myUserList.add(user);
                                            updateUserList();
                                        }
                                    });

                                }

                            }
                        }
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void runMainThread(View view) {
        AddToListThread addThread = new AddToListThread();
        new Thread(addThread).start();
    }

    class AddToListThread implements Runnable {

        @Override
        public void run() {
            showFollowing();
            for (String follow: followingIDs) {
                Log.d(TAG, "New follow in database" + follow);
                firebaseNewData(follow);
            }
        }
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