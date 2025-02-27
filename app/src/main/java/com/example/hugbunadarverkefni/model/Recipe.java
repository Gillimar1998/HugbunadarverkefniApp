package com.example.hugbunadarverkefni.model;

import java.util.Date;
import java.util.List;

public class Recipe {
    private Long id;
    private String name;
    private String category;
    private List<String> ingredients;
    private Macros macros;
    private String cookTime;  // Backend uses `Duration`, so use `int` (in seconds)
    private Date creationDate;
    private boolean privatePost;
    private List<Long> likedUserIDs;

    // Getters and Setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public List<String> getIngredients() { return ingredients; }
    public Macros getMacros() { return macros; }
    public String getCookTime() { return cookTime; }
    public Date getCreationDate() { return creationDate; }
    public boolean isPrivatePost() { return privatePost; }
    public List<Long> getLikedUserIDs() { return likedUserIDs; }
}
