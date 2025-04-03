package com.example.hugbunadarverkefni.ui;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
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
import org.json.JSONObject;
import java.io.*;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditRecipeFragment extends Fragment {

    private EditText etName, etDescription, etCategory, etCookTime;
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

        etName = view.findViewById(R.id.etRecipeName);
        etDescription = view.findViewById(R.id.etRecipeDescription);
        etCategory = view.findViewById(R.id.etRecipeCategory);
        etCookTime = view.findViewById(R.id.etRecipeCookTime);
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
                    etName.setText(loadedRecipe.getName());
                    etDescription.setText(loadedRecipe.getDescription());
                    etCategory.setText(loadedRecipe.getCategory());
                    etCookTime.setText(String.valueOf(loadedRecipe.getCookTime()));
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
        api.patchRecipe(recipeId, requestBody).enqueue(new Callback<Recipe>() {
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
}
