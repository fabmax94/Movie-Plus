package com.plus.filme.fabiosilva.filme;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.plus.filme.fabiosilva.filme.adapter.MovieArrayAdapter;
import com.plus.filme.fabiosilva.filme.model.Movie;
import com.plus.filme.fabiosilva.filme.repository.impl.MovieRepository;
import com.plus.filme.fabiosilva.filme.viewModel.CustomViewModelFactory;
import com.plus.filme.fabiosilva.filme.viewModel.MovieViewModel;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements MovieArrayAdapter.IMovieHandleAdapter, MovieRepository.ConnectionHandle {

    public static int ORDER_BY = 1;
    private RecyclerView mRecyclerView;
    private MovieArrayAdapter mAdapter;
    private Snackbar mSnackBar;
    private boolean isPause;
    private MovieViewModel mMovieViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //snackbar para notificar a falta de internet
        mSnackBar = Snackbar.make(findViewById(R.id.constraint_container),
                getString(R.string.no_internet), Snackbar.LENGTH_INDEFINITE);

        mRecyclerView = findViewById(R.id.rv_movies);

        //inicializar o viewmodel e o observer para buscar filmes
        setupViewModel(savedInstanceState != null);

        //evento para buscar novos filmes a medida que o filme da lista é alcançado
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                assert linearLayoutManager != null;
                int totalItemCount = linearLayoutManager.getItemCount();
                int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                mMovieViewModel.setPosition(lastVisibleItem);
                //caso faltem somente 5 items na lista pra chegar no final e se já não tiver feito uma requisição então uma nova requisição será feita pra pegar a proxima pagina
                if (totalItemCount <= (lastVisibleItem + 5) && totalItemCount != 0 && !mMovieViewModel.getIsLoad()) {
                    mMovieViewModel.setPage(mMovieViewModel.getPage() + 1);
                    mMovieViewModel.setIsLoad(true);
                    loadMovies(false);
                }
            }
        });
    }

    private void setupViewModel(boolean isRestore) {
        CustomViewModelFactory customViewModelFactory = new CustomViewModelFactory(getApplication(), this);
        mMovieViewModel = ViewModelProviders.of(this, customViewModelFactory).get(MovieViewModel.class);
        loadMovies(isRestore);
    }

    private void loadRecyclerView(List<Movie> movies) {
        //caso o adapter já existir então somente atualize a lista e notifique
        if (mAdapter == null) {
            mAdapter = new MovieArrayAdapter(movies, MainActivity.this);
            int mGridColumn = 2;
            GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), mGridColumn);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.scrollToPosition(mMovieViewModel.getPosition());
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.updateMovies(movies);
            mAdapter.notifyDataSetChanged();
        }
        //libere novos requests e desapareça com o progressbar
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        mMovieViewModel.setIsLoad(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //pause o snackbar
        isPause = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //libere o funcionamento do snackbar
        isPause = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //infle o menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.order:
                //caso o menu clicado seja o de ordem então dê inicio a activity de seleção de ordem
                Intent intent = new Intent(this, OrderActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, mMovieViewModel.getOrderMode());
                startActivityForResult(intent, ORDER_BY);
                overridePendingTransition(R.anim.slide_down_in,
                        R.anim.slide_down_out);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        //evento de clique para os elementos da lista, caso clicado abra os detalhes do filme
        Movie movie = mAdapter.getItem(clickedItemIndex);
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra(DetailActivity.MOVIE_ID, movie.getId());
        startActivity(intent);
    }

    @Override
    public void checkFavorite(int id, Observer<Movie> callBack) {
        //função de callback para checar se o filme é favorito
        mMovieViewModel.findFavoriteById(id).observe(this, callBack);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //resultado da seleção de ordem
        if (resultCode == ORDER_BY) {
            assert data != null;
            mMovieViewModel.setOrderMode(Objects.requireNonNull(data.getExtras()).getString(OrderActivity.ORDER_TYPE));
            mMovieViewModel.setPage(1);
            mMovieViewModel.clear();
            //limpar a lista para serem listados em ordem diferente
            if (mAdapter != null) {
                mAdapter.clear();
                mAdapter.notifyDataSetChanged();
            }
            loadMovies(false);
        }
    }

    private void loadMovies(final boolean isRestore) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //consulte os filmes que serão listados somente com a presença da internet
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                        final LiveData liveData = mMovieViewModel.findMovies(isRestore);
                        liveData.observe(MainActivity.this, new Observer<List<Movie>>() {
                            @Override
                            public void onChanged(@Nullable List<Movie> movies) {
                                liveData.removeObserver(this);
                                loadRecyclerView(movies);
                            }
                        });
                    }
                });
            }
        }).start();
    }


    @Override
    public void checkConnection() {
        while (!isOnline() && !isPause) {
            MainActivity.this.runOnUiThread(new Runnable() {
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
        MainActivity.this.runOnUiThread(new Runnable() {
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
}
