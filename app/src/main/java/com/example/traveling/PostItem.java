package com.example.traveling;

import java.util.List;

/**
 This class merges a Firestore document with the image URI stored in SQLite. thus making a singular post

 TODO : add comments maybe
 */
public class PostItem {

    private String firestoreId;
    private String authorId;
    private String description;
    private String address;
    private String group;
    private boolean isPublic;
    private boolean isAnonymous;
    private long likes;
    private List<String> tags;
    private String imageUri;
    private long timestampMillis;

    public PostItem() {}

    public String getFirestoreId()              { return firestoreId; }
    public void setFirestoreId(String id)       { this.firestoreId = id; }

    public String getAuthorId()                 { return authorId; }
    public void setAuthorId(String authorId)    { this.authorId = authorId; }

    public String getDescription()              { return description; }
    public void setDescription(String d)        { this.description = d; }

    public String getAddress()                  { return address; }
    public void setAddress(String a)            { this.address = a; }

    public String getGroup()                    { return group; }
    public void setGroup(String g)              { this.group = g; }

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
}