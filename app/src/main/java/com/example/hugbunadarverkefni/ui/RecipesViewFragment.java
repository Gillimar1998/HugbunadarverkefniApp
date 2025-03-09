package com.example.hugbunadarverkefni.ui;

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
import com.example.hugbunadarverkefni.databinding.FragmentRecipesViewBinding;
import com.example.hugbunadarverkefni.model.Recipe;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipesViewFragment extends Fragment {
    private FragmentRecipesViewBinding binding;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipeList = new ArrayList<>();
    private List<Recipe> fullRecipeList = new ArrayList<>(); // Stores all recipes
    private Set<String> uniqueCategories = new HashSet<>(); // Stores unique categories
    private Set<String> selectedCategories = new HashSet<>(); // Tracks selected filters
    private EditText searchInput;
    private Button searchButton, clearFiltersButton; // Clear filters button
    private LinearLayout filterLayout; // Layout for category checkboxes
    private Spinner sortSpinner; // spinner for sort methods
    private String selectedSortOption = "A to Z";

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

        // Initialize UI elements
        searchInput = binding.getRoot().findViewById(R.id.searchInput);
        searchButton = binding.getRoot().findViewById(R.id.searchButton);
        filterLayout = binding.getRoot().findViewById(R.id.filterLayout);
        TextView sortLabel = view.findViewById(R.id.sortLabel);
        sortSpinner = view.findViewById(R.id.sortSpinner);

        // Floating Action Button for Adding Recipes
        FloatingActionButton fabAddRecipe = view.findViewById(R.id.fabAddRecipe);
        fabAddRecipe.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_RecipesViewFragment_to_AddRecipeFragment)
        );

        // Sorting options:
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(), R.array.sort_options, android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSortOption = parent.getItemAtPosition(position).toString();
                sortLabel.setText("Sorting by: " + selectedSortOption); // Update label dynamically
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Create "Clear Filters" button
        clearFiltersButton = new Button(getContext());
        clearFiltersButton.setText("Reset filters");
        clearFiltersButton.setOnClickListener(v -> clearFilters());
        filterLayout.addView(clearFiltersButton); // Add to layout

        // Search button functionality
        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim().toLowerCase();
            Log.d("SearchDebug", "Search button clicked with query: " + query);
            filterAndSortRecipes(query);
        });



        Button accountSettingsButton = view.findViewById(R.id.btnAccountSettings);
        accountSettingsButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_RecipesViewFragment_to_AccountSettingsFragment)
        );

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

                    applyFilters();

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
        createFilterCheckboxes();
    }

    private void createFilterCheckboxes() {
        getActivity().runOnUiThread(() -> {
            filterLayout.removeAllViews(); // Clear previous buttons
            filterLayout.addView(clearFiltersButton); // Ensure "All" button is at the top

            // Create checkboxes for each category
            for (String category : uniqueCategories) {
                CheckBox categoryCheckBox = new CheckBox(getContext());
                categoryCheckBox.setText(category);
                categoryCheckBox.setOnClickListener(v -> toggleCategoryFilter(category, categoryCheckBox.isChecked()));
                filterLayout.addView(categoryCheckBox);
            }
        });
    }

    private void clearFilters() {
        selectedCategories.clear(); // Clears only category filters
        searchInput.setText("");
        filterLayout.removeAllViews();
        filterLayout.addView(clearFiltersButton);
        createFilterCheckboxes();

        // Preserve the current sorting order
        applyFilters();
    }


    private void toggleCategoryFilter(String category, boolean isChecked) {
        if (isChecked) {
            selectedCategories.add(category);
        } else {
            selectedCategories.remove(category);
        }
        applyFilters();
    }

    private void applyFilters() {
        String query = searchInput.getText().toString().trim().toLowerCase();
        filterAndSortRecipes(query);
    }

    private void filterAndSortRecipes(String query) {
        List<Recipe> filteredList = new ArrayList<>();

        for (Recipe recipe : fullRecipeList) {
            boolean matchesSearch = query.isEmpty() || recipe.getName().toLowerCase().contains(query);
            boolean matchesCategory = selectedCategories.isEmpty() || selectedCategories.contains(recipe.getCategory());

            if (matchesSearch && matchesCategory) {
                filteredList.add(recipe);
            }
        }

        sortRecipes(filteredList);
        updateRecyclerView(filteredList);
    }

    private void sortRecipes(List<Recipe> recipes){
        switch(selectedSortOption){
            case "A to Z":
                recipes.sort(Comparator.comparing(Recipe::getName));
                break;
            case "Newest to Oldest":
                recipes.sort(Comparator.comparing(Recipe::getCreationDate).reversed());
                break;
            case "Oldest to Newest":
                recipes.sort(Comparator.comparing(Recipe::getCreationDate));
                break;
            case "Like Count":
                recipes.sort(Comparator.comparing(Recipe::getLikeCount).reversed());
                break;
            case "Cook Time":
                recipes.sort(Comparator.comparing(Recipe::getCookTime));
                break;
            default:
                recipes.sort(Comparator.comparing(Recipe::getName));
        }
    }

    private void updateRecyclerView(List<Recipe> recipes) {
        getActivity().runOnUiThread(() -> {
            recipeList.clear();
            recipeList.addAll(recipes);
            recipeAdapter.notifyDataSetChanged();
        });
    }

    private void openRecipeDetails(Recipe recipe) {
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
