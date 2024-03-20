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

public class AddPostActivity extends AppCompatActivity {

    int userId;
    private EditText bTitle, bDescription;
    private ImageView bImage;
    private static final int PICK_IMAGE_REQUEST = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_CAMERA_PERMISSION = 102;
    private static final int REQUEST_STORAGE_PERMISSION = 103;
    private Uri imageFilePath;
    private Bitmap imageToStore;
    DatabaseHelper databaseHelper;
    private Button saveBtn, viewBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        try {
            userId = getIntent().getIntExtra("userId", -1);
            bTitle = findViewById(R.id.title_blog);
            bImage = findViewById(R.id.post_image_blog);
            bDescription = findViewById(R.id.description_blog);
            saveBtn = findViewById(R.id.upload_blog);
            viewBtn = findViewById(R.id.view_blog);
            databaseHelper = new DatabaseHelper(this);

            bImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkAndRequestPermissions();
                }
            });

            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    storePosts(v);
                }
            });

            viewBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AddPostActivity.this, MainActivity.class);
                    startActivity(intent);
                    setResult(RESULT_OK);
                    finish();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AddPostActivity.this, MainActivity.class);
        startActivity(intent);
        setResult(RESULT_OK);
        finish();
        super.onBackPressed();
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
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
                bImage.setImageBitmap(imageToStore);
            } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
                // Handle image captured from the camera
                Bundle extras = data.getExtras();
                if (extras != null) {
                    imageToStore = (Bitmap) extras.get("data");
                    bImage.setImageBitmap(imageToStore);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    public void storePosts(View view){
        try{
            if (!bTitle.getText().toString().isEmpty()) {
                databaseHelper.storePosts(new Blog(userId, bTitle.getText().toString(),
                        imageToStore, bDescription.getText().toString()));
                bTitle.setText("");
                bDescription.setText("");
                bImage.setImageResource(R.drawable.baseline_image_search_24);

            }else {
                Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();

            }

        }catch(Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
}