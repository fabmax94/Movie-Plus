package com.plus.filme.fabiosilva.filme.viewModel;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.plus.filme.fabiosilva.filme.repository.impl.MovieRepository;

public class CustomViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private Application application;
    private MovieRepository.ConnectionHandle connectionHandle;

    public CustomViewModelFactory(Application application, MovieRepository.ConnectionHandle connectionHandle){
        this.application = application;
        this.connectionHandle = connectionHandle;
    }
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new MovieViewModel(this.application, this.connectionHandle);
    }
}
