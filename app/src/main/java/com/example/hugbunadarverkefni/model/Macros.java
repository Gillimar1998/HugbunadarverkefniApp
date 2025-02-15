package com.example.hugbunadarverkefni.model;

public class Macros {
    private long id;
    private int calories;
    private float fat;
    private float carbohydrates;
    private float protein;

    // Default Constructor
    public Macros() {}

    // Constructor
    public Macros(int calories, float fat, float carbohydrates, float protein) {
        this.calories = calories;
        this.fat = fat;
        this.carbohydrates = carbohydrates;
        this.protein = protein;
    }

    // Getters and Setters
    public long getId() { return id; }
    public int getCalories() { return calories; }
    public float getFat() { return fat; }
    public float getCarbohydrates() { return carbohydrates; }
    public float getProtein() { return protein; }

    public void setCalories(int calories) { this.calories = calories; }
    public void setFat(float fat) { this.fat = fat; }
    public void setCarbohydrates(float carbohydrates) { this.carbohydrates = carbohydrates; }
    public void setProtein(float protein) { this.protein = protein; }
}
