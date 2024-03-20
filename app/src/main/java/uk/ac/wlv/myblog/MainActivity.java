package uk.ac.wlv.myblog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private RecyclerView recyclerView;
    private BlogAdapter blogAdapter;
    int userId;
    private EditText textSearch;
    private ImageView searchButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try{
            recyclerView = findViewById(R.id.recyclerview);
            databaseHelper = new DatabaseHelper(this);

        }catch (Exception e){
            Toast.makeText(this, "No blog posts available.", Toast.LENGTH_SHORT).show();
        }
        getData();


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textSearch = findViewById(R.id.editTextSearch);
        searchButton = findViewById(R.id.iconSearch);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchText = textSearch.getText().toString();
                performSearch(searchText);
            }
        });

    }

    @Override
    public void onBackPressed() {
        // If the user is currently in the search mode, clear the search query and update the data
        if (!textSearch.getText().toString().isEmpty()) {
            textSearch.setText("");
            String search = textSearch.getText().toString();
            performSearch(search);// Show all posts
        } else {
            super.onBackPressed(); // Navigate back as usual
        }
    }


    public void getData() {
        try {
            // Get the blog data from the database
            ArrayList<Blog> blogData = databaseHelper.getAllBlogData();

            if (blogData.isEmpty()) {
                // If the blogData list is empty, show a message or placeholder
                // You can customize this part based on your UI design
                Toast.makeText(this, "No blog posts available.", Toast.LENGTH_SHORT).show();
            } else {
                // If the blogData list is not empty, set up the RecyclerView
                blogAdapter = new BlogAdapter(blogData);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setAdapter(blogAdapter);
            }
        } catch (Exception e) {
            Toast.makeText(this, "No blog posts available.", Toast.LENGTH_SHORT).show();
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        userId = getIntent().getIntExtra("userId", -1);
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add_blog) {
            Intent intent = new Intent(MainActivity.this, AddPostActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        }
        if (id == R.id.logout) {
            Toast.makeText(MainActivity.this,
                    "Logout Successfully!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        if (id == R.id.delete_blog){
            ArrayList<Integer> selectedPostIds = blogAdapter.getSelectedPostIds();
            deleteSelectedPosts(selectedPostIds);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Refresh the RecyclerView in MainActivity
            getData();
        }
    }

    public void deletePost(int postId) {
        try {
            // Call the method in DatabaseHelper to delete the post
            boolean result = databaseHelper.deletePost(postId);

            if (result) {
                // Refresh the RecyclerView
                getData();
                Toast.makeText(this, "Post deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to delete the post", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteSelectedPosts(ArrayList<Integer> selectedPostIds) {
        try {
            // Show a confirmation dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirm Delete");
            builder.setMessage("Do you want to delete the selected posts?");

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Call the method in DatabaseHelper to delete the selected posts
                    boolean result = databaseHelper.deleteSelectedPosts(selectedPostIds);

                    if (result) {
                        // Refresh the RecyclerView
                        getData();
                        Toast.makeText(MainActivity.this, "Selected posts deleted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to delete the selected posts", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.create().show();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void performSearch(String searchText) {
        ArrayList<Blog> searchResults = databaseHelper.getFilteredBlogData(searchText);

        if (searchResults != null) {
            blogAdapter.updateData(searchResults);
        } else {
            // Handle the case where no matching posts are found
            Toast.makeText(MainActivity.this, "No matching posts found", Toast.LENGTH_SHORT).show();
        }
    }
}
