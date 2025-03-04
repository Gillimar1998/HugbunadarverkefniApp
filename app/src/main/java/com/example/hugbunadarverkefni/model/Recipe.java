package com.example.hugbunadarverkefni.model;

import java.util.List;

public class Recipe {
    private Long id;
    private String name;
    private User user; // The user who posted the recipe
    private String description;
    private String category;
    private int cookTime;// Stored as an integer
    private long userId;
    private String creationDate; // Optional: Can be null or set by backend
    private boolean privatePost;
    private List<Long> likedUserIDs; // List of user IDs who liked this recipe
    private int likeCount; // Number of likes

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

    public Recipe(String title, long userId, String description, String category, int cookTime, Boolean privatePost) {
        this.name = title;
        this.userId = userId;
        this.description = description;
        this.category = category;
        this.cookTime = cookTime;
        this.privatePost = privatePost;
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

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public long getUserId(){ return userId; }
    public void setUserId(long userId){this.userId = userId; }

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

}
