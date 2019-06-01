package com.plus.filme.fabiosilva.filme.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.plus.filme.fabiosilva.filme.model.Movie;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface IFavoriteDAO {
    @Insert(onConflict = REPLACE)
    void save(Movie movie);

    @Delete
    void delete(Movie movie);

    @Query("SELECT *FROM favorite WHERE id=:id")
    LiveData<Movie> findFavoriteById(int id);

    @Query("SELECT *FROM favorite")
    LiveData<List<Movie>> findAll();
}
