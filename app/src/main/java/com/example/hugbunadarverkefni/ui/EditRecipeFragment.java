package com.example.hugbunadarverkefni.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.navigation.fragment.NavHostFragment;
import com.example.hugbunadarverkefni.R;
import com.example.hugbunadarverkefni.api.RecipeApiService;
import com.example.hugbunadarverkefni.api.RetrofitClient;
import com.example.hugbunadarverkefni.model.Recipe;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditRecipeFragment extends Fragment {

    private EditText etName, etDescription, etCategory, etCookTime;
    private Button btnSave;
    private long recipeId;
    private Recipe loadedRecipe;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_recipe, container, false);

        // initialize UI
        etName = view.findViewById(R.id.etRecipeName);
        etDescription = view.findViewById(R.id.etRecipeDescription);
        etCategory = view.findViewById(R.id.etRecipeCategory);
        etCookTime = view.findViewById(R.id.etRecipeCookTime);
        btnSave = view.findViewById(R.id.btnSaveChanges);

        if (getArguments() != null) {
            recipeId = getArguments().getLong("recipeId", -1);
            if (recipeId != -1) {
                loadRecipeDetails(recipeId);
            } else {
                Toast.makeText(getContext(), "Invalid recipe ID", Toast.LENGTH_SHORT).show();
            }
        }

        btnSave.setOnClickListener(v -> saveRecipe());

        return view;
    }

    private void loadRecipeDetails(long id) {
        RecipeApiService api = RetrofitClient.getClient().create(RecipeApiService.class);
        api.getRecipeById(id).enqueue(new Callback<Recipe>() {
            @Override
            public void onResponse(Call<Recipe> call, Response<Recipe> response) {
                if (response.isSuccessful() && response.body() != null) {
                    loadedRecipe = response.body();  // Store original recipe
                    etName.setText(loadedRecipe.getName());
                    etDescription.setText(loadedRecipe.getDescription());
                    etCategory.setText(loadedRecipe.getCategory());
                    etCookTime.setText(String.valueOf(loadedRecipe.getCookTime()));
                } else {
                    Toast.makeText(getContext(), "Failed to load recipe", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Recipe> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void saveRecipe() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String cookTimeStr = etCookTime.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(description) ||
                TextUtils.isEmpty(category) || TextUtils.isEmpty(cookTimeStr)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int cookTime = Integer.parseInt(cookTimeStr);

        // Build JSON manually
        JSONObject json = new JSONObject();
        try {
            json.put("id", recipeId);
            json.put("name", name);
            json.put("description", description);
            json.put("category", category);
            json.put("cookTime", cookTime);
        } catch (Exception e) {
            Toast.makeText(getContext(), "JSON error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        // Create RequestBody
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(mediaType, json.toString());

        // Send PATCH request
        RecipeApiService api = RetrofitClient.getClient().create(RecipeApiService.class);
        api.patchRecipe(recipeId, requestBody).enqueue(new Callback<Recipe>() {
            @Override
            public void onResponse(Call<Recipe> call, Response<Recipe> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Recipe updated!", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(EditRecipeFragment.this).popBackStack();
                } else {
                    Toast.makeText(getContext(), "Failed to update recipe", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Recipe> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}