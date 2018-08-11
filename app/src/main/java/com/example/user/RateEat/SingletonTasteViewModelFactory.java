package com.example.user.RateEat;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.example.user.RateEat.Model.Taste;
import com.example.user.RateEat.Taste.TasteViewModel;

public class SingletonTasteViewModelFactory<T extends ViewModel>  extends ViewModelProvider.NewInstanceFactory {
    T t;
    final static SingletonTasteViewModelFactory f = new SingletonTasteViewModelFactory<TasteViewModel>(new TasteViewModel());

    private SingletonTasteViewModelFactory(T t){
        this.t = t;
    }

    public static SingletonTasteViewModelFactory get(){
        return f;
    }

    @NonNull
    @Override
    public <A extends ViewModel> A create(@NonNull Class<A> modelClass) {
        return (A)SingletonTasteViewModelFactory.get().t;
    }
}
