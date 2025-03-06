package com.example.hugbunadarverkefni.ui;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import java.io.File;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddRecipeFragment extends Fragment {
    private FragmentAddRecipeBinding binding;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView recipeImagePreview;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddRecipeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get reference to the ImageView where the image will be displayed
        recipeImagePreview = view.findViewById(R.id.recipeImage);

        // Set the placeholder image initially
        recipeImagePreview.setImageResource(R.drawable.ic_placeholder);

        // Add an onClickListener to allow the user to pick an image
        Button btnSelectImage = view.findViewById(R.id.btnSelectImage);

        btnSelectImage.setOnClickListener(v -> openImageChooser());

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
                        String category = binding.recipeCategory.getText().toString().trim(); // Ensure category field exists
                        String cookTimeStr = binding.cookTime.getText().toString().trim();
                        int cookTime = 0;

                        // Validate required fields
                        if (title.isEmpty() || description.isEmpty() || category.isEmpty() || cookTimeStr.isEmpty()) {
                            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Validate cook time input
                        try {
                            cookTime = Integer.parseInt(cookTimeStr);
                        } catch (NumberFormatException e) {
                            Toast.makeText(getContext(), "Cook time must be a number", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Prepare the request body for the API
                        RequestBody namePart = RequestBody.create(MediaType.parse("text/plain"), title);
                        RequestBody descriptionPart = RequestBody.create(MediaType.parse("text/plain"), description);
                        RequestBody categoryPart = RequestBody.create(MediaType.parse("text/plain"), category);
                        RequestBody cookTimePart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(cookTime));
                        RequestBody userIdPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(userId));

                        // Handle image if available
                        MultipartBody.Part imagePart = null;
                        if (imageUri != null) {
                            File imageFile = new File(getRealPathFromURI(imageUri));
                            if (imageFile.exists()) { // Ensure the file exists
                                RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
                                imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), requestBody);
                            }
                        }

                        // Make the API call to add the recipe
                        RecipeApiService apiService = RetrofitClient.getClient().create(RecipeApiService.class);
                        Call<Recipe> callRecipe = apiService.addRecipe(namePart, descriptionPart, categoryPart, cookTimePart, userIdPart, imagePart);

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
                    Toast.makeText(getContext(), "Failed to fetch user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            previewImage(imageUri);
        }
    }

    private void previewImage(Uri imageUri) {
        // Check if imageUri is null before setting it
        if (imageUri != null) {
            recipeImagePreview.setImageURI(imageUri);
        } else {
            // If no image is selected, keep the placeholder
            recipeImagePreview.setImageResource(R.drawable.ic_placeholder);
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = requireActivity().getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        return null;
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
