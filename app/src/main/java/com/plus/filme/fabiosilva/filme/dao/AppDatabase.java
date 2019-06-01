package com.plus.filme.fabiosilva.filme.dao;

import android.app.Application;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import com.plus.filme.fabiosilva.filme.model.Movie;

@Database(entities = {Movie.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract IFavoriteDAO favoriteDAO();
    public static String DATABASE_NAME = "movie_plus";
    private static AppDatabase appDatabase;
    public static AppDatabase getInstance(Application application){
        if(appDatabase == null){
            appDatabase = Room.databaseBuilder(application.getApplicationContext(), AppDatabase.class, DATABASE_NAME).build();
        }
        return appDatabase;
    }
}

