package com.example.traveling;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DataBaseHelper extends SQLiteOpenHelper {

    public DataBaseHelper(Context context) {
        super(context, "posts.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE posts (" +
                        "postId INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "firestore_id TEXT," +       // links to Firestore document
                        "image_uri TEXT," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS posts");
        onCreate(db);
    }

    public void insertPost(String firestoreId, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("firestore_id", firestoreId);
        values.put("image_uri", imageUri);
        db.insert("posts", null, values);
        db.close();
    }

    public String getImageUri(String firestoreId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                "posts",
                new String[]{"image_uri"},
                "firestore_id = ?",
                new String[]{firestoreId},
                null, null, null
        );
        String uri = null;
        if (cursor.moveToFirst()) {
            uri = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return uri;
    }

    public void deletePost(String firestoreId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("posts", "firestore_id = ?", new String[]{firestoreId});
        db.close();
    }
}