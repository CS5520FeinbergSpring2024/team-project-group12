package edu.northeastern.group12_finalproject;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

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
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    private Context searchContext = SearchActivity.this;

    // Widgets.
    private EditText searchET;
    private ListView nameListView;

    // Name list for the searched users.
    private List<Users> myUserList;
    private UserListAdapter listAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        searchET = (EditText) findViewById(R.id.search);
        nameListView = (ListView) findViewById(R.id.listViewFriends);


        initTextListener();
        hideSoftKeyboard();

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

    private void initTextListener() {
        myUserList = new ArrayList<>();

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = searchET.getText().toString().toLowerCase(Locale.getDefault());
                searchForMatch(text);
            }
        });

    }

    private void updateUserList() {
        listAdapter = new UserListAdapter(SearchActivity.this, R.layout.layout_user_listitem, myUserList);

        nameListView.setAdapter(listAdapter);

        nameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Selected user navigate to viewProfile activity.
                Intent intent = new Intent(SearchActivity.this, ViewProfileActivity.class);
                intent.putExtra("calling activity", "Search Activity");
                intent.putExtra("intent user", (Parcelable) myUserList.get(position));
                startActivity(intent);

            }
        });
    }

    private void searchForMatch(String keyword) {
        Log.d(TAG, "SearchforMatch: Searching for a match" + keyword);
        if (keyword.isEmpty()) {
            myUserList.clear();
            updateUserList();
        }
        else {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    myUserList.clear();
                    for (DataSnapshot ds: snapshot.getChildren()) {
//                    Users user = ds.getValue(Users.class);
                        // Search all non-self user.
                        String uid = ds.child("uid").getValue().toString();
                        String email = ds.child("email").getValue().toString();
                        String username = ds.child("username").getValue().toString();
                        if (!(uid.equals(firebaseUser.getUid()))) {
                            if ((email.toLowerCase().contains(keyword.toLowerCase()))
                                    || username.toLowerCase().contains(keyword.toLowerCase())) {

                                // Create new user.public Users(String bio, String username, String email, int following, int followed, String uid) {
                                Users user = new Users("", username, email, 0, 0, uid);
                                myUserList.add(user);
                                updateUserList();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

//        myUserList.clear();
//        // update the user list accordingly.
//        if(keyword.length() == 0) {
//
//        } else {
//            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
//            Query query = reference.orderByChild(getString(R.string.field_username).equalsTo(keyword))
//        }

    }

    private void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

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