package com.example.hugbunadarverkefni.ui;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.hugbunadarverkefni.R;
import com.example.hugbunadarverkefni.adapter.RecipeAdapter;
import com.example.hugbunadarverkefni.api.RecipeApiService;
import com.example.hugbunadarverkefni.api.RetrofitClient;
import com.example.hugbunadarverkefni.api.UserApiService;
import com.example.hugbunadarverkefni.databinding.FragmentAddRecipeBinding;
import com.example.hugbunadarverkefni.databinding.FragmentRecipesViewBinding;
import com.example.hugbunadarverkefni.model.Recipe;
import com.example.hugbunadarverkefni.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddRecipeFragment extends Fragment {
    private FragmentAddRecipeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddRecipeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Handle Save Button
        binding.btnSubmitRecipe.setOnClickListener(v -> submitRecipe());

        // Handle Cancel Button
        binding.btnCancel.setOnClickListener(v -> {
            // Navigate back without saving
            NavHostFragment.findNavController(AddRecipeFragment.this).navigateUp();
        });

    }

    private void submitRecipe() {
        // Retrieve user ID from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        long userId = sharedPreferences.getLong("user_Id", -1);  // Default to -1 if not found


        if (userId == -1) {
            Toast.makeText(getContext(), "User is not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create an API service instance
        UserApiService userApiService = RetrofitClient.getClient().create(UserApiService.class);

        // Make an API call to fetch the user details by ID
        Call<User> userCall = userApiService.getUserById(userId);

        userCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    User loggedInUser = response.body();
                    if (loggedInUser != null) {
                        String title = binding.recipeTitle.getText().toString().trim();
                        String description = binding.recipeDescription.getText().toString().trim();
                        String category = binding.recipeCategory.getText().toString().trim(); // Ensure category field exists
                        Boolean privatePost = Boolean.FALSE;
                        String cookTimeStr = binding.cookTime.getText().toString().trim();
                        int cookTime = 0;

                        // Validate required fields
                        if (title.isEmpty() || description.isEmpty() || category.isEmpty() || cookTimeStr.isEmpty()) {
                            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Validate cook time input
                        try {
                            cookTime = Integer.parseInt(cookTimeStr);
                        } catch (NumberFormatException e) {
                            Toast.makeText(getContext(), "Cook time must be a number", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Create the new Recipe
                        Recipe newRecipe = new Recipe(title, userId, description, category, cookTime, privatePost);

                        // Make API call to add the recipe
                        RecipeApiService apiService = RetrofitClient.getClient().create(RecipeApiService.class);
                        Call<Recipe> callRecipe = apiService.addRecipe(newRecipe);

                        callRecipe.enqueue(new Callback<Recipe>() {
                            @Override
                            public void onResponse(Call<Recipe> call, Response<Recipe> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(getContext(), "Recipe added!", Toast.LENGTH_SHORT).show();
                                    NavHostFragment.findNavController(AddRecipeFragment.this).navigateUp(); // Go back
                                } else {
                                    Toast.makeText(getContext(), "Failed to add recipe", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Recipe> call, Throwable t) {
                                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "User data is null", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to fetch user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
