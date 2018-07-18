package com.example.user.RateEat.Model;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Taste.class, Restaurant.class, User.class}, version = 4)
public abstract class AppLocalStoreDb extends RoomDatabase {
    public abstract Taste.TasteDAO tasteDao();
    public abstract Restaurant.RestaurantDAO restaurantDao();
    public abstract User.UserDAO userDao();
}
