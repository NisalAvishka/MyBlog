package uk.ac.wlv.myblog;

import android.graphics.Bitmap;

public class Blog {
    private int postId;
    private int userId;
    private String title;
    private Bitmap image;
    private String description;
    private boolean isSelected;



    public Blog(int userId, String title) {
        this.userId = userId;
        this.title = title;
        this.isSelected = false;
    }

    // Constructor for posts with image and description
    public Blog(int userId, String title, Bitmap image, String description) {
        this.userId = userId;
        this.title = title;
        this.image = image;
        this.description = description;
        this.isSelected = false;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
