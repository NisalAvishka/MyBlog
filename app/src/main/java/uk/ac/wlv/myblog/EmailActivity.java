package uk.ac.wlv.myblog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class EmailActivity extends AppCompatActivity {

    private TextView subject, message;
    private ImageView image_view;
    private EditText email;
    private int postId, userId;
    private DatabaseHelper databaseHelper;
    private Button buttonSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);

        databaseHelper = new DatabaseHelper(this);

        Intent intent = getIntent();
        postId = intent.getIntExtra("postId", -1);
        userId = intent.getIntExtra("userId", -1);

        Blog blog = databaseHelper.getBlogDataByPostId(postId);

        if (blog != null) {
            String title = blog.getTitle();
            String description = blog.getDescription();
            Bitmap image = blog.getImage();

            subject = findViewById(R.id.subject);
            image_view = findViewById(R.id.email_image);
            message = findViewById(R.id.email_description);

            subject.setText(title);
            message.setText(description);
            image_view.setImageBitmap(image);
        }

        email = findViewById(R.id.emailAddress);
        buttonSend = findViewById(R.id.send);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInternetAvailable()) {
                    String recipientList = email.getText().toString();
                    String[] recipients = recipientList.split(",");
                    String emailSubject = subject.getText().toString();
                    String emailBody = subject.getText().toString();

                    Toast.makeText(EmailActivity.this, "Preparing to send email...", Toast.LENGTH_SHORT).show();

                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                                    emailIntent.putExtra(Intent.EXTRA_EMAIL, recipients);
                                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
                                    emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);
                                    emailIntent.setType("message/rfc822");

                                    // Attach the image
                                    Blog blog = databaseHelper.getBlogDataByPostId(postId);
                                    if (blog != null) {
                                        Bitmap image = blog.getImage();
                                        if (image != null) {
                                            try {
                                                // Create a temporary file to store the image
                                                File cachePath = new File(getCacheDir(), "images");
                                                cachePath.mkdirs(); // don't forget to make the directory
                                                FileOutputStream stream = new FileOutputStream(cachePath + "/image.png");
                                                image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                                stream.close();

                                                // Attach the image from the temporary file
                                                File imagePath = new File(cachePath, "image.png");
                                                Uri imageUri = FileProvider.getUriForFile(
                                                        EmailActivity.this,
                                                        "uk.ac.wlv.provider",
                                                        imagePath
                                                );
                                                emailIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                                                emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                    try {
                                        startActivity(Intent.createChooser(emailIntent, "Choose an email client..."));
                                        finish();
                                    } catch (Exception e) {
                                        Toast.makeText(EmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            },
                            50
                    );
                } else {
                    // Internet connection is not available, show a toast message
                    Toast.makeText(EmailActivity.this, "No internet connection. Please connect to the internet.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
}