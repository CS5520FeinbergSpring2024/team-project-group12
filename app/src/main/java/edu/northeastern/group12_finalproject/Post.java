package edu.northeastern.group12_finalproject;

import java.util.ArrayList;
import java.util.List;

public class Post {

    private String postId; // unique identifier for Post in database
    private String username;
    private long timestamp;
    private String imageUrl;
    private String postTitle;
    private String description;
    private int active_minutes;
    private float distance;
    private int likes;
    private List<String> comments;


    // constructor
    public Post() {
        //this.likes = 0;
        //this.comments = new ArrayList<>(); // initialize comments as empty array
    }
    // Constructor with imageUrl parameter
    public Post(String postId, String username, long timestamp, String imageUrl, String postTitle, String description, int active_minutes, float distance) {
        this.postId = postId;
        this.username = username;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
        this.postTitle = postTitle;
        this.description = description;
        this.active_minutes = active_minutes;
        this.distance = distance;
        this.likes = 0;
        // note: when initialized as an empty array, the comments field doesnt show up in Realtime DB.
        // But adding a comment upon initialization includes comments field in the DB correctly
        this.comments = new ArrayList<>(); // Initialize comments as an empty list if comments is null
        // this.comments.add("This is a sample comment.");


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
        return active_minutes;
    }

    public void setActiveMinutes() {
        this.active_minutes = active_minutes;
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

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    // Utility methods to add likes and comments

    public void addLikes() {
        this.likes++;
    }

    public void addComment(String comment) {
        this.comments.add(comment);
    }

}
