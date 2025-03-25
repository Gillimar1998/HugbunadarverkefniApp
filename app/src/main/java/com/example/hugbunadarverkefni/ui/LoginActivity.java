package com.example.hugbunadarverkefni.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hugbunadarverkefni.R;
import com.example.hugbunadarverkefni.api.RetrofitClient;
import com.example.hugbunadarverkefni.api.UserApiService;
import com.example.hugbunadarverkefni.model.LoginRequest;
import com.example.hugbunadarverkefni.model.User;

import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameInput, passwordInput;
    private Button loginButton, registerButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);


        if (isLoggedIn) {
            // If user is already logged in, go to MainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        usernameInput = findViewById(R.id.username);
        passwordInput = findViewById(R.id.password);
        loginButton = findViewById(R.id.btnLogin);
        registerButton = findViewById(R.id.btnRegister);

        loginButton.setOnClickListener(view -> loginUser());
        registerButton.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }



        UserApiService apiService = RetrofitClient.getUserApiService();


        LoginRequest loginRequest = new LoginRequest(username, "", password);

        Call<User> call = apiService.login(loginRequest);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();

                    // Save user session in SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", user.getUsername());
                    editor.putLong("user_Id", user.getId());
                    editor.putBoolean("isAdmin", user.isAdmin());
                    editor.putBoolean("isLoggedIn", true);

                    Call<Set<String>> call1 = apiService.getUserFavorites(user.getId());
                    call1.enqueue(new Callback<Set<String>>() {
                        @Override
                        public void onResponse(Call <Set<String>> call1, Response<Set<String>> response1){
                            if (response1.body() != null && response1.isSuccessful()) {
                                Set<String> favorites = response1.body();
                                editor.putStringSet("favorites", favorites);
                                editor.apply();
                                Log.d("Favorites", "Favorites saved in session " + response1.body());
                            } else {
                                Log.e("Favorites", "Failed to fetch favorites: " + response1.errorBody());
                            }
                        }

                        @Override
                        public void onFailure(Call<Set<String>> call, Throwable t) {
                            Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });



                    Log.d("LoginDebug", "User logged in: " + user.getUsername());

                    Toast.makeText(LoginActivity.this, "Welcome, " + user.getUsername(), Toast.LENGTH_SHORT).show();

                    // Go to MainActivity after successful login
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid login!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
