package uk.ac.wlv.myblog;

import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class EditPostsActivity extends AppCompatActivity {
    private EditText edit_title, edit_description;
    private ImageView edit_image;
    private Button edit_button;
    private int postId, userId;
    private DatabaseHelper databaseHelper;
    private static final int PICK_IMAGE_REQUEST = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_CAMERA_PERMISSION = 102;
    private static final int REQUEST_STORAGE_PERMISSION = 103;
    private Uri imageFilePath;
    private Bitmap imageToStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_posts);

        databaseHelper = new DatabaseHelper(this);

        Intent intent = getIntent();
        postId = intent.getIntExtra("postId", -1);
        userId = intent.getIntExtra("userId", -1);

        Blog blog = databaseHelper.getBlogDataByPostId(postId);
        if (blog != null) {
            String title = blog.getTitle();
            String description = blog.getDescription();
            Bitmap image = blog.getImage();

            edit_title = findViewById(R.id.edit_title);
            edit_image = findViewById(R.id.edit_image);
            edit_description = findViewById(R.id.edit_description);
            edit_button = findViewById(R.id.edit_blog);

            edit_title.setText(title);
            edit_description.setText(description);
            edit_image.setImageBitmap(image);

            edit_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkAndRequestPermissions();
                }
            });

            edit_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updatePost(v);
                }
            });
        }

    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
        } else {
            // Permissions already granted, show image source dialog
            showImageSourceDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION || requestCode == REQUEST_STORAGE_PERMISSION) {
            // Check if all permissions are granted
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                // Permissions granted, show image source dialog
                showImageSourceDialog();
            } else {
                // Permissions not granted, show a message or handle accordingly
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source");

        final String[] options = {"Gallery", "Camera"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    // Gallery option
                    chooseImageFromGallery();
                } else if (which == 1) {
                    // Camera option
                    captureImageFromCamera();
                }
            }
        });

        builder.show();
    }

    private void chooseImageFromGallery() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void captureImageFromCamera() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
                // Handle image selected from the gallery
                imageFilePath = data.getData();
                imageToStore = MediaStore.Images.Media.getBitmap(getContentResolver(), imageFilePath);
                edit_image.setImageBitmap(imageToStore);
            } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
                // Handle image captured from the camera
                Bundle extras = data.getExtras();
                if (extras != null) {
                    imageToStore = (Bitmap) extras.get("data");
                    edit_image.setImageBitmap(imageToStore);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void updatePost(View view) {
        try {
            String updatedTitle = edit_title.getText().toString().trim();
            String updatedDescription = edit_description.getText().toString().trim();

            // Ensure that at least the title is not empty before attempting to update
            if (!updatedTitle.isEmpty()) {
                DatabaseHelper dbHelper = new DatabaseHelper(this);

                // Create a new Blog object with the updated data
                Blog updatedBlog = new Blog(userId, updatedTitle, imageToStore, updatedDescription);

                // Call the updatePost method with the postId and updatedBlog
                dbHelper.updatePost(postId, updatedBlog);

                // Finish the activity or navigate to another screen as needed
                Intent intent = new Intent(EditPostsActivity.this, MainActivity.class);
                startActivity(intent);
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}