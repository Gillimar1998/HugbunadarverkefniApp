package com.example.hugbunadarverkefni.api;

import com.example.hugbunadarverkefni.model.Recipe;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RecipeApiService {
    @GET("/recipes")
    Call<List<Recipe>> getRecipes();

    @GET("/recipes/{id}")
    Call<Recipe> getRecipeById(@Path("id") Long id);

    @POST("/recipes")
    Call<Recipe> addRecipe(@Body Recipe recipe);

    @POST("/recipes/{id}/like")
    Call<Map<String, Object>> likeRecipe(@Path("id") long recipeId, @Query("userId") long userId);

    @DELETE("recipes/{id}")
    Call<Void> deleteRecipe(@Path("id") long recipeId);

    @GET("/recipes/search")
    Call<List<Recipe>> searchRecipes(@Query("name") String name);


}
