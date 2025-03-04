package com.example.hugbunadarverkefni.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.widget.EditText;
import android.widget.Button;


public class RecipesViewFragment extends Fragment {
    private FragmentRecipesViewBinding binding;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipeList = new ArrayList<>();
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
        recipeAdapter = new RecipeAdapter(getContext(), recipeList, recipe -> {
            // Navigate to RecipeViewFragment and pass the recipe ID
            Bundle bundle = new Bundle();
            bundle.putLong("recipeId", recipe.getId());

            NavHostFragment.findNavController(RecipesViewFragment.this)
                    .navigate(R.id.action_RecipesViewFragment_to_RecipeViewFragment, bundle);
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(recipeAdapter);

        searchInput = binding.searchInput;
        searchButton = binding.searchButton;

        searchButton.setOnClickListener(v -> {
           String query = searchInput.getText().toString().trim();
           if(!query.isEmpty()){
               searchRecipes(query);

           }
           else{
               fetchRecipes(); // If there is nothing in the query, we get all of the recipes.
           }
        });

        // Fetch recipes directly in the fragment
        fetchRecipes();

        // Set up FloatingActionButton to navigate to AddRecipeFragment
        binding.fabAddRecipe.setOnClickListener(v -> {
            NavHostFragment.findNavController(RecipesViewFragment.this)
                   .navigate(R.id.action_RecipesViewFragment_to_AddRecipeFragment);
        });

    }

    private void fetchRecipes() {
        RecipeApiService apiService = RetrofitClient.getClient().create(RecipeApiService.class);
        Call<List<Recipe>> call = apiService.getRecipes();

        call.enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API Response", "Fetched " + response.body().size() + " recipes");

                    recipeList.clear();
                    recipeList.addAll(response.body());
                    recipeAdapter.notifyDataSetChanged();
                } else {
                    Log.e("API Error", "Failed to fetch data: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<List<Recipe>> call, Throwable t) {
                Log.e("API Error", "Network Failure: " + t.getMessage());
            }
        });
    }

    private void searchRecipes(String query){
        RecipeApiService apiService = RetrofitClient.getClient().create(RecipeApiService.class);
        Call<List<Recipe>> call = apiService.searchRecipes(query);

        call.enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(@NonNull Call<List<Recipe>> call, @NonNull Response<List<Recipe>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API Response", "Found " + response.body().size() + " recipes for search query: " + query);

                    recipeList.clear();
                    recipeList.addAll(response.body());
                    recipeAdapter.notifyDataSetChanged();
                } else {
                    Log.e("API Error", "Failed to fetch search results: " + response.errorBody());
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Recipe>> call, @NonNull Throwable t) {
                Log.e("API Error", "Network Failure: " + t.getMessage());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


