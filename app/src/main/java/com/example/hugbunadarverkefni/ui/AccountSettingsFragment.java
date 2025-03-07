package com.example.hugbunadarverkefni.ui;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.hugbunadarverkefni.R;
import com.example.hugbunadarverkefni.api.UserApiService;
import com.example.hugbunadarverkefni.api.RecipeApiService;
import com.example.hugbunadarverkefni.api.RetrofitClient;
import com.example.hugbunadarverkefni.model.User;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.MediaType;

public class AccountSettingsFragment extends Fragment {

    private EditText emailField, usernameField, passwordField;
    private Button saveButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_settings, container, false);

        emailField = view.findViewById(R.id.editEmail);
        usernameField = view.findViewById(R.id.editUsername);
        passwordField = view.findViewById(R.id.editPassword);
        saveButton = view.findViewById(R.id.btnSave);

        Long userId = getUserIdFromStorage(); // Fetch userId from SharedPreferences or arguments
        if (userId != null) {
            loadUserData(userId); // Load the user's existing data
        } else {
            Log.e("AccountSettings", "User ID is null");
        }

        saveButton.setOnClickListener(v -> {
            String newUsername = usernameField.getText().toString();
            String newEmail = emailField.getText().toString();
            String newPassword = passwordField.getText().toString();

            updateUser(userId, newUsername, newEmail, newPassword);
        });

        return view;
    }



    private void updateUser(Long userId, String newUsername, String newEmail, String newPassword) {
        UserApiService apiService = RetrofitClient.getClient().create(UserApiService.class);

        // Fetch the existing user to retain the 'admin' status
        Call<User> getUserCall = apiService.getUserById(userId);
        getUserCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User existingUser = response.body();
                    boolean isAdmin = existingUser.isAdmin(); // Retain admin status

                    // Create updated user object while keeping the admin status
                    User updatedUser = new User(newUsername, newEmail, newPassword, isAdmin);

                    // Now send the update request
                    Call<User> updateCall = apiService.updateUser(userId, updatedUser);
                    updateCall.enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            if (response.isSuccessful()) {
                                Log.d("UpdateUser", "User updated successfully: " + response.body());
                            } else {
                                Log.e("UpdateUser", "Failed to update user, response code: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            Log.e("UpdateUser", "Network failure: " + t.getMessage());
                        }
                    });

                } else {
                    Log.e("GetUser", "Failed to fetch user data, response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("GetUser", "Network failure: " + t.getMessage());
            }
        });
    }

    private void loadUserData(Long userId) {
        UserApiService apiService = RetrofitClient.getClient().create(UserApiService.class);

        Call<User> call = apiService.getUserById(userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    usernameField.setText(user.getUsername());
                    emailField.setText(user.getEmail());
                    // Leave password empty for security reasons
                } else {
                    Log.e("LoadUser", "Failed to fetch user, response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("LoadUser", "Network failure: " + t.getMessage());
            }
        });
    }

    private Long getUserIdFromStorage() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        long userId = sharedPreferences.getLong("user_Id", -1);
        return userId;
    }


}
