package com.example.hugbunadarverkefni.model;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

public class Recipe {

    private final User user;

    public Recipe(String name, User user, String category, String cookTimeMin) {
        this.name = name;
        this.user = user;
        this.category = category;
        this.cookTime = cookTimeMin;
        this.creationDate = new Date();
    }
    private Long id;
    private String name;
    private String category;
    private String description;
    private Macros macros;
    private String cookTime;


    private Date creationDate;
    private boolean privatePost;
    private List<Long> likedUserIDs;

    // Getters and Setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public Macros getMacros() { return macros; }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public int getCooktimeInMinutes() {
        try {
            return (int) Duration.parse(cookTime).toMinutes(); // Convert to integer
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return 0; // Default value if parsing fails
        }
    }
    public Date getCreationDate() { return creationDate; }
    public boolean isPrivatePost() { return privatePost; }
    public List<Long> getLikedUserIDs() { return likedUserIDs; }
}
