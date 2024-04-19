package edu.northeastern.group12_finalproject;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Users implements Parcelable {
    private String bio;
    private String username;
    private String email;
    private int following;
    private int followed;

    private double activeMinutes;
    private double distance;
    private String uid;
    private String profileImageUrl;


    // constructor
    public Users() {
    }
    // Constructor with imageUrl parameter
    public Users(String bio, String username, String email, int following, int followed, String uid, double activeMinutes, double distance, String profileImageUrl) {
        this.bio = bio;
        this.username = username;
        this.email = email;
        this.following = following;
        this.followed = followed;
        this.uid = uid;
        this.activeMinutes = activeMinutes;
        this.distance = distance;
        this.profileImageUrl = profileImageUrl;
    }

    protected Users(Parcel in) {
        bio = in.readString();
        username = in.readString();
        email = in.readString();
        following = in.readInt();
        followed = in.readInt();
        uid = in.readString();
        activeMinutes = in.readDouble();
        distance = in.readDouble();
        profileImageUrl = in.readString();
    }

    public static final Creator<Users> CREATOR = new Creator<Users>() {
        @Override
        public Users createFromParcel(Parcel in) {
            return new Users(in);
        }

        @Override
        public Users[] newArray(int size) {
            return new Users[size];
        }
    };

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

    public double getActiveMinutes() {
        return activeMinutes;
    }

    public double getDistance() {
        return distance;
    }

    public void setActiveMinutes(double activeMinutes) {
        this.activeMinutes = activeMinutes;
    }

    public void setDistance(double distance) {
        this.distance = distance;
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

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(bio);
        dest.writeString(username);
        dest.writeString(email);
        dest.writeInt(following);
        dest.writeInt(followed);
        dest.writeString(uid);
        dest.writeDouble(activeMinutes);
        dest.writeDouble(distance);
        dest.writeString(profileImageUrl);
    }
}
