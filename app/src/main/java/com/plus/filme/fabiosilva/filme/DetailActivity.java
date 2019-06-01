package com.plus.filme.fabiosilva.filme;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.plus.filme.fabiosilva.filme.model.Movie;
import com.plus.filme.fabiosilva.filme.model.Review;
import com.plus.filme.fabiosilva.filme.repository.impl.MovieRepository;
import com.plus.filme.fabiosilva.filme.viewModel.CustomViewModelFactory;
import com.plus.filme.fabiosilva.filme.viewModel.MovieViewModel;

import java.util.List;
import java.util.Objects;

public class DetailActivity extends AppCompatActivity implements MovieRepository.ConnectionHandle {

    public static final String MOVIE_ID = "MOVIE_ID";

    private ConstraintLayout mContainer;
    private TextView mTitle;
    private TextView mGender;
    private TextView mRate;
    private TextView mDate;
    private TextView mResume;
    private ImageView mImage;
    private Snackbar mSnackBar;
    private boolean isDestroy;
    private MovieViewModel mMovieViewModel;
    private Movie mMovie;
    private int mMovieId;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSnackBar = Snackbar.make(findViewById(R.id.container),
                getString(R.string.no_internet), Snackbar.LENGTH_INDEFINITE);

        mContainer = findViewById(R.id.container);
        mTitle = findViewById(R.id.title);
        mGender = findViewById(R.id.gender);
        mRate = findViewById(R.id.rate);
        mDate = findViewById(R.id.date);
        mResume = findViewById(R.id.resume);
        mImage = findViewById(R.id.image);

        if (getIntent().hasExtra(DetailActivity.MOVIE_ID)) {
            mMovieId = Objects.requireNonNull(getIntent().getExtras()).getInt(DetailActivity.MOVIE_ID);
            setupViewModel(mMovieId);
        } else {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
            case R.id.favorite:
                //add aos favoritos somente se o filme já estiver carregado
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mMovie != null) {
                            if (!mMovieViewModel.getIsFavorite()) {
                                mMovieViewModel.saveFavorite(mMovie);
                            } else {
                                mMovieViewModel.deleteFavorite(mMovie);
                            }
                        }
                    }
                }).start();
        }
        return true;
    }

    private void checkIsFavorite(final MenuItem item) {
        //caso o filme tinha sido encontrado no banco então ele é favorito, então defina o icone preenchido
        mMovieViewModel.findFavoriteById(mMovieId).observe(this, new Observer<Movie>() {
            @Override
            public void onChanged(@Nullable Movie movie) {
                if (movie == null) {
                    item.setIcon(getDrawable(R.drawable.ic_star_border_white_24dp));
                } else {
                    item.setIcon(getDrawable(R.drawable.ic_star_white_24dp));
                    setMovie(movie);
                }
                mMovieViewModel.setIsFavorite(movie != null);
            }
        });
    }

    private void setMovie(Movie movie) {
        if (mMovie == null) {
            mMovie = movie;
            loadMovie(movie);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detail, menu);
        checkIsFavorite(menu.findItem(R.id.favorite));
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSnackBar.isShown()) {
            mSnackBar.dismiss();
        }
        isDestroy = true;
    }

    @Override
    public void checkConnection() {
        while (!isOnline() && !isDestroy) {
            DetailActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    snackBarShow(true);
                }
            });
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        snackBarShow(false);
    }

    private void snackBarShow(final boolean show) {
        DetailActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                if (show) {
                    if (!mSnackBar.isShown()) {
                        mSnackBar.show();
                    }
                } else {
                    if (mSnackBar.isShown()) {
                        mSnackBar.dismiss();
                    }
                }
            }
        });
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    private void findMovieById(final int id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DetailActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                        //procurar o filme
                        mMovieViewModel.getMovie(id).observe(DetailActivity.this, new Observer<Movie>() {
                            @Override
                            public void onChanged(@Nullable Movie movie) {
                                setMovie(movie);
                            }
                        });

                        //procurar os reviews
                        mMovieViewModel.findReviews(id).observe(DetailActivity.this, new Observer<List<Review>>() {
                            @Override
                            public void onChanged(@Nullable List<Review> reviews) {
                                LinearLayout linearLayout = findViewById(R.id.container_review);
                                for (Review review : reviews) {
                                    View viewPrepare = getLayoutInflater().inflate(R.layout.review_layout, null);
                                    linearLayout.addView(viewPrepare);
                                    TextView author = viewPrepare.findViewById(R.id.tv_author);
                                    author.setText(review.getAuthor());
                                    TextView content = viewPrepare.findViewById(R.id.tv_content);
                                    content.setText(review.getContent());
                                }
                                if(reviews.size() != 0){
                                    linearLayout.setVisibility(View.VISIBLE);
                                }
                            }
                        });


                        //procurar os trailers
                        mMovieViewModel.findVideos(id).observe(DetailActivity.this, new Observer<List<String>>() {
                            @Override
                            public void onChanged(@Nullable List<String> videos) {
                                LinearLayout linearLayout = findViewById(R.id.container_trailer);
                                for (final String video : videos) {
                                    View viewPrepare = getLayoutInflater().inflate(R.layout.trailer_layout, null);
                                    linearLayout.addView(viewPrepare);
                                    ImageView thumbnail = viewPrepare.findViewById(R.id.img_thumnail);
                                    thumbnail.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                                                    Uri.parse("http://www.youtube.com/watch?v=" + video));
                                            startActivity(webIntent);
                                        }
                                    });
                                    //carregar imagem para o thumbnail
                                    Glide.with(viewPrepare.getContext())
                                            .load("http://img.youtube.com/vi/" + video + "/0.jpg")
                                            .into(thumbnail);
                                }
                                if(videos.size() != 0){
                                    linearLayout.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                });
            }
        }).start();
    }

    private void loadMovie(Movie movie) {
        //preencher o background com imagem
        Glide.with(getApplicationContext()).load(movie.getFullImage()).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                mContainer.setBackground(resource);
            }
        });

        mTitle.setText(movie.getTitle());
        mGender.setText(movie.getGender());
        mRate.setText(String.valueOf(movie.getRate()));
        mDate.setText(movie.getDate());
        mResume.setText(movie.getResume());
        Glide.with(getApplicationContext())
                .load(movie.getImage())
                .into(mImage);
        findViewById(R.id.progressBar).setVisibility(View.GONE);
    }

    private void setupViewModel(int id) {
        CustomViewModelFactory customViewModelFactory = new CustomViewModelFactory(getApplication(), this);
        mMovieViewModel = ViewModelProviders.of(this, customViewModelFactory).get(MovieViewModel.class);
        findMovieById(id);
    }
}
