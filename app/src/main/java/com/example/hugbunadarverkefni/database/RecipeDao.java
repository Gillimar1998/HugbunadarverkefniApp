package com.example.hugbunadarverkefni.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.OnConflictStrategy;

import java.util.Collection;
import java.util.List;

import com.example.hugbunadarverkefni.model.Recipe;

@Dao
public interface RecipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRecipe(RecipeEntity recipe);

    @Query("SELECT * FROM recipes WHERE id = :id LIMIT 1")
    RecipeEntity getRecipeById(Long id);

    @Query("SELECT * FROM recipes")
    List<RecipeEntity> getAllRecipes();

    @Delete
    void deleteRecipe(RecipeEntity recipe);
}

