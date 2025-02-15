package com.example.hugbunadarverkefni.model;

public class Rating {
    private Long id;
    private int value;
    private Recipe recipe;
    private User user;

    // Default Constructor
    public Rating() {}

    // Constructor
    public Rating(int value, Recipe recipe, User user) {
        this.value = value;
        this.recipe = recipe;
        this.user = user;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }

    public Recipe getRecipe() { return recipe; }
    public void setRecipe(Recipe recipe) { this.recipe = recipe; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
