package com.example.hugbunadarverkefni.model;

import java.util.Date;
import java.util.List;

public class Recipe {
    private Long id;
    private String name;
    private String category;
    private String description;
    private Macros macros;
    private Integer cookTime;
    private Date creationDate;
    private boolean privatePost;
    private List<Long> likedUserIDs;

    // Getters and Setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public Macros getMacros() { return macros; }
    public Integer getCookTime() { return cookTime; }
    public Date getCreationDate() { return creationDate; }
    public boolean isPrivatePost() { return privatePost; }
    public List<Long> getLikedUserIDs() { return likedUserIDs; }
}
