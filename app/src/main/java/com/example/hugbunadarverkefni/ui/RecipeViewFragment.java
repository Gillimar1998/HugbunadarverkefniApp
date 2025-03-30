package com.example.hugbunadarverkefni.ui;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.widget.EditText;

import com.bumptech.glide.Glide;

import androidx.navigation.fragment.NavHostFragment;
import com.example.hugbunadarverkefni.R;
import com.example.hugbunadarverkefni.api.RecipeApiService;
import com.example.hugbunadarverkefni.api.RetrofitClient;
import com.example.hugbunadarverkefni.api.UserApiService;
import com.example.hugbunadarverkefni.model.Comment;
import com.example.hugbunadarverkefni.model.Recipe;
import com.example.hugbunadarverkefni.model.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeViewFragment extends Fragment {

    private TextView titleTextView, categoryTextView, cookTimeTextView, descriptionTextView, likesTextView, recipePrivate;
    private ImageView recipeImageView;
    private LinearLayout commentsContainer;
    private SharedPreferences sharedPreferences;
    private UserApiService userApiService;
    private ImageButton likeButton;
    private long recipeId, userId;
    private int likeCount = 0;
    private Button btnDeleteRecipe, btnEditRecipe;
    private EditText commentInput;
    private Button commentSubmit;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_view, container, false);

        // Initialize UI elements
        titleTextView = view.findViewById(R.id.recipeTitle);
        categoryTextView = view.findViewById(R.id.recipeCategory);
        cookTimeTextView = view.findViewById(R.id.recipeCookTime);
        descriptionTextView = view.findViewById(R.id.recipeDescription);
        recipePrivate = view.findViewById(R.id.recipePrivate);
        likesTextView = view.findViewById(R.id.recipeLikes);
        recipeImageView = view.findViewById(R.id.ivRecipeImage); // ImageView for recipe image
        commentsContainer = view.findViewById(R.id.commentsContainer);
        likeButton = view.findViewById(R.id.btnLike);
        btnDeleteRecipe = view.findViewById(R.id.btnDeleteRecipe);
        btnEditRecipe = view.findViewById(R.id.btnEditRecipe);
        commentInput = view.findViewById(R.id.commentInput);
        commentSubmit = view.findViewById(R.id.commentSubmit);

        btnEditRecipe.setVisibility(View.GONE); // default hide the edit button



        // Get recipe ID from arguments
        if (getArguments() != null) {
            recipeId = getArguments().getLong("recipeId", -1);
            if (recipeId != -1) {
                fetchRecipeDetails(recipeId);
            } else {
                Toast.makeText(getContext(), "Invalid Recipe ID", Toast.LENGTH_SHORT).show();
            }
        }

        commentSubmit.setOnClickListener(v -> submitComment());


        // Like button click event
        likeButton.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
            long userId = sharedPreferences.getLong("user_Id", -1);

            if (userId == -1) {
                Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            Set<String> favorites = sharedPreferences.getStringSet("favorites", new HashSet<>());

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
                        List<Double> likedUserIds = (List<Double>) responseBody.get("likedUserIDs");
                        boolean isLiked = likedUserIds.contains((double) userId); // Check if the user is in the list

                        // Update UI
                        likesTextView.setText("Likes: " + likeCount);
                        if (isLiked) {
                            favorites.add(String.valueOf(recipeId));
                            editor.putStringSet("favorites", favorites);
                            editor.apply();
                            UserApiService userApiService = RetrofitClient.getClient().create(UserApiService.class);
                            Call<User> callObject = userApiService.addRecipeToFavorites(userId, recipeId);

                            callObject.enqueue(new Callback<User>(){
                                @Override
                                public void onResponse(Call<User> callobject, Response<User> responseUser) {
                                    User responseBodyUser = responseUser.body();
                                    Objects.requireNonNull(responseBodyUser).setFavorites(favorites);
                                }

                                @Override
                                public void onFailure(Call<User> callobject, Throwable t) {
                                    //Toast.makeText(getContext(), "Network error with user: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                            Toast.makeText(getContext(), "Liked and added to Favorites!", Toast.LENGTH_SHORT).show();
                            likeButton.setImageResource(R.drawable.ic_favorite_filled); // Change to "liked" image
                        } else {
                            favorites.remove(String.valueOf(recipeId));
                            editor.putStringSet("favorites", favorites);
                            editor.apply();
                            UserApiService userApiService = RetrofitClient.getClient().create(UserApiService.class);
                            Call<User> callObject = userApiService.removeRecipeFromFavorites(userId, recipeId);

                            callObject.enqueue(new Callback<User>(){
                                @Override
                                public void onResponse(Call<User> callobject, Response<User> responseUser) {
                                    User responseBodyUser = responseUser.body();
                                    Objects.requireNonNull(responseBodyUser).setFavorites(favorites);
                                }

                                @Override
                                public void onFailure(Call<User> callobject, Throwable t) {
                                    //Toast.makeText(getContext(), "Network error with user: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                            Toast.makeText(getContext(), "Unliked and removed from Favorites!", Toast.LENGTH_SHORT).show();
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

    private void submitComment() {
        String content = commentInput.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(getContext(), "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        long userId = sharedPreferences.getLong("user_Id", -1);

        RecipeApiService apiService = RetrofitClient.getClient().create(RecipeApiService.class);
        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId);
        body.put("content", content);

        apiService.postComment(recipeId, body).enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Comment added", Toast.LENGTH_SHORT).show();
                    fetchRecipeDetails(recipeId);
                    commentInput.setText("");
                } else {
                    Toast.makeText(getContext(), "Failed to add comment", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Comment> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        cookTimeTextView.setText("Duration: " + recipe.getCookTime() + " min");
        descriptionTextView.setText("Description: " + recipe.getDescription());
        likesTextView.setText("Likes: " + recipe.getLikeCount());
        recipePrivate.setText("PostPrivate :" + recipe.isPrivatePost());

        // Handle ImageView visibility
        if (recipe.getImage() != null && recipe.getImage().getUrl() != null && !recipe.getImage().getUrl().isEmpty()) {
            recipeImageView.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(recipe.getImage().getUrl())
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .into(recipeImageView);
        } else {
            recipeImageView.setVisibility(View.GONE); // Hide ImageView if no image is available
        }

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getLong("user_Id", -1);
        boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);

        if (isAdmin){
            recipePrivate.setVisibility(View.VISIBLE);
        } else {
            recipePrivate.setVisibility(View.INVISIBLE);
        }

        likesTextView.setText("Likes: " + recipe.getLikeCount());

        if (recipe.getUser().getId() == userId || isAdmin) {
            btnDeleteRecipe.setVisibility(View.VISIBLE);
            btnEditRecipe.setVisibility(View.VISIBLE);

            btnDeleteRecipe.setOnClickListener(v -> showDeleteConfirmation(recipeId));
            btnEditRecipe.setOnClickListener(v -> navigateToEditRecipe(recipeId));
        }

        // like button
        if (recipe.getLikedUserIDs().contains(userId)) {
            likeButton.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            likeButton.setImageResource(R.drawable.ic_favorite_border);
        }

        // Load comments dynamically
        commentsContainer.removeAllViews();
        if (recipe.getComments() != null && !recipe.getComments().isEmpty()) {
            for (Comment comment : recipe.getComments()) {
                LinearLayout commentLayout = new LinearLayout(getContext());
                commentLayout.setOrientation(LinearLayout.VERTICAL);
                commentLayout.setPadding(10, 5, 10, 5);

                // TextView for comment text
                TextView commentView = new TextView(getContext());
                commentView.setText(comment.getUser().getUsername() + ": " + comment.getContent());
                commentView.setTextSize(14f);
                commentLayout.addView(commentView);

                // ImageView for comment image (if available)
                if (comment.getImage() != null && comment.getImage().getUrl() != null) {
                    ImageView commentImageView = new ImageView(getContext());
                    commentImageView.setLayoutParams(new LinearLayout.LayoutParams(200, 200)); // Image size
                    commentImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    // Load image using Glide
                    Glide.with(this)
                            .load(comment.getImage().getUrl())
                            .placeholder(R.drawable.ic_placeholder_image)
                            .error(R.drawable.ic_placeholder_image)
                            .into(commentImageView);

                    commentLayout.addView(commentImageView);
                }

                commentsContainer.addView(commentLayout);
            }
        } else {
            TextView noComments = new TextView(getContext());
            noComments.setText("No comments yet.");
            noComments.setTextSize(14f);
            noComments.setPadding(10, 5, 10, 5);
            commentsContainer.addView(noComments);
        }
    }

    private void deleteComment(long commentId) {
        RecipeApiService apiService = RetrofitClient.getClient().create(RecipeApiService.class);
        apiService.deleteComment(recipeId, commentId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Comment deleted", Toast.LENGTH_SHORT).show();
                    fetchRecipeDetails(recipeId);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToEditRecipe(long recipeId) {
        Bundle bundle = new Bundle();
        bundle.putLong("recipeId", recipeId);
        NavHostFragment.findNavController(this).navigate(R.id.action_recipeViewFragment_to_EditRecipeFragment, bundle);
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



