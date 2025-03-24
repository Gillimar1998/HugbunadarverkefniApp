package com.example.hugbunadarverkefni.model;

import java.util.List;
import java.util.Set;

public class User {
    private Long id;
    private String username;
    private String email;
    private String password;
    private boolean admin;
    private List<Recipe> recipes;
    private List<Rating> ratings;
    private Set<String> favorites;

    // Default Constructor
    public User() {}

    // Constructor
    public User(String username, String email, String password, boolean admin, Set<String> favorites) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.admin = admin;
        this.favorites = favorites;
    }



    // Getters and Setters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isAdmin() { return admin; }
    public void setAdmin(boolean admin) { this.admin = admin; }

    public List<Recipe> getRecipes() { return recipes; }
    public void setRecipes(List<Recipe> recipes) { this.recipes = recipes; }

    public List<Rating> getRatings() { return ratings; }
    public void setRatings(List<Rating> ratings) { this.ratings = ratings; }

    public Set<String> getFavorites(){ return favorites; }
    public void setFavorites(Set<String> favorites){ this.favorites = favorites; }
}
