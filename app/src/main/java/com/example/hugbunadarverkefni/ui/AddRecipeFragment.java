package com.example.hugbunadarverkefni.ui;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import static com.google.gson.internal.$Gson$Types.arrayOf;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
    private boolean private_post = false;
    private static final int STORAGE_PERMISSION_CODE = 100;

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

        // An onClickListener to allow the user to pick an image
        Button btnSelectImage = view.findViewById(R.id.btnSelectImage);

        btnSelectImage.setOnClickListener(v -> openImageChooser());

        binding.switchPrivatePost.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                private_post = true;
                Toast.makeText(getContext(), "Post is private", Toast.LENGTH_SHORT).show();
            } else {
                private_post = false;
                Toast.makeText(getContext(), "Post is not private", Toast.LENGTH_SHORT).show();
            }
        });

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
                        String category = binding.recipeCategory.getText().toString().trim();
                        String cookTimeStr = binding.cookTime.getText().toString().trim();
                        int cookTime = 0;

                        if (title.isEmpty() || description.isEmpty() || category.isEmpty() || cookTimeStr.isEmpty()) {
                            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try {
                            cookTime = Integer.parseInt(cookTimeStr);
                        } catch (NumberFormatException e) {
                            Toast.makeText(getContext(), "Cook time must be a number", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        RequestBody namePart = RequestBody.create(MediaType.parse("text/plain"), title);
                        RequestBody descriptionPart = RequestBody.create(MediaType.parse("text/plain"), description);
                        RequestBody categoryPart = RequestBody.create(MediaType.parse("text/plain"), category);
                        RequestBody cookTimePart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(cookTime));
                        RequestBody userIdPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(userId));
                        RequestBody privatePart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(private_post));

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
                        Call<Recipe> callRecipe = apiService.addRecipe(namePart, descriptionPart, categoryPart, cookTimePart, userIdPart, privatePart, imagePart);

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
                                Toast.makeText(getContext(), "Error in adding: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e("AddRecipe", "Error in adding: " + t.getMessage());
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
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        } else {
            pickImageFromGallery();
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery();
            } else {
                Toast.makeText(getContext(), "Permission denied. Cannot select image.", Toast.LENGTH_SHORT).show();
            }
        }
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

    private String getRealPathFromURI(Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        String fileName = cursor.getString(index);
                        File file = new File(requireContext().getCacheDir(), fileName);
                        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                             OutputStream outputStream = new FileOutputStream(file)) {
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = inputStream.read(buffer)) > 0) {
                                outputStream.write(buffer, 0, length);
                            }
                            return file.getAbsolutePath();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String[] proj = {MediaStore.Images.Media.DATA};
            try (Cursor cursor = requireActivity().getContentResolver().query(uri, proj, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    return cursor.getString(columnIndex);
                }
            }
        }
        return null;
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
