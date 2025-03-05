package com.example.hugbunadarverkefni.model;

import java.io.Serializable;

public class Comment implements Serializable { // Implements Serializable
    private Long id;
    private String content;
    private User user;
    private Image image; // ✅ Added image field

    // Default Constructor
    public Comment() {}

    // Constructor
    public Comment(Long id, String content, User user, Image image) {
        this.id = id;
        this.content = content;
        this.user = user;
        this.image = image;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Image getImage() { return image; } // ✅ Getter for image
    public void setImage(Image image) { this.image = image; }
}
