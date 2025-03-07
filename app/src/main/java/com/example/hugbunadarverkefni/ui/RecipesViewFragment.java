package com.example.hugbunadarverkefni.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.hugbunadarverkefni.R;
import com.example.hugbunadarverkefni.adapter.RecipeAdapter;
import com.example.hugbunadarverkefni.api.RecipeApiService;
import com.example.hugbunadarverkefni.api.RetrofitClient;
import com.example.hugbunadarverkefni.databinding.FragmentRecipesViewBinding;
import com.example.hugbunadarverkefni.model.Recipe;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipesViewFragment extends Fragment {
    private FragmentRecipesViewBinding binding;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipeList = new ArrayList<>();
    private List<Recipe> fullRecipeList = new ArrayList<>(); // Stores all recipes
    private Set<String> uniqueCategories = new HashSet<>(); // Stores unique categories
    private EditText searchInput;
    private Button searchButton;
    private LinearLayout filterLayout; // Layout for dynamic category buttons

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecipesViewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup RecyclerView
        recipeAdapter = new RecipeAdapter(getContext(), recipeList, this::openRecipeDetails);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(recipeAdapter);

        // Initialize search input, button, and filter layout
        searchInput = binding.getRoot().findViewById(R.id.searchInput);
        searchButton = binding.getRoot().findViewById(R.id.searchButton);
        filterLayout = binding.getRoot().findViewById(R.id.filterLayout);

        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim().toLowerCase();
            Log.d("SearchDebug", "Search button clicked with query: " + query);

            if (!query.isEmpty()) {
                filterLocalRecipes(query);
            } else {
                updateRecyclerView(fullRecipeList); // Show all recipes if search is cleared
            }
        });

        // Floating Action Button click listener
        binding.getRoot().findViewById(R.id.fabAddRecipe).setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_RecipesViewFragment_to_AddRecipeFragment);
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

                    // Extract unique categories
                    extractCategories(fullRecipeList);

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

    private void extractCategories(List<Recipe> recipes) {
        uniqueCategories.clear();
        for (Recipe recipe : recipes) {
            if (recipe.getCategory() != null) {
                uniqueCategories.add(recipe.getCategory()); // Add unique category
            }
        }
        createFilterButtons();
    }

    private void createFilterButtons() {
        getActivity().runOnUiThread(() -> {
            filterLayout.removeAllViews(); // Clear previous buttons

            // Add "All" button to reset filter
            Button allButton = new Button(getContext());
            allButton.setText("All");
            allButton.setOnClickListener(v -> updateRecyclerView(fullRecipeList));
            filterLayout.addView(allButton);

            // Create buttons for each unique category
            for (String category : uniqueCategories) {
                Button categoryButton = new Button(getContext());
                categoryButton.setText(category);
                categoryButton.setOnClickListener(v -> filterByCategory(category));
                filterLayout.addView(categoryButton);
            }
        });
    }

    private void filterByCategory(String category) {
        Log.d("FilterDebug", "Filtering by category: " + category);
        List<Recipe> filteredList = new ArrayList<>();

        for (Recipe recipe : fullRecipeList) {
            if (recipe.getCategory().equalsIgnoreCase(category)) {
                filteredList.add(recipe);
            }
        }

        Log.d("FilterDebug", "Filtered results: " + filteredList.size() + " recipes found for " + category);
        updateRecyclerView(filteredList);
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

    private void openRecipeDetails(Recipe recipe) {
        Log.d("RecipeClick", "Clicked on Recipe: " + recipe.getName());

        Bundle bundle = new Bundle();
        bundle.putLong("recipeId", recipe.getId());

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_RecipesViewFragment_to_RecipeViewFragment, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
