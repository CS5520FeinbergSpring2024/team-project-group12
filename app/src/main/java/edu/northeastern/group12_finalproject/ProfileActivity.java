package edu.northeastern.group12_finalproject;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    TextView profileName;
    TextView bio;
    TextView editTv;

    private ProgressBar progBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Progress bar
        progBar = (ProgressBar) findViewById(R.id.profileProgressBar);
        progBar.setVisibility(View.GONE);

        retrieveFirebaseInfo();

        // Take you to edit profile page.
        editTv = findViewById(R.id.tvEditProfile);
        editTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
                finish();
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

        toolbar.setOnMenuItemClickListener(new androidx.appcompat.widget.Toolbar.OnMenuItemClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.d(TAG, "onMenuItemClick: clicked menu item: " + item);
                int id = item.getItemId();
                if (id == R.id.profileMenu) {
                    Log.d(TAG, "onMenuItemClick: Navigating to Profile Preferences. ");
                }
                return false;
            }
        });
    }
    // Override oncreateoptionmenu for top tool bar.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
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
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");

        // Initial View set up.
        profileName = findViewById(R.id.profileName);
        bio = findViewById(R.id.bio);

        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check until required data gets updated
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    // Retrieve data
                    String name = "" + dataSnapshot.child("username").getValue();
                    String bioData = "" + dataSnapshot.child("bio").getValue();

                    // Set data to textView.
                    profileName.setText(name);
                    bio.setText(bioData);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}