package com.example.user.RateEat.Model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
public class User{
    @PrimaryKey
    @NonNull
    public String id;

    public String displayName;
    public String imageURL;

    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public User() {}

    @Ignore
    public User(@NonNull String id, String displayName, String imageURL) {
        this.id = id;
        this.displayName = displayName;
        this.imageURL = imageURL;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("displayName", displayName);
        result.put("imageURL", imageURL);

        return result;
    }

    @Override
    public String toString() {
        return String.format("User: %s, %s", id, displayName);
    }

    @Dao
    public interface UserDAO {
        @Query("SELECT * FROM User")
        List<User> getAll();

        @Query("SELECT * FROM User WHERE id = :id")
        User getById(String id);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertAll(User... users);

        @Delete
        void delete(User user);
    }
}
