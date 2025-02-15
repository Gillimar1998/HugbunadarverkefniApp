package com.example.hugbunadarverkefni.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.hugbunadarverkefni.R;
import com.example.hugbunadarverkefni.adapter.RecipeAdapter;
import com.example.hugbunadarverkefni.api.RecipeApiService;
import com.example.hugbunadarverkefni.api.RetrofitClient;
import com.example.hugbunadarverkefni.model.Recipe;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate activity_main.xml (which includes content_main.xml)
        setContentView(R.layout.activity_main);

        // Find views by ID (traditional approach)
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view ->
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null)
                        .show()
        );

        // Setup NavController with the NavHostFragment in content_main.xml
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // If you have a RecyclerView in activity_main.xml or content_main.xml, do:
        recyclerView = findViewById(R.id.recyclerView);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        // Fetch data (if you're using Retrofit)
        fetchRecipes();
    }

    private void fetchRecipes() {
        RecipeApiService apiService = RetrofitClient.getClient().create(RecipeApiService.class);
        Call<List<Recipe>> call = apiService.getRecipes();

        call.enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter = new RecipeAdapter(response.body());
                    if (recyclerView != null) {
                        recyclerView.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Recipe>> call, Throwable t) {
                Log.e("API Error", "Failed to fetch data: " + t.getMessage());
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        // For the Up button in the toolbar
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}

