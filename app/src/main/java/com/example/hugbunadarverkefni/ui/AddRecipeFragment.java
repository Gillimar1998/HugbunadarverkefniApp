package com.example.hugbunadarverkefni.ui;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;



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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.hugbunadarverkefni.R;
import com.example.hugbunadarverkefni.api.RecipeApiService;
import com.example.hugbunadarverkefni.api.RetrofitClient;
import com.example.hugbunadarverkefni.api.UserApiService;
import com.example.hugbunadarverkefni.databinding.FragmentAddRecipeBinding;
import com.example.hugbunadarverkefni.model.Recipe;
import com.example.hugbunadarverkefni.model.User;

import java.io.FileOutputStream;
import java.io.IOException;
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
    private Uri imageUri;
    private ImageView recipeImagePreview;
    private boolean private_post = false;
    private static final int STORAGE_PERMISSION_CODE = 100;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri cameraImageUri;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddRecipeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Uri selectedImage = null;

                        if (result.getData() != null && result.getData().getData() != null) {
                            // 📸 Gallery selected
                            selectedImage = result.getData().getData();
                            requireContext().getContentResolver().takePersistableUriPermission(
                                    selectedImage, Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                            Log.d("RecipeUpload", "✅ Selected from gallery: " + selectedImage);
                        } else {
                            // 📷 Camera image was saved to our FileProvider URI
                            selectedImage = cameraImageUri;
                            Log.d("RecipeUpload", "✅ Taken from camera: " + selectedImage);
                        }

                        if (selectedImage != null) {
                            imageUri = selectedImage;
                            previewImage(imageUri);
                        }
                    }
                }
        );



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
                            try {
                                File imageFile = copyUriToFile(imageUri);
                                RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
                                imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), requestBody);
                                Log.d("RecipeUpload", "✅ Copied image for upload: " + imageFile.getAbsolutePath());
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }


                        // Make the API call to add the recipe

                        if (imagePart != null) {
                            try {
                                Log.d("RecipeUpload", "✅ Image is ready for upload: " + imagePart.body().contentLength());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            Log.d("RecipeUpload", "❌ Image part is null");
                        }
                        RecipeApiService apiService = RetrofitClient.getClient().create(RecipeApiService.class);
                        Call<Recipe> callRecipe = apiService.addRecipe(namePart, descriptionPart, categoryPart, cookTimePart, userIdPart, privatePart, imagePart);

                        callRecipe.enqueue(new Callback<Recipe>() {
                            @Override
                            public void onResponse(Call<Recipe> call, Response<Recipe> response) {
                                Recipe uploadedRecipe = response.body();
                                if (uploadedRecipe != null) {
                                    Log.d("RecipeUpload", "Recipe title: " + uploadedRecipe.getName());
                                    Log.d("RecipeUpload", "Image URL: " +
                                            (uploadedRecipe.getImage() != null ? uploadedRecipe.getImage().getUrl() : "No image"));
                                }
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
        Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        galleryIntent.setType("image/*");
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Create a temporary file for the photo
        File imageFile = new File(requireContext().getCacheDir(), "camera_image.jpg");

        cameraImageUri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".fileprovider",
                imageFile
        );
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);

        // Combine intents
        Intent chooser = Intent.createChooser(galleryIntent, "Select or take a picture");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});

        imagePickerLauncher.launch(chooser);
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

    private File copyUriToFile(Uri uri) throws IOException {
        File tempFile = new File(requireContext().getCacheDir(), "temp_recipe_image.jpg");

        try (InputStream in = requireContext().getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(tempFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
        return tempFile;
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
