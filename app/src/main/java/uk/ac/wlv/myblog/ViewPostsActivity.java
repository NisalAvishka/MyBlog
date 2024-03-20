package uk.ac.wlv.myblog;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

public class ViewPostsActivity extends AppCompatActivity {

    private EditText view_title, view_description;
    private ImageView view_image;
    private int postId, userId;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_posts);

        databaseHelper = new DatabaseHelper(this);

        Intent intent = getIntent();
        postId = intent.getIntExtra("postId", -1);
        userId = intent.getIntExtra("userId", -1);

        Blog blog = databaseHelper.getBlogDataByPostId(postId);
        if (blog != null) {
            String title = blog.getTitle();
            String description = blog.getDescription();
            Bitmap image = blog.getImage();

            view_title = findViewById(R.id.view_title);
            view_image = findViewById(R.id.view_image);
            view_description = findViewById(R.id.view_description);

            view_title.setText(title);
            view_description.setText(description);
            view_image.setImageBitmap(image);
        }
    }
}