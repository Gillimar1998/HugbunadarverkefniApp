package com.example.hugbunadarverkefni.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.hugbunadarverkefni.model.Recipe;

@Database(entities = {RecipeEntity.class}, version = 1)
public abstract class RecipeDatabase extends RoomDatabase {
    private static volatile RecipeDatabase instance;

    public abstract RecipeDao recipeDao();

    public static synchronized RecipeDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            RecipeDatabase.class, "recipe_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}

