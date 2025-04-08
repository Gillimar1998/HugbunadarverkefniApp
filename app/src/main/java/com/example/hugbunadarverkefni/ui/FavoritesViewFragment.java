package com.example.hugbunadarverkefni.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.hugbunadarverkefni.R;
import com.example.hugbunadarverkefni.adapter.RecipeAdapter;

import static android.content.Context.MODE_PRIVATE;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.hugbunadarverkefni.R;
import com.example.hugbunadarverkefni.adapter.RecipeAdapter;
import com.example.hugbunadarverkefni.api.RecipeApiService;
import com.example.hugbunadarverkefni.api.RetrofitClient;
import com.example.hugbunadarverkefni.api.UserApiService;
import com.example.hugbunadarverkefni.database.RecipeDao;
import com.example.hugbunadarverkefni.database.RecipeDatabase;
import com.example.hugbunadarverkefni.database.RecipeEntity;
import com.example.hugbunadarverkefni.databinding.FragmentFavoritesViewBinding;
import com.example.hugbunadarverkefni.databinding.FragmentRecipesViewBinding;
import com.example.hugbunadarverkefni.model.Comment;
import com.example.hugbunadarverkefni.model.Image;
import com.example.hugbunadarverkefni.model.Recipe;
import com.example.hugbunadarverkefni.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.*;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesViewFragment extends Fragment {

    private @NonNull FragmentFavoritesViewBinding binding;
    private List<Recipe> recipeList = new ArrayList<>();
    private List<RecipeEntity> localRecipeList = new ArrayList<>();
    private RecipeAdapter recipeAdapter;
    private RecipeApiService recipeApiService;
    private Button localRecipes;
    private RecipeDao recipeDao;  // Room DAO for accessing recipes
    private RecipeDatabase recipeDatabase;  // Room Database

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFavoritesViewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize API service properly
        recipeApiService = RetrofitClient.getClient().create(RecipeApiService.class);

        // Initialize Room Database and DAO
        recipeDatabase = RecipeDatabase.getInstance(requireContext());
        recipeDao = recipeDatabase.recipeDao();
        localRecipes = view.findViewById(R.id.btnLocalStorage);

        // Set up RecyclerView
        recipeAdapter = new RecipeAdapter(getContext(), recipeList, this::openRecipeDetails);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(recipeAdapter);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        long userId = sharedPreferences.getLong("user_Id", -1);

        localRecipes.setOnClickListener(v -> {
            localRecipeList.clear();
            Executors.newSingleThreadExecutor().execute(() -> {
                List<RecipeEntity> entities = recipeDao.getAllRecipes();
                List<Recipe> recipes = new ArrayList<>();

                for (RecipeEntity entity : entities) {
                    Recipe recipe = convertEntityToRecipe(entity);
                    if (recipe != null) recipes.add(recipe);
                }

                // Now update UI on the main thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    recipeAdapter.setRecipes(recipes);
                });
            });

        });

        recipeList.clear();
        getUserFavorites(userId);
    }

    private void getUserFavorites(Long userId) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        Set<String> recipeIds = sharedPreferences.getStringSet("favorites", new HashSet<>());
        Log.d("Favorites", "Stored favorites in SharedPreferences: " + recipeIds);

        if (recipeIds != null) {
            for (String id : recipeIds) {
                try {
                    Long recipeId = Long.parseLong(id); // Convert String to Long

                    // Make an asynchronous API call
                    recipeApiService.getRecipeById(recipeId).enqueue(new Callback<Recipe>() {
                        @Override
                        public void onResponse(Call<Recipe> call, Response<Recipe> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                recipeList.add(response.body());
                                recipeAdapter.notifyDataSetChanged(); // Refresh RecyclerView
                            }
                        }

                        @Override
                        public void onFailure(Call<Recipe> call, Throwable t) {
                            Log.e("Favorites", "Failed to fetch recipe: " + recipeId, t);
                        }
                    });

                } catch (NumberFormatException e) {
                    Log.e("Favorites", "Invalid recipe ID: " + id, e);
                }
            }

        }
    }



    private void openRecipeDetails(Recipe recipe) {
        Bundle bundle = new Bundle();
        bundle.putLong("recipeId", recipe.getId());

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_FavoritesViewFragment_to_RecipeViewFragment, bundle);
    }

    private Recipe convertEntityToRecipe(RecipeEntity entity) {
        User user = new User();
        user.setId(entity.getUserId());
        user.setUsername("Guest"); // Optional, use placeholder or store locally

        Image image = new Image();
        image.setUrl(entity.getImagePath());

        Recipe recipe = new Recipe(
                entity.getId(),
                entity.getName(),
                user,
                entity.getDescription(),
                entity.getCategory(),
                entity.getCookTime(),
                entity.getCreationDate(),
                entity.isPrivatePost(),
                entity.getLikedUserIDs(),
                entity.getLikeCount()
        );

        recipe.setComments(entity.getComments());
        recipe.setImage(image);

        return recipe;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

