package com.plus.filme.fabiosilva.filme.viewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import com.plus.filme.fabiosilva.filme.R;
import com.plus.filme.fabiosilva.filme.dao.AppDatabase;
import com.plus.filme.fabiosilva.filme.model.Movie;
import com.plus.filme.fabiosilva.filme.model.Review;
import com.plus.filme.fabiosilva.filme.repository.IMovieRepository;
import com.plus.filme.fabiosilva.filme.repository.impl.MovieRepository;

import java.util.List;

public class MovieViewModel extends AndroidViewModel {

    private String mOrderMode;
    private int mPage;
    private boolean mIsFavorite;
    private IMovieRepository mMovieRepository;
    private int mPosition;
    private boolean mIsLoad;

    public MovieViewModel(@NonNull Application application, MovieRepository.ConnectionHandle connectionHandle) {
        super(application);
        mMovieRepository = new MovieRepository(connectionHandle);
        mOrderMode = application.getString(R.string.rate);
        mPage = 1;
        mIsLoad = false;
    }


    public MutableLiveData<Movie> getMovie(int id) {
        return mMovieRepository.findById(id);
    }

    public LiveData<Movie> findFavoriteById(int id) {
        AppDatabase appDatabase = AppDatabase.getInstance(getApplication());
        return appDatabase.favoriteDAO().findFavoriteById(id);
    }

    public LiveData<List<Movie>> findMovies(boolean isRestore) {
        //pegar filmes do banco de dados caso seja os favoritos
        if (mOrderMode.equals(getApplication().getString(R.string.favorite))) {
            AppDatabase appDatabase = AppDatabase.getInstance(getApplication());
            return appDatabase.favoriteDAO().findAll();
        } else {
            //caso a activity seja recriada, use os filmes já baixados no livedata
            if (isRestore) {
                return mMovieRepository.getMovies();
            } else if (mOrderMode.equals(getApplication().getString(R.string.rate))) {
                return mMovieRepository.findAllByRate(mPage);
            } else if (mOrderMode.equals(getApplication().getString(R.string.popularity))) {
                return mMovieRepository.findAllByPopularity(mPage);
            }
        }
        return null;
    }

    public void setPage(int mPage) {
        this.mPage = mPage;
    }

    public void setOrderMode(String mOrderMode) {
        this.mOrderMode = mOrderMode;
    }

    public int getPage() {
        return mPage;
    }

    public String getOrderMode() {
        return mOrderMode;
    }

    public void saveFavorite(Movie movie) {
        AppDatabase appDatabase = AppDatabase.getInstance(getApplication());
        appDatabase.favoriteDAO().save(movie);
    }

    public void deleteFavorite(Movie movie) {
        AppDatabase appDatabase = AppDatabase.getInstance(getApplication());
        appDatabase.favoriteDAO().delete(movie);
    }

    public void setIsFavorite(boolean isFavorite) {
        this.mIsFavorite = isFavorite;
    }

    public boolean getIsFavorite() {
        return mIsFavorite;
    }

    public void clear() {
        mMovieRepository.initLiveData();
    }

    public LiveData<List<Review>> findReviews(int id) {
        return mMovieRepository.findReviewsById(id);
    }

    public void setPosition(int mPosition) {
        this.mPosition = mPosition;
    }

    public int getPosition() {
        return mPosition;
    }

    public boolean getIsLoad() {
        return mIsLoad;
    }

    public void setIsLoad(boolean isLoad) {
        mIsLoad = isLoad;
    }

    public LiveData<List<String>> findVideos(int id) {
        return mMovieRepository.findVideosById(id);
    }
}
