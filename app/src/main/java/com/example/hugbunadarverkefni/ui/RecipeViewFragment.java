package com.example.hugbunadarverkefni.ui;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.hugbunadarverkefni.R;
import com.example.hugbunadarverkefni.api.RecipeApiService;
import com.example.hugbunadarverkefni.api.RetrofitClient;
import com.example.hugbunadarverkefni.model.Comment;
import com.example.hugbunadarverkefni.model.Recipe;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeViewFragment extends Fragment {

    private TextView titleTextView, categoryTextView, cookTimeTextView, descriptionTextView, likesTextView;
    private ImageView recipeImageView;
    private LinearLayout commentsContainer;
    private ImageButton likeButton;
    private long recipeId;
    private int likeCount = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_view, container, false);

        // Initialize UI elements
        titleTextView = view.findViewById(R.id.recipeTitle);
        categoryTextView = view.findViewById(R.id.recipeCategory);
        cookTimeTextView = view.findViewById(R.id.recipeCookTime);
        descriptionTextView = view.findViewById(R.id.recipeDescription);
        likesTextView = view.findViewById(R.id.recipeLikes);
        recipeImageView = view.findViewById(R.id.ivRecipeImage); // ImageView for recipe image
        commentsContainer = view.findViewById(R.id.commentsContainer);
        likeButton = view.findViewById(R.id.btnLike);

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
            likeCount++;
            likesTextView.setText("Likes: " + likeCount);
            Toast.makeText(getContext(), "Liked!", Toast.LENGTH_SHORT).show();
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
        cookTimeTextView.setText("Duration: " + recipe.getCookTime() + " min");
        descriptionTextView.setText("Description: " + recipe.getDescription());
        likesTextView.setText("Likes: " + recipe.getLikeCount());

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
}



