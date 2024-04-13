package edu.northeastern.group12_finalproject;

import android.icu.text.DateFormat;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Post {

    private String postId; // unique identifier for Post in database
    private String username;
    private String userID;
    private long timestamp;
    private String imageUrl;
    private String postTitle;
    private String description;
    private int activeMinutes;
    private float distance;
    private int likes;
    private HashMap<String, Comment> comments;
    private boolean liked;


    // constructor
    public Post() {
        //this.likes = 0;
        //this.comments = new ArrayList<>(); // initialize comments as empty array
    }
    // Constructor with imageUrl parameter
    public Post(String postId, String username, String userID, long timestamp, String imageUrl, String postTitle, String description, int activeMinutes, float distance) {
        this.postId = postId;
        this.username = username;
        this.userID = userID;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
        this.postTitle = postTitle;
        this.description = description;
        this.activeMinutes = activeMinutes;
        this.distance = distance;
        this.likes = 0;
        // note: when initialized as an empty array, the comments field doesnt show up in Realtime DB.
        // But adding a comment upon initialization includes comments field in the DB correctly
        this.comments = new HashMap<>(); // Initialize comments as an empty list if comments is null
    }

    // getters and setters
    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // Setter method for imageUrl
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getActiveMinutes() {
        return activeMinutes;
    }

    public void setActiveMinutes() {
        this.activeMinutes = activeMinutes;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public int getLikes() {
        return likes;
    }

    public void incrementLikes() {
        likes++;
    }

    public void decrementLikes() {
        likes--;
    }

    public void addComment(Comment comment) {
        if (comments == null) {
            comments = new HashMap<>(); // Initialize comments HashMap if null
        }
        // Add the comment to the HashMap using its ID as the key
        comments.put(comment.getId(), comment);
    }

    public HashMap<String, Comment> getComments() {
        return comments;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public String getTimestampDifference() {
        long now = System.currentTimeMillis();
        long difference = Math.abs(now - timestamp) / 1000; // Difference in seconds

        if (difference < 60) {
            return difference + " seconds ago";
        } else if (difference < 60 * 60) {
            long minutes = difference / 60;
            return minutes + " minutes ago";
        } else if (difference < 60 * 60 * 24) {
            long hours = difference / (60 * 60);
            return hours + " hours ago";
        } else if (difference < 60 * 60 * 24 * 365) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            return calendar.get(Calendar.DAY_OF_MONTH) + " " +
                    new DateFormatSymbols().getMonths()[calendar.get(Calendar.MONTH)] + " at " +
                    DateFormat.getTimeInstance().format(calendar.getTime());
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            return calendar.get(Calendar.DAY_OF_MONTH) + " " +
                    new DateFormatSymbols().getMonths()[calendar.get(Calendar.MONTH)] + ", " +
                    calendar.get(Calendar.YEAR) + " at " +
                    DateFormat.getTimeInstance().format(calendar.getTime());
        }
    }
}
