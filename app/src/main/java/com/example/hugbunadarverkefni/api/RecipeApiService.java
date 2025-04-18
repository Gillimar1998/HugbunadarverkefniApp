package com.example.hugbunadarverkefni.api;

import com.example.hugbunadarverkefni.model.Comment;
import com.example.hugbunadarverkefni.model.Recipe;
import com.example.hugbunadarverkefni.model.User;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
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
            @Part("privatePost") RequestBody privatePost,
            @Part MultipartBody.Part file
    );

    @POST("/recipes/{id}/like")
    Call<Map<String, Object>> likeRecipe(@Path("id") long recipeId, @Query("userId") long userId);

    @DELETE("recipes/{id}")
    Call<Void> deleteRecipe(@Path("id") long recipeId);

    @GET("/recipes/search")
    Call<List<Recipe>> searchRecipes(@Query("name") String name);

    @PUT("/recipes/{id}")
    Call<Recipe> editRecipe(@Path("id") long id, @Body Recipe recipe);

    @PATCH("/recipes/{id}")
    Call<Recipe> patchRecipe(@Path("id") long id, @Body RequestBody body);

    @Multipart
    @PATCH("/recipes/{id}")
    Call<Recipe> patchRecipeMultipart(
            @Path("id") long id,
            @Part("name") RequestBody name,
            @Part("description") RequestBody description,
            @Part("category") RequestBody category,
            @Part("cookTime") RequestBody cookTime,
            @Part MultipartBody.Part image
    );

    @POST("/recipes/{id}/comments")
    Call<Comment> postComment(@Path("id") long recipeId, @Body Map<String, Object> body);


    @Multipart
    @POST("/comments/add")
    Call<Comment> postCommentMultipart(
            @Part("recipeId") RequestBody recipeId,
            @Part("userId") RequestBody userId,
            @Part("content") RequestBody content,
            @Part MultipartBody.Part image  // can be null
    );

    @Multipart
    @PATCH("/comments/{id}")
    Call<Comment> patchComment(
            @Path("id") Long commentId,
            @Part("userId") RequestBody userId,
            @Part("content") RequestBody content,
            @Part MultipartBody.Part image  // can be null
    );

    @DELETE("/comments/{id}")
    Call<Void> deleteComment(
            @Path("id") long commentId,
            @Query("userId") long userId
    );


}
