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



    // Constructor for creating new recipes (without ID & creationDate)
    public Recipe(String name, User user, String description, String category, int cookTime, boolean privatePost) {
        this.name = name;
        this.user = user;
        this.description = description;
        this.category = category;
        this.cookTime = cookTime;
        this.privatePost = privatePost;
        this.likedUserIDs = List.of(); // Initialize empty
        this.likeCount = 0;
    }

    // Constructor for receiving recipes from the backend (full version)
    public Recipe(Long id, String name, User user, String description, String category, int cookTime, String creationDate, boolean privatePost, List<Long> likedUserIDs, int likeCount) {
        this.id = id;
        this.name = name;
        this.user = user;
        this.description = description;
        this.category = category;
        this.cookTime = cookTime;
        this.creationDate = creationDate;
        this.privatePost = privatePost;
        this.likedUserIDs = likedUserIDs;
        this.likeCount = likeCount;
    }


    // Getters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
  
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
  
    public int getCookTime() { return cookTime; }
    public void setCookTime(int cookTime) { this.cookTime = cookTime; }
  
    public String getCreationDate() { return creationDate; }
    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }
  
     public boolean isPrivatePost() { return privatePost; }
    public void setPrivatePost(boolean privatePost) { this.privatePost = privatePost; }

    public List<Long> getLikedUserIDs() { return likedUserIDs; }
    public void setLikedUserIDs(List<Long> likedUserIDs) { this.likedUserIDs = likedUserIDs; }
    
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
  
    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public long getUserId(){ return userId; }
    public void setUserId(long userId){this.userId = userId; }
    
    public Image getImage() { return image; }
    public void setImage(Image image) { this.image = image; }

   

}

