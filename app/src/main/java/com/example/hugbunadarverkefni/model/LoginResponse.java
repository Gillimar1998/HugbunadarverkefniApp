package com.example.hugbunadarverkefni.model;

public class LoginResponse {
    private String message;
    private User user;

    public LoginResponse(String message, User user) {
        this.message = message;
        this.user = user;
    }

    // Getters
    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }
}
