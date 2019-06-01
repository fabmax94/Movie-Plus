package com.plus.filme.fabiosilva.filme.repository.impl;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.text.TextUtils;
import android.util.Log;
import com.plus.filme.fabiosilva.filme.model.Movie;
import com.plus.filme.fabiosilva.filme.model.Review;
import com.plus.filme.fabiosilva.filme.repository.IMovieRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MovieRepository implements IMovieRepository {

    public final String API_KEY = "1ea183a23e8e561d988e5de4b7d1fafd";
    private final String URL_API = "http://api.themoviedb.org/3";
    private final String URL_IMAGE_API = "http://image.tmdb.org/t/p/w185";
    private final String URL_FULL_IMAGE_API = "http://image.tmdb.org/t/p/w500";
    private ConnectionHandle mConnectionHandle;
    private MutableLiveData<List<Movie>> mLiveDataList;
    private MutableLiveData<List<Review>> mLiveDataReviewList;
    private MutableLiveData<List<String>> mLiveDataVideoList;
    private MutableLiveData<Movie> mLiveData;

    public MovieRepository(ConnectionHandle connectionHandle){
        mConnectionHandle = connectionHandle;
        initLiveData();
    }

    @Override
    public MutableLiveData<List<Review>> findReviewsById(final int id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mConnectionHandle.checkConnection();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(URL_API);
                stringBuilder.append("/movie/" + id);
                stringBuilder.append("/reviews");
                stringBuilder.append("?api_key=" + API_KEY);
                stringBuilder.append("&language=" + Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry());
                try {
                    mLiveDataReviewList.postValue(findReviewByFilter(stringBuilder));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return mLiveDataReviewList;
    }

    @Override
    public MutableLiveData<List<String>> findVideosById(final int id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mConnectionHandle.checkConnection();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(URL_API);
                stringBuilder.append("/movie/" + id);
                stringBuilder.append("/videos");
                stringBuilder.append("?api_key=" + API_KEY);
                stringBuilder.append("&language=" + Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry());
                try {
                    mLiveDataVideoList.postValue(findVideoByFilter(stringBuilder));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return mLiveDataVideoList;
    }

    @Override
    public MutableLiveData<Movie> findById(final int id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mConnectionHandle.checkConnection();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(URL_API);
                stringBuilder.append("/movie/" + id);
                stringBuilder.append("?api_key=" + API_KEY);
                stringBuilder.append("&language=" + Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry());
                try {
                    mLiveData.postValue(findMovieByFilter(stringBuilder));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return mLiveData;
    }

    @Override
    public void initLiveData() {
        mLiveDataList = new MutableLiveData<>();
        mLiveDataReviewList = new MutableLiveData<>();
        mLiveDataVideoList = new MutableLiveData<>();
        mLiveData = new MutableLiveData<>();
    }

    @Override
    public LiveData<List<Movie>> getMovies() {
        return mLiveDataList;
    }

    @Override
    public MutableLiveData<List<Movie>> findAllByRate(final Integer... page) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mConnectionHandle.checkConnection();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(URL_API);
                stringBuilder.append("/movie/top_rated");
                stringBuilder.append("?api_key=" + API_KEY);
                int pageString = page.length > 0 ? page[0] : 1;
                stringBuilder.append("&page=" + pageString);
                stringBuilder.append("&language=" + Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry());
                try {
                    postValue(findAllMoviesByFilter(stringBuilder));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return mLiveDataList;
    }

    @Override
    public MutableLiveData<List<Movie>> findAllByPopularity(final Integer... page) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mConnectionHandle.checkConnection();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(URL_API);
                stringBuilder.append("/movie/popular");
                stringBuilder.append("?api_key=" + API_KEY);
                int pageString = page.length > 0 ? page[0] : 1;
                stringBuilder.append("&page=" + pageString);
                stringBuilder.append("&language=" + Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry());
                try {
                    Log.i("teste", stringBuilder.toString());
                    postValue(findAllMoviesByFilter(stringBuilder));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return mLiveDataList;
    }

    private ArrayList<Movie> findAllMoviesByFilter(StringBuilder stringBuilder) throws IOException {
        URL url = new URL(stringBuilder.toString());
        InputStream stream = null;
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            stream = conn.getInputStream();
            return parseResult(stringify(stream));
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    private ArrayList<Movie> parseResult(String result) {
        String streamAsString = result;
        ArrayList<Movie> results = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(streamAsString);
            JSONArray array = (JSONArray) jsonObject.get("results");
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonMovieObject = array.getJSONObject(i);
                int id = jsonMovieObject.getInt("id");
                String title = jsonMovieObject.getString("title");
                Double rate = jsonMovieObject.getDouble("vote_average");
                String image = getImageByUrl(jsonMovieObject.getString("poster_path"));
                results.add(new Movie(id, title, rate, image));
            }
        } catch (JSONException e) {
            System.err.println(e);
            Log.d(MovieRepository.class.getName(), "Error parsing JSON. String was: " + streamAsString);
        }
        return results;
    }

    private String stringify(InputStream stream) {
        try {
            Reader reader = new InputStreamReader(stream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(reader);
            return bufferedReader.readLine();
        } catch (IOException ioException) {
            Log.e(MovieRepository.class.getName(), "Error to stringify");
            return null;
        }
    }

    private String getImageByUrl(String url) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(URL_IMAGE_API);
        stringBuilder.append(url);
        return stringBuilder.toString();
    }

    private String getFullImageByUrl(String url) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(URL_FULL_IMAGE_API);
        stringBuilder.append(url);
        return stringBuilder.toString();
    }

    private Movie findMovieByFilter(StringBuilder stringBuilder) throws IOException {
        URL url = new URL(stringBuilder.toString());
        InputStream stream = null;
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            stream = conn.getInputStream();
            return parseMovieResult(stringify(stream));
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    private List<Review> findReviewByFilter(StringBuilder stringBuilder) throws IOException {
        URL url = new URL(stringBuilder.toString());
        InputStream stream = null;
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            stream = conn.getInputStream();
            return parseReviewsResult(stringify(stream));
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    private List<String> findVideoByFilter(StringBuilder stringBuilder) throws IOException {
        URL url = new URL(stringBuilder.toString());
        InputStream stream = null;
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            stream = conn.getInputStream();
            return parseVideosResult(stringify(stream));
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    private Movie parseMovieResult(String result) {
        String streamAsString = result;
        try {
            JSONObject jsonMovieObject = new JSONObject(streamAsString);
            int id = jsonMovieObject.getInt("id");
            String title = jsonMovieObject.getString("original_title");
            Double rate = jsonMovieObject.getDouble("vote_average");
            String resume = jsonMovieObject.getString("overview");
            String date = jsonMovieObject.getString("release_date");
            JSONArray array = (JSONArray) jsonMovieObject.get("genres");
            List<String> gender = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                gender.add(array.getJSONObject(i).getString("name"));
            }
            String image = getImageByUrl(jsonMovieObject.getString("poster_path"));
            String fullImage = getFullImageByUrl(jsonMovieObject.getString("poster_path"));
            return new Movie(id, title, rate, TextUtils.join(", ", gender), date, resume, image, fullImage);

        } catch (JSONException e) {
            System.err.println(e);
            Log.d(MovieRepository.class.getName(), "Error parsing JSON. String was: " + streamAsString);
        }
        return null;
    }

    private List<Review> parseReviewsResult(String result) {
        String streamAsString = result;
        try {
            JSONObject jsonMovieObject = new JSONObject(streamAsString);
            JSONArray array = (JSONArray) jsonMovieObject.get("results");
            List<Review> reviews = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                reviews.add(new Review(array.getJSONObject(i).getString("author"), array.getJSONObject(i).getString("content")));
            }
            return reviews;

        } catch (JSONException e) {
            System.err.println(e);
            Log.d(MovieRepository.class.getName(), "Error parsing JSON. String was: " + streamAsString);
        }
        return null;
    }

    private List<String> parseVideosResult(String result) {
        String streamAsString = result;
        try {
            JSONObject jsonMovieObject = new JSONObject(streamAsString);
            JSONArray array = (JSONArray) jsonMovieObject.get("results");
            List<String> videos = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                videos.add(array.getJSONObject(i).getString("key"));
            }
            return videos;

        } catch (JSONException e) {
            System.err.println(e);
            Log.d(MovieRepository.class.getName(), "Error parsing JSON. String was: " + streamAsString);
        }
        return null;
    }

    private void postValue(List<Movie> movies){
        if (mLiveDataList.getValue() == null) {
            mLiveDataList.postValue(movies);
        } else {
            mLiveDataList.getValue().addAll(movies);
            mLiveDataList.postValue(mLiveDataList.getValue());
        }
    }

    public interface ConnectionHandle{
        void checkConnection();
    }
}
