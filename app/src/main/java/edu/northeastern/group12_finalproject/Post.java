package edu.northeastern.group12_finalproject;

public class Post {

    private String postId; // unique identifier for Post in database
    private String username;
    private long timestamp;
    private String imageUrl;
    private String postTitle;
    private String description;
    private int active_minutes;
    private float distance;

    // constructor
    public Post() {

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

}
