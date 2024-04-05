package edu.northeastern.group12_finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create dummy data
        List<Post> posts = createDummyPosts();

        // Set up adapter
        PostAdapter adapter = new PostAdapter(posts);
        recyclerView.setAdapter(adapter);

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

    // Method to create dummy posts
    private List<Post> createDummyPosts() {
        List<Post> posts = new ArrayList<>();
        // Create and add dummy posts
        posts.add(new Post("1", "User1", System.currentTimeMillis(), "image_url1", "Title1", "Description1", 10, 1.5f));
        posts.add(new Post("2", "User2", System.currentTimeMillis(), "image_url2", "Title2", "Description2", 20, 2.5f));
        return posts;
    }
}