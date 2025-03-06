package com.example.hugbunadarverkefni.api;

import com.example.hugbunadarverkefni.model.Recipe;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RecipeApiService {
    @GET("/recipes")
    Call<List<Recipe>> getRecipes();

    @GET("/recipes/{id}")
    Call<Recipe> getRecipeById(@Path("id") Long id);

    @Multipart
    @POST("/recipes")
    Call<Recipe> addRecipe(
            @Part("name") RequestBody name,
            @Part("description") RequestBody description,
            @Part("category") RequestBody category,
            @Part("cookTime") RequestBody cookTime,
            @Part("userId") RequestBody userId,
            @Part MultipartBody.Part file
    );


    @POST("/recipes/{id}/like")
    Call<Map<String, Object>> likeRecipe(@Path("id") long recipeId, @Query("userId") long userId);

    @DELETE("recipes/{id}")
    Call<Void> deleteRecipe(@Path("id") long recipeId);

    @GET("/recipes/search")
    Call<List<Recipe>> searchRecipes(@Query("name") String name);


}
