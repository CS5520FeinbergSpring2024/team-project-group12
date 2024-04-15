package edu.northeastern.group12_finalproject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList;

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("PostAdapter", "Post at position " + position + " loaded: " + post.getPostTitle());

        holder.titleTextView.setText(post.getPostTitle());
        holder.descriptionTextView.setText(post.getDescription());
        holder.usernameTextView.setText(post.getUsername());
        int activeMinutes = post.getActiveMinutes();
        holder.activeMinutesTextView.setText("Active Minutes: " + String.valueOf(activeMinutes));
        holder.distanceTextView.setText("Total Distance: " + String.valueOf(post.getDistance()));
        holder.timestamp.setText(post.getTimestampDifference());

        // Set the initial like count
        if (post.getLikes() != null) {
            int initialLikes = 0;
            for (Boolean value : post.getLikes().values()) {
                if (Boolean.TRUE.equals(value)) {
                    initialLikes++;
                }
            }
            holder.likesTextView.setText(initialLikes + " likes");
        } else {
            holder.likesTextView.setText("0 likes");
        }

        // Check if comments list is not null before accessing its size
        if (post.getComments() != null) {
            Log.d("PostAdapter", "Comments loaded for post: " + post.getPostTitle() + ", size: " + post.getComments().size());
            holder.commentsTextView.setText(String.valueOf(post.getComments().size()) + " Comments");
        } else {
            holder.commentsTextView.setText("0 Comments"); // Set default value if comments list is null
            Log.d("PostAdapter", "No comments found for post: " + post.getPostTitle());
        }

        // Load image using Picasso
        Picasso.get()
                .load(post.getImageUrl())
                .into(holder.postImage);

        // Set visibility of moreButton based on user ID comparison
        if (post.getUserID().equals(currentUserId)) {
            holder.moreButton.setVisibility(View.VISIBLE);
        } else {
            holder.moreButton.setVisibility(View.GONE);
        }

        // Set up like button onClickListener
        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Implement like functionality here
                DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("posts").child(post.getPostId());
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Assuming you have Firebase Auth setup
                DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("posts").child(post.getPostId()).child("likes").child(currentUserId);

                // Check if the current user has liked the post
                likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean isCurrentlyLiked = snapshot.exists() && snapshot.getValue(Boolean.class);

                        if (isCurrentlyLiked) {
                            // User has liked this post, now remove like
                            likesRef.removeValue(); // Remove this user's like
                        } else {
                            // User has not liked this post, now add like
                            likesRef.setValue(true); // Set this user's like to true
                        }

                        // After updating Firebase, update UI locally (might be better to use Firebase event listeners to listen to changes)
                        updateLikesDisplay(post.getPostId(), holder.likesTextView);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("PostAdapter", "Failed to read like status", error.toException());
                    }
                });
            }
        });

        // Set up comment button onClickListener
        holder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), CommentActivity.class);
                // Pass necessary data to the intent (postId, postTitle, imageUrl, comments)
                intent.putExtra("postId", post.getPostId());
                intent.putExtra("postTitle", post.getPostTitle());
                intent.putExtra("description", post.getDescription());
                intent.putExtra("imageUrl", post.getImageUrl());
                intent.putExtra("username", post.getUsername());
                intent.putExtra("activeMinutes", post.getActiveMinutes());
                intent.putExtra("distance", post.getDistance());
                intent.putExtra("timestamp", post.getTimestamp());
                // Get the comments HashMap
                HashMap<String, Comment> commentsMap = post.getComments();
                if (commentsMap != null && !commentsMap.isEmpty()) {
                    intent.putParcelableArrayListExtra("comments", new ArrayList<>(commentsMap.values()));
                } else {
                    intent.putParcelableArrayListExtra("comments", new ArrayList<>());
                }
                // Start the activity
                view.getContext().startActivity(intent);
            }
        });

        holder.moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show confirmation dialog
                new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Delete Post")
                        .setMessage("Are you sure you want to delete this post?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deletePost(post.getPostId(), position, holder.itemView.getContext());
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
        // Load profile photo to posts.
        retrieveProfilePhoto(post.getUserID(), holder.profilePhoto);
    }

    private void deletePost(String postId, int position, Context context) {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("posts").child(postId);
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Post post = snapshot.getValue(Post.class);
                    if (post != null) {
                        int activeMinutes = post.getActiveMinutes();
                        float distance = post.getDistance();

                        // Now delete the post
                        postRef.removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Decrement user data
                                updateUserData(post.getUserID(), -activeMinutes, -distance);
                                // Remove the post from the list and update the adapter
                                postList.remove(position);
                                notifyItemRemoved(position);
                                Toast.makeText(context.getApplicationContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context.getApplicationContext(), "Failed to delete post", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Toast.makeText(context.getApplicationContext(), "Post not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to fetch post data", error.toException());
            }
        });
    }
    private void updateUserData(String userId, int minutesChange, float distanceChange) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        userRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Users user = mutableData.getValue(Users.class);
                if (user != null) {
                    user.setActiveMinutes(user.getActiveMinutes() + minutesChange);
                    user.setDistance(user.getDistance() + distanceChange);
                    mutableData.setValue(user);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (!committed) {
                    Log.e("Firebase", "User data update failed", databaseError.toException());
                } else {
                    Log.d("Firebase", "User data updated successfully");
                }
            }
        });
    }

    // This function updates the number of likes displayed in the UI
    private void updateLikesDisplay(String postId, TextView likesTextView) {
        DatabaseReference postLikesRef = FirebaseDatabase.getInstance().getReference().child("posts").child(postId).child("likes");
        postLikesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int likeCount = 0;
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Boolean isLiked = childSnapshot.getValue(Boolean.class);
                    if (Boolean.TRUE.equals(isLiked)) {
                        likeCount++;
                    }
                }
                likesTextView.setText(likeCount + " likes");
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PostAdapter", "Failed to count likes", error.toException());
            }
        });
    }


    @Override
    public int getItemCount() {
        return postList.size();
    }

    // Method to update the data in your adapter
    public void updateData(List<Post> newPosts) {
        postList.clear(); // Clear the existing data
        postList.addAll(newPosts); // Add the new data
        notifyDataSetChanged(); // Notify the adapter that the data set has changed
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;
        TextView usernameTextView;
        TextView activeMinutesTextView;
        TextView distanceTextView;
        TextView likesTextView;
        TextView commentsTextView;
        ImageView postImage;
        TextView timestamp;
        Button likeButton;
        Button commentButton;
        ImageButton moreButton;

        // Adding pictureTV to show profile photo
        CircleImageView profilePhoto;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.ptitletv);
            descriptionTextView = itemView.findViewById(R.id.descript);
            usernameTextView = itemView.findViewById(R.id.username_text_view);
            activeMinutesTextView = itemView.findViewById(R.id.active_minutes_text_view);
            distanceTextView = itemView.findViewById(R.id.distance_text_view);
            likesTextView = itemView.findViewById(R.id.like_count);
            commentsTextView = itemView.findViewById(R.id.comment_count);
            postImage = itemView.findViewById(R.id.image_view);
            timestamp = itemView.findViewById(R.id.time_text_view);
            moreButton = itemView.findViewById(R.id.morebtn);

            likeButton = itemView.findViewById(R.id.like);
            commentButton = itemView.findViewById(R.id.comment);

            // Adding pictureTV to show profile photo
            profilePhoto = itemView.findViewById(R.id.picturetv);
        }
    }

    private void retrieveProfilePhoto(String uid, CircleImageView photo) {

        Query q = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("uid").equalTo(uid);

        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    Users currUser = ds.getValue(Users.class);
                    String url = currUser.getProfileImageUrl();
                    if ((url != null)) {
                        if (!(url.equals("0"))) {
                            Picasso.get()
                                    .load(url)
                                    .into(photo);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
