package com.example.traveling;

import java.util.List;

/**
 Represents a single post, mapping Firestore document fields + Firebase Storage image URL.
 */
public class PostItem {

    private String firestoreId;
    private String authorId;
    private String title;
    private String description;
    private String address;
    private String groupId;
    private String groupName;
    private boolean isPublic;
    private boolean isAnonymous;
    private long likes;
    private List<String> tags;
    private String imageUri;
    private long timestampMillis;
    private boolean likedByMe = false;
    private double latitude;
    private double longitude;

    public PostItem() {}

    public String getFirestoreId()              { return firestoreId; }
    public void setFirestoreId(String id)       { this.firestoreId = id; }

    public String getAuthorId()                 { return authorId; }
    public void setAuthorId(String authorId)    { this.authorId = authorId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription()              { return description; }
    public void setDescription(String d)        { this.description = d; }

    public String getAddress()                  { return address; }
    public void setAddress(String a)            { this.address = a; }

    public String getGroupId()                    { return groupId; }
    public void setGroupId(String g)              { this.groupId = g; }
    public String getGroupName() {
        return groupName;
    }
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    public boolean isPublic()                   { return isPublic; }
    public void setPublic(boolean pub)          { this.isPublic = pub; }

    /* not sure i need this */
    public boolean isAnonymous()                { return isAnonymous; }
    public void setAnonymous(boolean anon)      { this.isAnonymous = anon; }

    public long getLikes()                      { return likes; }
    public void setLikes(long likes)            { this.likes = likes; }

    public List<String> getTags()               { return tags; }
    public void setTags(List<String> tags)      { this.tags = tags; }

    public String getImageUri()                 { return imageUri; }
    public void setImageUri(String uri)         { this.imageUri = uri; }

    public long getTimestampMillis()            { return timestampMillis; }
    public void setTimestampMillis(long ms)     { this.timestampMillis = ms; }
    public boolean isLikedByMe()              { return likedByMe; }
    public void setLikedByMe(boolean liked)   { this.likedByMe = liked; }
    public double getLatitude()  { return latitude; }
    public double getLongitude() { return longitude; }
    public void setLatitude(double latitude)   { this.latitude  = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}