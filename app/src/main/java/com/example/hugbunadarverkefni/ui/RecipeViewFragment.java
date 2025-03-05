package com.example.hugbunadarverkefni.ui;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.hugbunadarverkefni.R;
import com.example.hugbunadarverkefni.api.RecipeApiService;
import com.example.hugbunadarverkefni.api.RetrofitClient;
import com.example.hugbunadarverkefni.model.Recipe;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeViewFragment extends Fragment {

    private TextView titleTextView, categoryTextView, cookTimeTextView, descriptionTextView, likesTextView;
    private LinearLayout commentsContainer;
    private ImageButton likeButton;
    private long recipeId;
    private int likeCount = 0;
    private Button btnDeleteRecipe;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_view, container, false);

        // Initialize UI elements
        titleTextView = view.findViewById(R.id.recipeTitle);
        categoryTextView = view.findViewById(R.id.recipeCategory);
        cookTimeTextView = view.findViewById(R.id.recipeCookTime);
        descriptionTextView = view.findViewById(R.id.recipeDescription);
        likesTextView = view.findViewById(R.id.recipeLikes);
        commentsContainer = view.findViewById(R.id.commentsContainer);
        likeButton = view.findViewById(R.id.btnLike);
        btnDeleteRecipe = view.findViewById(R.id.btnDeleteRecipe);


        // Get recipe ID from arguments
        if (getArguments() != null) {
            recipeId = getArguments().getLong("recipeId", -1);
            if (recipeId != -1) {
                fetchRecipeDetails(recipeId);
            } else {
                Toast.makeText(getContext(), "Invalid Recipe ID", Toast.LENGTH_SHORT).show();
            }
        }


        // Like button click event
        likeButton.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
            long userId = sharedPreferences.getLong("user_Id", -1);

            if (userId == -1) {
                Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            RecipeApiService apiService = RetrofitClient.getClient().create(RecipeApiService.class);
            Call<Map<String, Object>> call = apiService.likeRecipe(recipeId, userId);

            call.enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Map<String, Object> responseBody = response.body();

                        // Get the updated like count
                        int likeCount = ((Double) responseBody.get("likeCount")).intValue();

                        // Get the list of users who liked this post
                        List<Double> likedUserIds = (List<Double>) responseBody.get("likedUserIds");
                        boolean isLiked = likedUserIds.contains((double) userId); // Check if the user is in the list

                        // Update UI
                        likesTextView.setText("Likes: " + likeCount);
                        if (isLiked) {
                            Toast.makeText(getContext(), "Liked!", Toast.LENGTH_SHORT).show();
                            likeButton.setImageResource(R.drawable.ic_favorite_filled); // Change to "liked" image
                        } else {
                            Toast.makeText(getContext(), "Unliked!", Toast.LENGTH_SHORT).show();
                            likeButton.setImageResource(R.drawable.ic_favorite_border); // Change to "unliked" image
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to update like", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });


        return view;
    }

    private void fetchRecipeDetails(long recipeId) {
        RecipeApiService apiService = RetrofitClient.getClient().create(RecipeApiService.class);
        Call<Recipe> call = apiService.getRecipeById(recipeId);

        call.enqueue(new Callback<Recipe>() {
            @Override
            public void onResponse(Call<Recipe> call, Response<Recipe> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Recipe recipe = response.body();
                    displayRecipeDetails(recipe);
                } else {
                    Log.e("API Error", "Failed to fetch recipe details: " + response.errorBody());
                    Toast.makeText(getContext(), "Error fetching recipe", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Recipe> call, Throwable t) {
                Log.e("API Error", "Network Failure: " + t.getMessage());
                Toast.makeText(getContext(), "Network error, please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayRecipeDetails(Recipe recipe) {
        titleTextView.setText(recipe.getName());
        categoryTextView.setText("Category: " + recipe.getCategory());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            cookTimeTextView.setText("Duration: " + recipe.getCookTime() + " min");
        }
        descriptionTextView.setText("Description: " + recipe.getDescription());

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        long userId = sharedPreferences.getLong("user_Id", -1);

        likesTextView.setText("Likes: " + recipe.getLikeCount());

        if (recipe.getUser().getId() == userId) {
            btnDeleteRecipe.setVisibility(View.VISIBLE);
            btnDeleteRecipe.setOnClickListener(v -> showDeleteConfirmation(recipeId));
        }

        if (recipe.getLikedUserIDs().contains(userId)) {
            likeButton.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            likeButton.setImageResource(R.drawable.ic_favorite_border);
        }
        // Load comments dynamically
        commentsContainer.removeAllViews();
//        if (recipe.getComments() != null && !recipe.getComments().isEmpty()) {
//            for (Recipe.Comment comment : recipe.getComments()) {
//                TextView commentView = new TextView(getContext());
//                commentView.setText(comment.getUser().getUsername() + ": " + comment.getContent());
//                commentView.setTextSize(14f);
//                commentView.setPadding(10, 5, 10, 5);
//                commentsContainer.addView(commentView);
//            }
//        } else {
//            TextView noComments = new TextView(getContext());
//            noComments.setText("No comments yet.");
//            noComments.setTextSize(14f);
//            noComments.setPadding(10, 5, 10, 5);
//            commentsContainer.addView(noComments);
//        }
    }

    private void showDeleteConfirmation(long recipeId) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Recipe")
                .setMessage("Are you sure you want to delete this recipe?")
                .setPositiveButton("Yes", (dialog, which) -> deleteRecipe(recipeId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    //Deleting a recipe
    private void deleteRecipe(long recipeId) {
        RecipeApiService apiService = RetrofitClient.getClient().create(RecipeApiService.class);

        // Make the API call to delete the recipe
        Call<Void> call = apiService.deleteRecipe(recipeId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Handle success response, possibly showing the success message
                    Toast.makeText(getContext(), "Recipe deleted!", Toast.LENGTH_SHORT).show();

                    // Navigate to the RecipesViewFragment or back to the previous screen
                    NavHostFragment.findNavController(RecipeViewFragment.this)
                            .navigate(R.id.action_recipeViewFragment_to_recipesViewFragment);
                } else {
                    // Log the error body and show the error message
                    Toast.makeText(getContext(), "Failed to delete recipe", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Handle network failure
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }





}



