package com.example.hugbunadarverkefni.database;

import android.graphics.Bitmap;

import com.example.hugbunadarverkefni.model.Comment;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.example.hugbunadarverkefni.model.Comment;
import com.example.hugbunadarverkefni.model.User;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity(tableName = "recipes")
@TypeConverters({Converters.class})
public class RecipeEntity {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    private String name;
    private String description;
    private String category;
    private int cookTime;
    private boolean privatePost;
    private Long userId;
    private int likeCount;
    public String imagePath;

    @TypeConverters({Converters.class})
    private Date creationDate;

    @TypeConverters({Converters.class})
    private List<Long> likedUserIDs;

    @TypeConverters({Converters.class})
    private List<Comment> comments;

    // Constructor
    public RecipeEntity(Long id, String name, String description, String category, int cookTime, boolean privatePost, Long userId, int likeCount, String imagePath, Date creationDate, List<Long> likedUserIDs, List<Comment> comments) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.cookTime = cookTime;
        this.privatePost = privatePost;
        this.userId = userId;
        this.likeCount = likeCount;
        this.imagePath = imagePath;
        this.creationDate = creationDate;
        this.likedUserIDs = likedUserIDs;
        this.comments = comments;
    }

    // Getters and Setters
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

    public boolean isPrivatePost() { return privatePost; }
    public void setPrivatePost(boolean privatePost) { this.privatePost = privatePost; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public Date getCreationDate() { return creationDate; }
    public void setCreationDate(Date creationDate) { this.creationDate = creationDate; }

    public List<Long> getLikedUserIDs() { return likedUserIDs; }
    public void setLikedUserIDs(List<Long> likedUserIDs) { this.likedUserIDs = likedUserIDs; }

    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }
}

