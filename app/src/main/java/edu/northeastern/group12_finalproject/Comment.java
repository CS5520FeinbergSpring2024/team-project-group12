package edu.northeastern.group12_finalproject;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Comment implements Parcelable {

    private String username;
    private long timestamp;
    private String text;

    // Constructor
    public Comment(String username, long timestamp, String text) {
        this.username = username;
        this.timestamp = timestamp;
        this.text = text;
    }

    // Parcelable implementation
    protected Comment(Parcel in) {
        username = in.readString();
        timestamp = in.readLong();
        text = in.readString();
    }

    public static final Parcelable.Creator<Comment> CREATOR = new Parcelable.Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(username);
        parcel.writeLong(timestamp);
        parcel.writeString(text);
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getText() {
        return text;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTimestampDifference(long commentTimestamp) {
        long now = System.currentTimeMillis();
        long difference = Math.abs(now - commentTimestamp) / 1000; // Difference in seconds

        if (difference < 60) {
            return difference + " seconds ago";
        } else if (difference < 60 * 60) {
            long minutes = difference / 60;
            return minutes + " minutes ago";
        } else if (difference < 60 * 60 * 24) {
            long hours = difference / (60 * 60);
            return hours + " hours ago";
        } else if (difference < 60 * 60 * 24 * 30) {
            long days = difference / (60 * 60 * 24);
            return days + " days ago";
        } else {
            // If the difference is more than 30 days, return the formatted date
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(commentTimestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
            return sdf.format(calendar.getTime());
        }
    }
}
