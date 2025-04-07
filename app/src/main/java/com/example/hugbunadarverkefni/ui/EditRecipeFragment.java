package com.example.hugbunadarverkefni.ui;

import static android.app.Activity.RESULT_OK;

import com.bumptech.glide.Glide;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.hugbunadarverkefni.R;
import com.example.hugbunadarverkefni.api.RecipeApiService;
import com.example.hugbunadarverkefni.api.RetrofitClient;
import com.example.hugbunadarverkefni.model.Recipe;

import java.io.*;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditRecipeFragment extends Fragment {

    private EditText getName, getDescription, getCategory, getCookTime;
    private Button btnSave, btnSelectImage;
    private ImageView imagePreview;
    private Uri imageUri;
    private long recipeId;
    private Recipe loadedRecipe;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_recipe, container, false);

        getName = view.findViewById(R.id.getRecipeName);
        getDescription = view.findViewById(R.id.getRecipeDescription);
        getCategory = view.findViewById(R.id.getRecipeCategory);
        getCookTime = view.findViewById(R.id.getRecipeCookTime);
        btnSave = view.findViewById(R.id.btnSaveChanges);
        btnSelectImage = view.findViewById(R.id.btnSelectImage);
        imagePreview = view.findViewById(R.id.recipeImage);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        if (selectedImage != null) {
                            requireContext().getContentResolver().takePersistableUriPermission(
                                    selectedImage,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                            imageUri = selectedImage;
                            imagePreview.setImageURI(imageUri);
                        }
                    } else {
                        imageUri = null;
                        Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        btnSelectImage.setOnClickListener(v -> openImageChooser());

        if (getArguments() == null || !getArguments().containsKey("recipeId")) {
            Toast.makeText(getContext(), "Missing recipe data. Returning...", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();
            return view;
        }

        recipeId = getArguments().getLong("recipeId", -1);
        if (recipeId != -1) {
            loadRecipeDetails(recipeId);
        } else {
            Toast.makeText(getContext(), "Invalid recipe ID", Toast.LENGTH_SHORT).show();
        }

        btnSave.setOnClickListener(v -> saveRecipe());

        return view;
    }

    private void openImageChooser() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            imagePickerLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Could not open image picker: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File copyUriToFile(Uri uri) throws IOException {
        File outputDir = requireContext().getCacheDir();
        File tempFile = new File(outputDir, "temp_image.jpg");

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

    private void loadRecipeDetails(long id) {
        RecipeApiService api = RetrofitClient.getClient().create(RecipeApiService.class);
        api.getRecipeById(id).enqueue(new Callback<Recipe>() {
            @Override
            public void onResponse(Call<Recipe> call, Response<Recipe> response) {
                if (response.isSuccessful() && response.body() != null) {
                    loadedRecipe = response.body();
                    getName.setText(loadedRecipe.getName());
                    getDescription.setText(loadedRecipe.getDescription());
                    getCategory.setText(loadedRecipe.getCategory());
                    getCookTime.setText(String.valueOf(loadedRecipe.getCookTime()));
                    if (loadedRecipe.getImage() != null && loadedRecipe.getImage().getUrl() != null) {
                        String imageUrl = loadedRecipe.getImage().getUrl();
                        Glide.with(requireContext())
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_placeholder) // Add a placeholder
                                .error(R.drawable.ic_placeholder) // Add an error image
                                .into(imagePreview);
                    } else {
                        Toast.makeText(getContext(), "No image found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load recipe", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Recipe> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveRecipe() {
        String name = getName.getText().toString().trim();
        String description = getDescription.getText().toString().trim();
        String category = getCategory.getText().toString().trim();
        String cookTimeStr = getCookTime.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(description) ||
                TextUtils.isEmpty(category) || TextUtils.isEmpty(cookTimeStr)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int cookTime = Integer.parseInt(cookTimeStr);

        // Convert text values into RequestBody
        RequestBody nameBody = RequestBody.create(MediaType.parse("text/plain"), name);
        RequestBody descriptionBody = RequestBody.create(MediaType.parse("text/plain"), description);
        RequestBody categoryBody = RequestBody.create(MediaType.parse("text/plain"), category);
        RequestBody cookTimeBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(cookTime));

        MultipartBody.Part imagePart = null;
        if (imageUri != null) {
            try {
                File imageFile = copyUriToFile(imageUri);
                RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
                imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), imageBody);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Image error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Call API
        RecipeApiService api = RetrofitClient.getClient().create(RecipeApiService.class);
        api.patchRecipeMultipart(recipeId, nameBody, descriptionBody, categoryBody, cookTimeBody, imagePart)
                .enqueue(new Callback<Recipe>() {
                    @Override
                    public void onResponse(Call<Recipe> call, Response<Recipe> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Recipe updated!", Toast.LENGTH_SHORT).show();
                            NavHostFragment.findNavController(EditRecipeFragment.this).popBackStack();
                        } else {
                            Toast.makeText(getContext(), "Failed to update recipe", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Recipe> call, Throwable t) {
                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



    /*
    private void saveRecipe() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String cookTimeStr = etCookTime.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(description) ||
                TextUtils.isEmpty(category) || TextUtils.isEmpty(cookTimeStr)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int cookTime = Integer.parseInt(cookTimeStr);

        JSONObject json = new JSONObject();
        try {
            json.put("id", recipeId);
            json.put("name", name);
            json.put("description", description);
            json.put("category", category);
            json.put("cookTime", cookTime);

            if (imageUri != null) {
                File copied = copyUriToFile(imageUri);
                json.put("imagePath", copied.getAbsolutePath());
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "JSON error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(mediaType, json.toString());

        RecipeApiService api = RetrofitClient.getClient().create(RecipeApiService.class);
        api.patchRecipeMultipart(recipeId, requestBody).enqueue(new Callback<Recipe>() {
            @Override
            public void onResponse(Call<Recipe> call, Response<Recipe> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Recipe updated!", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(EditRecipeFragment.this).popBackStack();
                } else {
                    Toast.makeText(getContext(), "Failed to update recipe", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Recipe> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

     */
}
