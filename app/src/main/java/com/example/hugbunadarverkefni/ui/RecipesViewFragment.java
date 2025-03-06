package com.example.hugbunadarverkefni.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.hugbunadarverkefni.R;
import com.example.hugbunadarverkefni.adapter.RecipeAdapter;
import com.example.hugbunadarverkefni.api.RecipeApiService;
import com.example.hugbunadarverkefni.api.RetrofitClient;
import com.example.hugbunadarverkefni.databinding.FragmentRecipesViewBinding;
import com.example.hugbunadarverkefni.model.Recipe;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipesViewFragment extends Fragment {
    private FragmentRecipesViewBinding binding;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipeList = new ArrayList<>();
    private List<Recipe> fullRecipeList = new ArrayList<>(); // Stores all recipes
    private EditText searchInput;
    private Button searchButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecipesViewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup RecyclerView
        recipeAdapter = new RecipeAdapter(getContext(), recipeList, recipe -> Log.d("RecipeClick", "Clicked Recipe: " + recipe.getName()));
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(recipeAdapter);

        // Initialize search input and button
        searchInput = binding.getRoot().findViewById(R.id.searchInput);
        searchButton = binding.getRoot().findViewById(R.id.searchButton);

        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim().toLowerCase();
            Log.d("SearchDebug", "Search button clicked with query: " + query);

            if (!query.isEmpty()) {
                filterLocalRecipes(query);
            } else {
                updateRecyclerView(fullRecipeList); // Show all recipes if search is cleared
            }
        });

        // Fetch all recipes initially
        fetchRecipes();
    }

    private void fetchRecipes() {
        Log.d("RecipeFetch", "Fetching all recipes...");
        RecipeApiService apiService = RetrofitClient.getClient().create(RecipeApiService.class);
        Call<List<Recipe>> call = apiService.getRecipes();

        call.enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("RecipeFetch", "Fetched " + response.body().size() + " recipes");

                    fullRecipeList.clear();
                    fullRecipeList.addAll(response.body());
                    updateRecyclerView(fullRecipeList);
                } else {
                    Log.e("RecipeFetch", "Failed to fetch recipes: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<List<Recipe>> call, Throwable t) {
                Log.e("RecipeFetch", "Network Failure: " + t.getMessage());
            }
        });
    }

    private void filterLocalRecipes(String query) {
        Log.d("SearchDebug", "Filtering recipes locally for query: " + query);
        List<Recipe> filteredList = new ArrayList<>();

        for (Recipe recipe : fullRecipeList) {
            if (recipe.getName().toLowerCase().contains(query) || (recipe.getDescription() != null && recipe.getDescription().toLowerCase().contains(query))) {
                filteredList.add(recipe);
            }
        }

        Log.d("SearchDebug", "Filtered results: " + filteredList.size() + " recipes match the search.");
        updateRecyclerView(filteredList);
    }

    private void updateRecyclerView(List<Recipe> recipes) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                recipeList.clear();
                recipeList.addAll(recipes);
                recipeAdapter.notifyDataSetChanged();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
