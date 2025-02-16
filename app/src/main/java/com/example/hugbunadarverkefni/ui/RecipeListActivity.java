package com.example.hugbunadarverkefni.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hugbunadarverkefni.api.RecipeApiService;
import com.example.hugbunadarverkefni.adapter.RecipeAdapter;
import com.example.hugbunadarverkefni.model.Recipe;
import com.example.hugbunadarverkefni.api.RetrofitClient;
import com.example.hugbunadarverkefni.R;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RecipeListActivity.this, LoginActivity.class));
                finish();
            }
        });

        fetchRecipes();
    }

    private void fetchRecipes() {
        RecipeApiService apiService = RetrofitClient.getClient().create(RecipeApiService.class);
        Call<List<Recipe>> call = apiService.getRecipes();

        call.enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(@NonNull Call<List<Recipe>> call, @NonNull Response<List<Recipe>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Recipe> recipeList = response.body();
                    adapter = new RecipeAdapter(RecipeListActivity.this, recipeList);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(RecipeListActivity.this, "Failed to load recipes", Toast.LENGTH_SHORT).show();
                    Log.e("API_ERROR", "Response: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Recipe>> call, @NonNull Throwable t) {
                Toast.makeText(RecipeListActivity.this, "Error fetching recipes", Toast.LENGTH_SHORT).show();
                Log.e("API_ERROR", "Error: " + t.getMessage());
            }
        });
    }
}
