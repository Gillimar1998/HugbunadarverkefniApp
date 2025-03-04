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
        Log.d("LoginDebug", "User ID saved: " + userId);  // Log the user ID to check if it's saved properly


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
                        String cooktime = "PT" + binding.cookTime.getText().toString() + "M"; // Convert to PT format

                        if (title.isEmpty() || description.isEmpty()) {
                            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Create the new Recipe with the full User object
                        Recipe newRecipe = new Recipe(title, loggedInUser, description, cooktime);

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
                    Log.e("LoginDebug", "Failed to fetch user. Response code: " + response.code());
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
