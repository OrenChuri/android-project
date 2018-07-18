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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

@Entity
public class Restaurant implements Serializable {
    @PrimaryKey
    @NonNull
    public String id;

    public String name;
    public String location;
    public String imageURL;
    public long lastUpdated;

    // Default constructor required for calls to DataSnapshot.getValue(Restaurant.class)
    public Restaurant() {}

    @Ignore
    public Restaurant(@NonNull String id, String name, String location, String imageURL) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.imageURL = imageURL;
        this.lastUpdated = 0;
    }

    @Exclude
    public HashMap<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("name", name);
        result.put("location", location);
        result.put("imageURL", imageURL);

        return result;
    }

    @Override
    public String toString() {
        return String.format("Restaurant: %s, %s, %s", id, name, location);
    }

    @Dao
    public interface RestaurantDAO {
        @Query("SELECT * FROM Restaurant")
        List<Restaurant> getAll();

        @Query("SELECT * FROM Restaurant WHERE id = :id")
        Restaurant getById(String id);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertAll(Restaurant... restaurants);

        @Delete
        void delete(Restaurant restaurant);
    }
}
