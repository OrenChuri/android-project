package com.example.user.RateEat.Model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
public class Taste {
    @PrimaryKey
    @NonNull
    public String id;

    @NonNull
    public String restId;

    public String title;
    public String author;
    public String authorId;
    public String description;
    public String imageURL;
    public String date;
    public float starCount = 0;
    public long lastUpdated;
    public boolean isDeleted;

    // Default constructor required for calls to DataSnapshot.getValue(Taste.class)
    public Taste() {}

    @Ignore
    public Taste(@NonNull String id,@NonNull String restId, String title,
                 String authorId, String author,
                 String description, float stars, String imageURL, String date) {
        this.id = id;
        this.restId = restId;
        this.title = title;
        this.authorId = authorId;
        this.author = author;
        this.description = description;
        this.imageURL = imageURL;
        this.starCount = stars;
        this.date = date;
        this.lastUpdated = 0;
        isDeleted = false;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("restId", restId);
        result.put("title", title);
        result.put("author", author);
        result.put("authorId", authorId);
        result.put("description", description);
        result.put("imageURL", imageURL);
        result.put("starCount", starCount);
        result.put("date", date);
        result.put("isDeleted", isDeleted);

        return result;
    }

    @Override
    public String toString() {
        return String.format("Taste: %s, %s, %s", id, author, title);
    }

        @Dao
    public interface TasteDAO {
        @Query("SELECT * FROM Taste")/* WHERE isDeleted = 0")*/
//        @Query("SELECT * FROM Taste WHERE isDeleted = 0")
        List<Taste> getAll();

        @Query("SELECT * FROM Taste WHERE id = :id")/* and isDeleted = 0")*/
//        @Query("SELECT * FROM Taste WHERE id = :id and isDeleted = 0")
        Taste getById(String id);

        @Query("SELECT * FROM Taste WHERE restId = :restId")/*and isDeleted = 0")*/
//        @Query("SELECT * FROM Taste WHERE restId = :restId and isDeleted = 0")
        List<Taste> getByRestId(String restId);

        @Query("SELECT * FROM Taste WHERE authorId = :userId")/*and isDeleted = 0")*/
//        @Query("SELECT * FROM Taste WHERE authorId = :userId and isDeleted = 0")
        List<Taste> getByUserId(String userId);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertAll(Taste... tastes);

        @Delete
        void delete(Taste taste);

        @Update
        void update(Taste taste);
    }
}
