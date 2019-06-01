package com.plus.filme.fabiosilva.filme.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import com.plus.filme.fabiosilva.filme.model.Movie;
import com.plus.filme.fabiosilva.filme.model.Review;

import java.util.List;

public interface IMovieRepository {
    MutableLiveData<List<Movie>> findAllByRate(Integer... page);
    MutableLiveData<List<Movie>> findAllByPopularity(Integer... page);
    MutableLiveData<Movie> findById(int id);
    void initLiveData();
    LiveData<List<Movie>> getMovies();
    MutableLiveData<List<Review>> findReviewsById(final int id);
    MutableLiveData<List<String>> findVideosById(final int id);
}
