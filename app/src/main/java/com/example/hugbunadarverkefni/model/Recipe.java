package com.example.hugbunadarverkefni.model;

import android.os.Build;
import androidx.annotation.RequiresApi;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Recipe implements Serializable { // Implements Serializable for passing data
    private Long id;
    private String name;
    private String category;
    private String description;
    private Image image; // Added image field
    private int cookTime; // Changed from String to int to avoid parsing issues
    private Date creationDate;
    private boolean privatePost;
    private List<Long> likedUserIDs;
    private int likeCount; // Added like count field
    private List<Comment> comments; // Added comments list

    private final User user; // Kept as final

    public Recipe(String name, User user, String category, int cookTime) {
        this.name = name;
        this.user = user;
        this.category = category;
        this.cookTime = cookTime;
        this.creationDate = new Date();
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public Image getImage() { return image; } // Getter for image
    public int getCookTime() { return cookTime; }
    public Date getCreationDate() { return creationDate; }
    public boolean isPrivatePost() { return privatePost; }
    public List<Long> getLikedUserIDs() { return likedUserIDs; }
    public int getLikeCount() { return likeCount; }
    public List<Comment> getComments() { return comments; }

}

