package com.plus.filme.fabiosilva.filme.adapter;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.plus.filme.fabiosilva.filme.R;
import com.plus.filme.fabiosilva.filme.model.Movie;
import java.util.ArrayList;
import java.util.List;

public class MovieArrayAdapter extends RecyclerView.Adapter<MovieArrayAdapter.MovieViewHolder> {

    private List<Movie> mMovieList;

    final private IMovieHandleAdapter mOnClickListener;

    public void updateMovies(List<Movie> movies) {
        mMovieList = movies;
    }

    public void clear() {
        mMovieList.clear();
    }

    public Movie getItem(int clickedItemIndex) {
        return mMovieList.get(clickedItemIndex);
    }

    public interface IMovieHandleAdapter {
        void onListItemClick(int clickedItemIndex);

        void checkFavorite(int id, Observer<Movie> callBack);
    }

    public MovieArrayAdapter(List<Movie> movies, IMovieHandleAdapter listener) {
        mMovieList = movies;
        mOnClickListener = listener;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.item_array_movie_adapter, viewGroup, false);
        MovieViewHolder viewHolder = new MovieViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        holder.bind(mMovieList.get(position));
    }

    @Override
    public int getItemCount() {
        if (mMovieList == null) {
            mMovieList = new ArrayList<>();
        }
        return mMovieList.size();
    }

    class MovieViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        TextView mTitle;
        ImageView mImage;
        ImageView mFavorite;


        public MovieViewHolder(View viewContext) {
            super(viewContext);

            mTitle = viewContext.findViewById(R.id.title);
            mImage = viewContext.findViewById(R.id.image);
            mFavorite = viewContext.findViewById(R.id.iv_favorite);
            itemView.setOnClickListener(this);
        }

        void bind(Movie movie) {
            mTitle.setText(movie.getTitle());

            //callback para definir se o filme é favorito ou não
            Observer<Movie> callBack = new Observer<Movie>() {
                @Override
                public void onChanged(@Nullable Movie favoriteEntity) {
                    if (favoriteEntity != null) {
                        mFavorite.setVisibility(View.VISIBLE);
                    } else {
                        mFavorite.setVisibility(View.GONE);
                    }
                }
            };

            mOnClickListener.checkFavorite(movie.getId(), callBack);

            if (movie.getImage() != null) {
                Glide.with(itemView.getContext())
                        .load(movie.getImage())
                        .into(mImage);
            }
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition);
        }
    }
}
