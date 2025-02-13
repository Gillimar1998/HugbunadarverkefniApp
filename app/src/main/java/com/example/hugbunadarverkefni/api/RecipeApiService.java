package com.example.hugbunadarverkefni.api;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public class RecipeApiService {
    @GET("recipes")
    Call<List<Recipe>> getRecipes();
}
