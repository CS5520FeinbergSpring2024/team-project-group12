package edu.northeastern.group12_finalproject;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        Log.d("PostAdapter", "Post at position " + position + " loaded: " + post.getPostTitle());

        holder.titleTextView.setText(post.getPostTitle());
        holder.descriptionTextView.setText(post.getDescription());
        holder.usernameTextView.setText(post.getUsername());
        int activeMinutes = post.getActiveMinutes();
        holder.activeMinutesTextView.setText("Active Minutes: " + String.valueOf(activeMinutes));
        holder.distanceTextView.setText("Total Distance: " + String.valueOf(post.getDistance()));
        holder.likesTextView.setText(String.valueOf(post.getLikes()) + " likes");
        holder.timestamp.setText(post.getTimestampDifference());

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

        // Set up like button onClickListener
        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Implement like functionality here
                DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("posts").child(post.getPostId());

                postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Post firebasePost = snapshot.getValue(Post.class);
                            if (firebasePost != null) {
                                if (firebasePost.isLiked()) {
                                    // Unlike the post
                                    firebasePost.setLiked(false);
                                    firebasePost.decrementLikes();
                                } else {
                                    // Like the post
                                    firebasePost.setLiked(true);
                                    firebasePost.incrementLikes();
                                }
                                // Update like count and like status in Firebase
                                postRef.child("likes").setValue(firebasePost.getLikes());
                                postRef.child("liked").setValue(firebasePost.isLiked());
                                // Update local UI
                                holder.likesTextView.setText(String.valueOf(firebasePost.getLikes()) + " likes");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("PostAdapter", "Error fetching post data from Firebase: " + error.getMessage());
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

            likeButton = itemView.findViewById(R.id.like);
            commentButton = itemView.findViewById(R.id.comment);

        }
    }
}
