package com.example.user.RateEat.Model;

import android.arch.persistence.room.Room;

import com.example.user.RateEat.MyApplication;

public class AppLocalStore{
    private static final String DB_NAME = "database-name";

    static public AppLocalStoreDb db = Room.databaseBuilder(MyApplication.getMyContext(),
            AppLocalStoreDb.class,
            DB_NAME).fallbackToDestructiveMigration().build();

    public static void deleteDataBase()
    {
        MyApplication.getMyContext().deleteDatabase(DB_NAME);
    }
}