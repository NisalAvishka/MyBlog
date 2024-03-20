package uk.ac.wlv.myblog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    Context context;
    public static final int VERSION = 2;
    private static final String DB_NAME = "blog";
    private ByteArrayOutputStream objectByteArrayOutputStream;
    private byte[] imageInBytes;
    public DatabaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTableQuery = "CREATE TABLE users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "email TEXT UNIQUE NOT NULL, "
                + "password TEXT NOT NULL, "
                + "name TEXT"
                + ")";
        sqLiteDatabase.execSQL(createTableQuery);

        String createPostTableQuery = "CREATE TABLE posts ("
                + "postId INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "title TEXT NOT NULL, "
                + "image BLOB, "
                + "description TEXT,"
                + "userId INTEGER NOT NULL,"
                + "FOREIGN KEY(userId) REFERENCES users(id)"
                + ")";
        sqLiteDatabase.execSQL(createPostTableQuery);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop Table if exists users");
        sqLiteDatabase.execSQL("drop Table if exists posts");
    }

    public Boolean insertData(String email, String password, String name){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("email", email);
        contentValues.put("password", password);
        contentValues.put("name", name);
        long result = sqLiteDatabase.insert("users", null, contentValues);
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public Boolean checkEmail(String email){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("Select * from users where email = ?", new String[]{email});
        if(cursor.getCount() > 0) {
            return true;
        }else {
            return false;
        }
    }

    public Boolean checkEmailPassword(String email, String password){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("Select * from users where email = ? and password = ?", new String[]{email, password});
        if (cursor.getCount() > 0) {
            return true;
        }else {
            return false;
        }
    }

    public int getUserId(String email, String password) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String[] columns = {"id"};
        String selection = "email=? AND password=?";
        String[] selectionArgs = {email, password};
        Cursor cursor = sqLiteDatabase.query("users", columns, selection, selectionArgs, null, null, null);
        int userId = -1;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                // Log column names for debugging
                String[] columnNames = cursor.getColumnNames();
                for (String columnName : columnNames) {
                    Log.d("DatabaseHelper", "Column Name: " + columnName);
                }

                // Try to retrieve user ID
                int columnIndex = cursor.getColumnIndex("id");
                if (columnIndex != -1) {
                    userId = cursor.getInt(columnIndex);
                } else {
                    Log.e("DatabaseHelper", "Column 'id' not found in the result set");
                }
            }

            cursor.close();
        } else {
            Log.e("DatabaseHelper", "Cursor is null");
        }

        return userId;
    }

    public void storePosts(Blog objectBlog) {
        try {
            SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();

            contentValues.put("title", objectBlog.getTitle());
            contentValues.put("description", objectBlog.getDescription());
            contentValues.put("userId", objectBlog.getUserId());

            Bitmap imageToStoreBitmap = objectBlog.getImage();

            if (imageToStoreBitmap != null) {
                // Compress the image to reduce its size
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                imageToStoreBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream); // Adjust quality as needed

                // Check if the compressed size is acceptable (e.g., less than 1MB)
                if (outputStream.size() > 1024 * 1024) {
                    // If the compressed size is still large, you may further reduce the quality or use other compression techniques
                    Toast.makeText(context, "Image size is still large after compression", Toast.LENGTH_SHORT).show();
                    return;
                }

                byte[] compressedImageInBytes = outputStream.toByteArray();
                contentValues.put("image", compressedImageInBytes);
            }

            long result = sqLiteDatabase.insert("posts", null, contentValues);
            if (result != -1) {
                Toast.makeText(context, "Post added Successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to add the Post", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    public ArrayList<Blog> getAllBlogData() {
        try {
            SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
            ArrayList<Blog> blogArrayList = new ArrayList<>();

            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM posts ORDER BY postId DESC", null);
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    int postId = cursor.getInt(0);

                    String blogTitle = cursor.getString(1);

                    // Check for null values before decoding the byte array
                    byte[] imageBytes = cursor.getBlob(2);
                    Bitmap bitmap = (imageBytes != null) ? BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length) : null;

                    String blogDescription = cursor.getString(3);

                    Integer userId = cursor.getInt(4);

                    Blog blog = new Blog(userId, blogTitle, bitmap, blogDescription);
                    // Set the postId using the setter
                    blog.setPostId(postId);

                    blogArrayList.add(blog);
                }
                return blogArrayList;
            } else {
                Toast.makeText(context, "No Posts Available", Toast.LENGTH_SHORT).show();
                return null;
            }
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public Blog getBlogDataByPostId(int postId) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Blog blog = null;

        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM posts WHERE postId = ?", new String[]{String.valueOf(postId)});
        if (cursor.moveToFirst()) {
            int retrievedPostId = cursor.getInt(0);
            String title = cursor.getString(1);
            byte[] imageBytes = cursor.getBlob(2);
            Bitmap image = (imageBytes != null) ? BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length) : null;
            String description = cursor.getString(3);
            int userId = cursor.getInt(4);

            blog = new Blog(userId, title, image, description);
            blog.setPostId(retrievedPostId);
        }

        cursor.close();
        return blog;
    }

    public void updatePost(int postId, Blog updatedBlog) {
        try {
            SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();

            contentValues.put("title", updatedBlog.getTitle());
            contentValues.put("description", updatedBlog.getDescription());

            Bitmap updatedImageBitmap = updatedBlog.getImage();

            if (updatedImageBitmap != null) {
                // Compress the image to reduce its size
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                updatedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream); // Adjust quality as needed

                // Check if the compressed size is acceptable (e.g., less than 1MB)
                if (outputStream.size() > 1024 * 1024) {
                    // If the compressed size is still large, you may further reduce the quality or use other compression techniques
                    Toast.makeText(context, "Image size is still large after compression", Toast.LENGTH_SHORT).show();
                    return;
                }

                byte[] compressedImageInBytes = outputStream.toByteArray();
                contentValues.put("image", compressedImageInBytes);
            }

            // Assuming 'postId' is the column name for the post ID in your database
            String whereClause = "postId=?";
            String[] whereArgs = {String.valueOf(postId)};

            int result = sqLiteDatabase.update("posts", contentValues, whereClause, whereArgs);

            if (result > 0) {
                Toast.makeText(context, "Post updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to update the post", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public boolean deletePost(int postId) {
        try {
            SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
            String whereClause = "postId=?";
            String[] whereArgs = {String.valueOf(postId)};
            int result = sqLiteDatabase.delete("posts", whereClause, whereArgs);
            return result > 0;
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public boolean deleteSelectedPosts(ArrayList<Integer> postIds) {
        try {
            SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
            StringBuilder whereClauseBuilder = new StringBuilder("postId IN (");

            for (int i = 0; i < postIds.size(); i++) {
                whereClauseBuilder.append(postIds.get(i));
                if (i < postIds.size() - 1) {
                    whereClauseBuilder.append(", ");
                }
            }

            whereClauseBuilder.append(")");

            String whereClause = whereClauseBuilder.toString();

            int result = sqLiteDatabase.delete("posts", whereClause, null);
            return result > 0;
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public ArrayList<Blog> getFilteredBlogData(String searchText) {
        try {
            SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
            ArrayList<Blog> blogArrayList = new ArrayList<>();

            // Use SQL query to retrieve filtered posts based on title or description
            String query = "SELECT * FROM posts WHERE title LIKE ? OR description LIKE ?";
            String[] selectionArgs = {"%" + searchText + "%", "%" + searchText + "%"};
            Cursor cursor = sqLiteDatabase.rawQuery(query, selectionArgs);

            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    int postId = cursor.getInt(0);
                    String blogTitle = cursor.getString(1);
                    byte[] imageBytes = cursor.getBlob(2);
                    Bitmap bitmap = (imageBytes != null) ? BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length) : null;
                    String blogDescription = cursor.getString(3);
                    int userId = cursor.getInt(4);

                    Blog blog = new Blog(userId, blogTitle, bitmap, blogDescription);
                    blog.setPostId(postId);

                    blogArrayList.add(blog);
                }
                return blogArrayList;
            } else {
                return null; // No matching posts found
            }
        } catch (Exception e) {
            // Existing error handling...
            return null;
        }
    }




}
