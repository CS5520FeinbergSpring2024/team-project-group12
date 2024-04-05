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
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.bottom_nav_home) {
                    openHomePage();
                    return true;
                } else if (item.getItemId() == R.id.bottom_nav_new_post) {
                    openNewPostPage();
                    return true;
                } else if (item.getItemId() == R.id.bottom_nav_profile) {
                    openProfilePage();
                    return true;
                }
                return false;
            }
        });
    }



    private void openHomePage() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // Method to open New Post Activity
    private void openNewPostPage() {
        Intent intent = new Intent(this, CreatePostActivity.class);
        startActivity(intent);
    }

    // Method to open Profile Activity
    private void openProfilePage() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
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