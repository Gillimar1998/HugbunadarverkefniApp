package com.example.hugbunadarverkefni.api;

import com.example.hugbunadarverkefni.model.Recipe;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RecipeApiService {
    @GET("/recipes")
    Call<List<Recipe>> getRecipes();

    @GET("/recipes/{id}")
    Call<Recipe> getRecipeById(@Path("id") Long id);

    @POST("/recipes")
    Call<Recipe> addRecipe(@Body Recipe recipe);


}
