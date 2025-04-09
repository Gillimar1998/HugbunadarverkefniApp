package com.example.hugbunadarverkefni.ui;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import android.widget.EditText;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import static android.app.Activity.RESULT_OK;



import com.bumptech.glide.Glide;

import androidx.navigation.fragment.NavHostFragment;
import com.example.hugbunadarverkefni.R;
import com.example.hugbunadarverkefni.api.RecipeApiService;
import com.example.hugbunadarverkefni.api.RetrofitClient;
import com.example.hugbunadarverkefni.api.UserApiService;
import com.example.hugbunadarverkefni.model.Comment;
import com.example.hugbunadarverkefni.model.Recipe;
import com.example.hugbunadarverkefni.model.User;

import org.json.JSONObject;

import java.io.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeViewFragment extends Fragment {

    private TextView titleTextView, categoryTextView, cookTimeTextView, descriptionTextView, likesTextView;
    private ImageView recipeImageView, commentImagePreview;
    private LinearLayout commentsContainer;
    private SharedPreferences sharedPreferences;
    private UserApiService userApiService;
    private ImageButton likeButton;
    private long recipeId, userId;
    private int likeCount = 0;
    private Button btnDeleteRecipe, btnEditRecipe;

    private ImageButton commentSubmit, btnSelectCommentImage;
    private EditText commentInput;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri imageUri;
    private ActivityResultLauncher<Intent> editImageLauncher;
    private Uri selectedEditImageUri;
    private ImageView editImagePreviewRef;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_view, container, false);

        // Initialize UI elements
        titleTextView = view.findViewById(R.id.recipeTitle);
        categoryTextView = view.findViewById(R.id.recipeCategory);
        cookTimeTextView = view.findViewById(R.id.recipeCookTime);
        descriptionTextView = view.findViewById(R.id.recipeDescription);
        likesTextView = view.findViewById(R.id.recipeLikes);
        recipeImageView = view.findViewById(R.id.ivRecipeImage); // ImageView for recipe image
        commentsContainer = view.findViewById(R.id.commentsContainer);
        likeButton = view.findViewById(R.id.btnLike);
        btnDeleteRecipe = view.findViewById(R.id.btnDeleteRecipe);
        btnEditRecipe = view.findViewById(R.id.btnEditRecipe);
        commentInput = view.findViewById(R.id.commentInput);
        commentSubmit = view.findViewById(R.id.commentSubmit); // ✅
        btnSelectCommentImage = view.findViewById(R.id.btnSelectCommentImage);
        commentImagePreview = view.findViewById(R.id.commentImagePreview);

        btnEditRecipe.setVisibility(View.GONE); // default hide the edit button



        // Get recipe ID from arguments
        if (getArguments() != null) {
            recipeId = getArguments().getLong("recipeId", -1);
            if (recipeId != -1) {
                fetchRecipeDetails(recipeId);
            } else {
                Toast.makeText(getContext(), "Invalid Recipe ID", Toast.LENGTH_SHORT).show();
            }
        }

        commentSubmit.setOnClickListener(v -> submitComment());

        btnSelectCommentImage.setOnClickListener(v -> openImageChooser());

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImage = null;

                        if (result.getData().getData() != null) {
                            // Image from gallery
                            selectedImage = result.getData().getData();
                            requireContext().getContentResolver().takePersistableUriPermission(
                                    selectedImage,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                        } else if (result.getData().getExtras() != null) {
                            // Image from camera (thumbnail)
                            Bitmap photo = (Bitmap) result.getData().getExtras().get("data");

                            // Save it to a temp file and get a URI
                            selectedImage = saveBitmapToUri(photo);
                        }

                        if (selectedImage != null) {
                            imageUri = selectedImage;
                            commentImagePreview.setVisibility(View.VISIBLE);
                            commentImagePreview.setImageURI(imageUri);
                        }
                    } else {
                        imageUri = null;
                        commentImagePreview.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        editImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        if (selectedImage != null) {
                            requireContext().getContentResolver().takePersistableUriPermission(
                                    selectedImage,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                            selectedEditImageUri = selectedImage;
                            if (editImagePreviewRef != null) {
                                editImagePreviewRef.setImageURI(selectedImage);
                            }
                        }
                    }
                }
        );




        // Like button click event
        likeButton.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
            long userId = sharedPreferences.getLong("user_Id", -1);

            if (userId == -1) {
                Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            Set<String> favorites = sharedPreferences.getStringSet("favorites", new HashSet<>());

            RecipeApiService apiService = RetrofitClient.getClient().create(RecipeApiService.class);
            Call<Map<String, Object>> call = apiService.likeRecipe(recipeId, userId);

            call.enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Map<String, Object> responseBody = response.body();

                        // Get the updated like count
                        int likeCount = ((Double) responseBody.get("likeCount")).intValue();

                        // Get the list of users who liked this post
                        List<Double> likedUserIds = (List<Double>) responseBody.get("likedUserIDs");
                        boolean isLiked = likedUserIds.contains((double) userId); // Check if the user is in the list

                        // Update UI
                        likesTextView.setText("Likes: " + likeCount);
                        if (isLiked) {
                            favorites.add(String.valueOf(recipeId));
                            editor.putStringSet("favorites", favorites);
                            editor.apply();
                            UserApiService userApiService = RetrofitClient.getClient().create(UserApiService.class);
                            Call<User> callObject = userApiService.addRecipeToFavorites(userId, recipeId);

                            callObject.enqueue(new Callback<User>(){
                                @Override
                                public void onResponse(Call<User> callobject, Response<User> responseUser) {
                                    User responseBodyUser = responseUser.body();
                                    Objects.requireNonNull(responseBodyUser).setFavorites(favorites);
                                }

                                @Override
                                public void onFailure(Call<User> callobject, Throwable t) {
                                    //Toast.makeText(getContext(), "Network error with user: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                            Toast.makeText(getContext(), "Liked and added to Favorites!", Toast.LENGTH_SHORT).show();
                            likeButton.setImageResource(R.drawable.ic_favorite_filled); // Change to "liked" image
                        } else {
                            favorites.remove(String.valueOf(recipeId));
                            editor.putStringSet("favorites", favorites);
                            editor.apply();
                            UserApiService userApiService = RetrofitClient.getClient().create(UserApiService.class);
                            Call<User> callObject = userApiService.removeRecipeFromFavorites(userId, recipeId);

                            callObject.enqueue(new Callback<User>(){
                                @Override
                                public void onResponse(Call<User> callobject, Response<User> responseUser) {
                                    User responseBodyUser = responseUser.body();
                                    Objects.requireNonNull(responseBodyUser).setFavorites(favorites);
                                }

                                @Override
                                public void onFailure(Call<User> callobject, Throwable t) {
                                    //Toast.makeText(getContext(), "Network error with user: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                            Toast.makeText(getContext(), "Unliked and removed from Favorites!", Toast.LENGTH_SHORT).show();
                            likeButton.setImageResource(R.drawable.ic_favorite_border); // Change to "unliked" image
                        }


                    } else {
                        Toast.makeText(getContext(), "Failed to update like", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });


        return view;
    }

    private void openImageChooser() {
        try {
            Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            galleryIntent.setType("image/*");
            galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            // Create a chooser intent
            Intent chooser = Intent.createChooser(galleryIntent, "Select or take a picture");

            // Add the camera intent as an option
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});

            imagePickerLauncher.launch(chooser);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Could not open image picker: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File copyUriToFile(Uri uri) throws IOException {
        File outputDir = requireContext().getCacheDir();
        File tempFile = new File(outputDir, "temp_comment_image.jpg");

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


    private void submitComment() {
        String content = commentInput.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(getContext(), "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        long userId = requireActivity().getSharedPreferences("UserPrefs", 0).getLong("user_Id", -1);
        if (userId == -1) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Wrap text values as RequestBody
        RequestBody recipeIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(recipeId));
        RequestBody userIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(userId));
        RequestBody contentBody = RequestBody.create(MediaType.parse("text/plain"), content);

        // Optional image part
        MultipartBody.Part imagePart = null;
        if (imageUri != null) {
            try {
                File file = copyUriToFile(imageUri);
                RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
                imagePart = MultipartBody.Part.createFormData("image", file.getName(), fileReqBody);
            } catch (IOException e) {
                Toast.makeText(getContext(), "Failed to process image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Make the API call
        RecipeApiService apiService = RetrofitClient.getClient().create(RecipeApiService.class);
        apiService.postCommentMultipart(recipeIdBody, userIdBody, contentBody, imagePart).enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Comment added!", Toast.LENGTH_SHORT).show();
                    commentInput.setText("");
                    commentImagePreview.setVisibility(View.GONE);
                    fetchRecipeDetails(recipeId); // ✅ refresh comments
                } else {
                    Toast.makeText(getContext(), "Failed to add comment", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Comment> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void fetchRecipeDetails(long recipeId) {
        RecipeApiService apiService = RetrofitClient.getClient().create(RecipeApiService.class);
        Call<Recipe> call = apiService.getRecipeById(recipeId);

        call.enqueue(new Callback<Recipe>() {
            @Override
            public void onResponse(Call<Recipe> call, Response<Recipe> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Recipe recipe = response.body();
                    displayRecipeDetails(recipe);
                } else {
                    Log.e("API Error", "Failed to fetch recipe details: " + response.errorBody());
                    Toast.makeText(getContext(), "Error fetching recipe", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Recipe> call, Throwable t) {
                Log.e("API Error", "Network Failure: " + t.getMessage());
                Toast.makeText(getContext(), "Network error, please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayRecipeDetails(Recipe recipe) {
        titleTextView.setText(recipe.getName());
        categoryTextView.setText("Category: " + recipe.getCategory());
        cookTimeTextView.setText("Duration: " + recipe.getCookTime() + " min");
        descriptionTextView.setText("Description: " + recipe.getDescription());
        likesTextView.setText("Likes: " + recipe.getLikeCount());

        // Handle ImageView visibility
        if (recipe.getImage() != null && recipe.getImage().getUrl() != null && !recipe.getImage().getUrl().isEmpty()) {
            recipeImageView.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(recipe.getImage().getUrl())
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .into(recipeImageView);
        } else {
            recipeImageView.setVisibility(View.GONE); // Hide ImageView if no image is available
        }

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getLong("user_Id", -1);
        boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);

        likesTextView.setText("Likes: " + recipe.getLikeCount());

        if (recipe.getUserId() == userId || isAdmin) {
            btnDeleteRecipe.setVisibility(View.VISIBLE);
            btnEditRecipe.setVisibility(View.VISIBLE);

            btnDeleteRecipe.setOnClickListener(v -> showDeleteConfirmation(recipeId));
            btnEditRecipe.setOnClickListener(v -> navigateToEditRecipe(recipeId));
        }

        // like button
        if (recipe.getLikedUserIDs().contains(userId)) {
            likeButton.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            likeButton.setImageResource(R.drawable.ic_favorite_border);
        }

        // Load comments dynamically
        commentsContainer.removeAllViews();
        if (recipe.getComments() != null && !recipe.getComments().isEmpty()) {
            for (Comment comment : recipe.getComments()) {
                LinearLayout commentLayout = new LinearLayout(getContext());
                commentLayout.setOrientation(LinearLayout.VERTICAL);
                commentLayout.setPadding(10, 5, 10, 5);

                LinearLayout commentRow = new LinearLayout(getContext());
                commentRow.setOrientation(LinearLayout.HORIZONTAL);
                commentRow.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                commentRow.setGravity(Gravity.CENTER_VERTICAL);

                // Comment Text
                TextView commentView = new TextView(getContext());
                commentView.setText(comment.getUser().getUsername() + ": " + comment.getContent());
                commentView.setTextSize(14f);
                commentView.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f // Push buttons to the right
                ));
                commentRow.addView(commentView);

                long currentUserId = sharedPreferences.getLong("user_Id", -1);

                if (comment.getUser().getId() == currentUserId || isAdmin) {
                    LinearLayout buttonLayout = new LinearLayout(getContext());
                    buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

                    if (comment.getUser().getId() == currentUserId) {
                        // Edit Button
                        ImageButton editButton = new ImageButton(getContext());
                        editButton.setImageResource(R.drawable.ic_edit);
                        editButton.setBackgroundColor(Color.TRANSPARENT);
                        editButton.setColorFilter(Color.parseColor("#2196F3"));
                        editButton.setPadding(10, 10, 10, 10);
                        editButton.setOnClickListener(v -> showEditCommentDialog(comment));
                        buttonLayout.addView(editButton);
                    }

                    // Delete Button
                    ImageButton deleteButton = new ImageButton(getContext());
                    deleteButton.setImageResource(R.drawable.ic_trash);
                    deleteButton.setBackgroundColor(Color.TRANSPARENT);
                    deleteButton.setColorFilter(Color.parseColor("#F44336"));
                    deleteButton.setPadding(10, 10, 10, 10);
                    deleteButton.setOnClickListener(v -> deleteComment(comment.getId()));
                    buttonLayout.addView(deleteButton);

                    commentRow.addView(buttonLayout);
                }

                commentLayout.addView(commentRow);

                // Optional Image (shown below the text + buttons)
                if (comment.getImage() != null && comment.getImage().getUrl() != null) {
                    ImageView commentImageView = new ImageView(getContext());
                    commentImageView.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
                    commentImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    Glide.with(this)
                            .load(comment.getImage().getUrl())
                            .placeholder(R.drawable.ic_placeholder_image)
                            .error(R.drawable.ic_placeholder_image)
                            .into(commentImageView);

                    commentLayout.addView(commentImageView);
                }

                commentsContainer.addView(commentLayout);
            }
        } else {
            TextView noComments = new TextView(getContext());
            noComments.setText("No comments yet.");
            noComments.setTextSize(14f);
            noComments.setPadding(10, 5, 10, 5);
            commentsContainer.addView(noComments);
        }
    }

    private void deleteComment(long commentId) {
        RecipeApiService apiService = RetrofitClient.getClient().create(RecipeApiService.class);
        long userId = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE).getLong("user_Id", -1);

        apiService.deleteComment(commentId, userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Comment deleted", Toast.LENGTH_SHORT).show();
                    fetchRecipeDetails(recipeId);
                } else {
                    Toast.makeText(getContext(), "Failed to delete comment", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private Uri saveBitmapToUri(Bitmap bitmap) {
        File imageFile = new File(requireContext().getCacheDir(), "camera_image.jpg");
        try (FileOutputStream out = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".fileprovider",
                imageFile
        );

    }

    private void showEditCommentDialog(Comment comment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Comment");

        // Create layout
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        // EditText for comment text
        final EditText input = new EditText(getContext());
        input.setText(comment.getContent());
        layout.addView(input);

        // ImageView for preview
        final ImageView imagePreview = new ImageView(getContext());
        imagePreview.setLayoutParams(new LinearLayout.LayoutParams(400, 400));
        imagePreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        layout.addView(imagePreview);

        // If existing image
        if (comment.getImage() != null && comment.getImage().getUrl() != null) {
            Glide.with(this).load(comment.getImage().getUrl()).into(imagePreview);
        }

        // Save image preview ref globally so the launcher can access it
        editImagePreviewRef = imagePreview;

        // Button to pick image
        Button selectImageButton = new Button(getContext());
        selectImageButton.setText("Change Image");
        layout.addView(selectImageButton);

        // Button click opens image picker
        selectImageButton.setOnClickListener(v -> {
            selectedEditImageUri = null; // Reset before opening picker
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            editImageLauncher.launch(intent);
        });

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String updatedText = input.getText().toString().trim();
            if (TextUtils.isEmpty(updatedText)) {
                Toast.makeText(getContext(), "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            MultipartBody.Part imagePart = null;
            if (selectedEditImageUri != null) {
                try {
                    File imageFile = copyUriToFile(selectedEditImageUri);
                    RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
                    imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), fileBody);
                } catch (IOException e) {
                    Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            updateComment(comment.getId(), updatedText, imagePart);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void updateComment(long commentId, String newText, MultipartBody.Part imagePart) {
        RecipeApiService apiService = RetrofitClient.getClient().create(RecipeApiService.class);

        RequestBody userIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(userId));
        RequestBody contentBody = RequestBody.create(MediaType.parse("text/plain"), newText);

        apiService.patchComment(commentId, userIdBody, contentBody, imagePart)
                .enqueue(new Callback<Comment>() {
                    @Override
                    public void onResponse(Call<Comment> call, Response<Comment> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Comment updated", Toast.LENGTH_SHORT).show();

                            // ✅ Clear image URI used during editing
                            // (Only if you're using something like a global editSelectedImageUri or temp image URI array)
                            imageUri = null; // For new comments
                            // If you're using selectedImageUri[0], you could do: selectedImageUri[0] = null;

                            fetchRecipeDetails(recipeId); // Refresh
                        } else {
                            Toast.makeText(getContext(), "Failed to update comment", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Comment> call, Throwable t) {
                        Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }





    private void navigateToEditRecipe(long recipeId) {
        Bundle bundle = new Bundle();
        bundle.putLong("recipeId", recipeId);
        NavHostFragment.findNavController(this).navigate(R.id.action_recipeViewFragment_to_EditRecipeFragment, bundle);
    }

    private void showDeleteConfirmation(long recipeId) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Recipe")
                .setMessage("Are you sure you want to delete this recipe?")
                .setPositiveButton("Yes", (dialog, which) -> deleteRecipe(recipeId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    //Deleting a recipe
    private void deleteRecipe(long recipeId) {
        RecipeApiService apiService = RetrofitClient.getClient().create(RecipeApiService.class);

        // Make the API call to delete the recipe
        Call<Void> call = apiService.deleteRecipe(recipeId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Handle success response, possibly showing the success message
                    Toast.makeText(getContext(), "Recipe deleted!", Toast.LENGTH_SHORT).show();

                    // Navigate to the RecipesViewFragment or back to the previous screen
                    NavHostFragment.findNavController(RecipeViewFragment.this)
                            .navigate(R.id.action_recipeViewFragment_to_recipesViewFragment);
                } else {
                    // Log the error body and show the error message
                    Toast.makeText(getContext(), "Failed to delete recipe", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Handle network failure
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}



