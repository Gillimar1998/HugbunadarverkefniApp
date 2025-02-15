package com.example.hugbunadarverkefni.api;

import com.example.hugbunadarverkefni.model.Recipe;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface RecipeApiService {
    @GET("/recipes")
    Call<List<Recipe>> getRecipes();
}
