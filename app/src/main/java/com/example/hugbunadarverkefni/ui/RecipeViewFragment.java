package com.example.hugbunadarverkefni.ui;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.example.hugbunadarverkefni.R;
import com.example.hugbunadarverkefni.api.RecipeApiService;
import com.example.hugbunadarverkefni.api.RetrofitClient;
import com.example.hugbunadarverkefni.model.Recipe;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeViewFragment extends Fragment {
    private TextView titleTextView;
    private long recipeId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_view, container, false);

        // Initialize UI elements
        titleTextView = view.findViewById(R.id.recipeName); // Make sure this ID exists in XML

        // Get recipe ID from arguments
        if (getArguments() != null) {
            recipeId = getArguments().getLong("recipeId", -1);
            if (recipeId != -1) {
                fetchRecipeDetails(recipeId);
            }
        }

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
                    titleTextView.setText(recipe.getName()); // Use getName() instead of getTitle()
                } else {
                    Log.e("API Error", "Failed to fetch recipe details: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<Recipe> call, Throwable t) {
                Log.e("API Error", "Network Failure: " + t.getMessage());
            }
        });
    }
}


