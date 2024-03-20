package uk.ac.wlv.myblog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class BlogAdapter extends RecyclerView.Adapter <BlogAdapter.BlogHolderClass> {
    ArrayList <Blog> blogArrayList;
    private ArrayList<Blog> filteredBlogList;
    private SparseBooleanArray selectedItems;
    private ArrayList<Integer> selectedPostIds;


    public BlogAdapter(ArrayList<Blog> blogArrayList) {
        this.blogArrayList = blogArrayList;
        this.selectedItems = new SparseBooleanArray();
        this.selectedPostIds = new ArrayList<>();
    }

    public void updateData(ArrayList<Blog> newData) {
        blogArrayList.clear();
        blogArrayList.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BlogHolderClass onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BlogHolderClass(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.blog_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BlogHolderClass holder, int position) {
        Blog blog = blogArrayList.get(position);
        if (blog != null) {
            holder.blogTitle.setText(blog.getTitle());
            holder.blogImage.setImageBitmap(blog.getImage());
            holder.blogDescription.setText(blog.getDescription());
            holder.selectionIndicator.setVisibility(isSelected(position) ? View.VISIBLE : View.GONE);

            // Set long click listener for the blog post
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    toggleSelection(holder.getAdapterPosition());
                    return true;
                }
            });

            // Set click listener for the blog post
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ViewPostsActivity.class);
                    intent.putExtra("postId", blog.getPostId());
                    intent.putExtra("userId", blog.getUserId());
                    v.getContext().startActivity(intent);
                }
            });

            // Set click listener for the update icon
            holder.iconUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), EditPostsActivity.class);
                    intent.putExtra("postId", blog.getPostId());
                    intent.putExtra("userId", blog.getUserId());
                    v.getContext().startActivity(intent);
                }
            });

            // Set click listener for the delete icon
            holder.iconDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteConfirmationDialog(v.getContext(), blog.getPostId());
                }
            });

            // Set click listener for the email icon
            holder.iconEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), EmailActivity.class);
                    intent.putExtra("postId", blog.getPostId());
                    intent.putExtra("userId", blog.getUserId());
                    v.getContext().startActivity(intent);
                }
            });

            // Set click listener for the social media icon
            holder.iconSocial.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String title = blogArrayList.get(holder.getAdapterPosition()).getTitle();
                    String description = blogArrayList.get(holder.getAdapterPosition()).getDescription();
                    Bitmap image = blogArrayList.get(holder.getAdapterPosition()).getImage();

                    // Share the blog details using an Intent
                    shareToTwitter(v.getContext(), title, description, image);
                }
            });

        }
    }

    public static class BlogHolderClass extends RecyclerView.ViewHolder{
        TextView blogTitle, blogDescription;
        ImageView blogImage, iconUpdate, iconDelete, iconEmail, iconSocial, selectionIndicator;

        public BlogHolderClass(@NonNull View itemView) {
            super(itemView);
            blogTitle = itemView.findViewById(R.id.blogTitle);
            blogImage = itemView.findViewById(R.id.blogImageView);
            blogDescription = itemView.findViewById(R.id.blogDescription);
            iconUpdate = itemView.findViewById(R.id.iconUpdate);
            iconDelete = itemView.findViewById(R.id.iconDelete);
            iconEmail = itemView.findViewById(R.id.iconEmail);
            selectionIndicator = itemView.findViewById(R.id.selectionIndicator);
            iconSocial = itemView.findViewById(R.id.iconSocialMedia);
        }
    }

    private void showDeleteConfirmationDialog(Context context, int postId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Post");
        builder.setMessage("Do you want to delete this post?");

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Call the method in MainActivity to delete the post
                if (context instanceof MainActivity) {
                    ((MainActivity) context).deletePost(postId);
                }
            }
        });


        builder.create().show();
    }

    public void toggleSelection(int position) {
        boolean isSelected = !selectedItems.get(position, false);
        selectedItems.put(position, isSelected);
        notifyItemChanged(position);

        // Update the selectedPostIds list
        int postId = blogArrayList.get(position).getPostId();
        if (isSelected) {
            selectedPostIds.add(postId);
        } else {
            selectedPostIds.remove(Integer.valueOf(postId));
        }

        notifyItemChanged(position);
    }

    public void clearSelection() {
        selectedItems.clear();
        selectedPostIds.clear(); // Clear the selected post IDs
        notifyDataSetChanged();
    }

    private boolean isSelected(int position) {
        return selectedItems.get(position, false);
    }

    public ArrayList<Integer> getSelectedPostIds() {
        return selectedPostIds;
    }



    @Override
    public int getItemCount() {
        return blogArrayList.size();
    }

    private void shareToTwitter(Context context, String title, String description, Bitmap image) {
        // Check for internet connectivity
        if (isNetworkAvailable(context)) {
            // Internet is available, proceed to share on Twitter

            // Create an intent with ACTION_SEND
            Intent tweetIntent = new Intent(Intent.ACTION_SEND);
            // Set the type of content to share
            tweetIntent.setType("text/plain");

            // Add the blog details to the intent
            String tweetContent = title + "\n" + description + "\n";
            tweetIntent.putExtra(Intent.EXTRA_TEXT, tweetContent);

            // Check if the image is not null before adding it to the intent
            if (image != null) {
                try {
                    // Create a temporary file to store the image
                    File cachePath = new File(context.getCacheDir(), "images");
                    cachePath.mkdirs(); // don't forget to make the directory
                    File imagePath = new File(cachePath, "image.png");
                    FileOutputStream stream = new FileOutputStream(imagePath);
                    image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    stream.close();

                    // Attach the image from the temporary file using FileProvider
                    Uri imageUri = FileProvider.getUriForFile(
                            context,
                            "uk.ac.wlv.provider",
                            imagePath
                    );
                    tweetIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    tweetIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Set the package name of the Twitter app
            tweetIntent.setPackage("com.twitter.android");

            // Start the Twitter app directly
            context.startActivity(tweetIntent);
        } else {
            // Internet is not available, show a toast message
            Toast.makeText(context, "Please connect to the internet", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
}
