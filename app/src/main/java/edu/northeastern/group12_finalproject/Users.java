package edu.northeastern.group12_finalproject;

public class Users {
    private String bio;
    private String username;
    private String email;
    private int following;
    private int followed;
    private String uid;


    // constructor
    public Users() {
    }
    // Constructor with imageUrl parameter
    public Users(String bio, String username, String email, int following, int followed, String uid) {
        this.bio = bio;
        this.username = username;
        this.email = email;
        this.following = following;
        this.followed = followed;
        this.uid = uid;
    }

    // getters and setters
    public String getBio() {
        return this.bio;
    }

    public String getEmail() {
        return this.email;
    }

    public String getUsername() {
        return username;
    }
    public int getFollowing() {
        return this.following;
    }

    public int getFollowed() {
        return this.followed;
    }

    public String getUid() {
        return uid;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFollowed(int followed) {
        this.followed = followed;
    }

    public void setFollowing(int following) {
        this.following = following;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
