package com.example.hugbunadarverkefni.api;

import com.example.hugbunadarverkefni.model.LoginRequest;
import com.example.hugbunadarverkefni.model.Recipe;
import com.example.hugbunadarverkefni.model.User;

import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserApiService {
    @POST("/users/login")
    Call<User> login(@Body LoginRequest loginRequest);

    @POST("/users")
    Call<User> registerUser(@Body User user);

    @PUT("users/{id}")
    Call<User> updateUser(
            @Path("id") Long id,
            @Body User updatedUser
    );

    @GET("users/{id}")
    Call<User> getUserById(@Path("id") long userId);

    @POST("users/{userId}/favorites/{recipeId}")
    Call<User> addRecipeToFavorites(
            @Path("userId") Long userId,
            @Path("recipeId") Long recipeId
    );

    @DELETE("users/{userId}/favorites/{recipeId}")
    Call<User> removeRecipeFromFavorites(
            @Path("userId") Long userId,
            @Path("recipeId") Long recipeId
    );

    @GET("users/{userId}/favorites")
    Call<Set<String>> getUserFavorites(@Path("userId") Long userId);
}