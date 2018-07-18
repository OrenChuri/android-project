package com.example.user.RateEat.Restaurant;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.user.RateEat.Model.Restaurant;
import com.example.user.RateEat.Model.RestaurantRepository;

import java.util.List;

public class RestaurantsViewModel extends ViewModel {
    private LiveData<List<Restaurant>> list;

    public RestaurantsViewModel() {
        list = RestaurantRepository.instance.getAll();
    }

    public LiveData<List<Restaurant>> getList() {
        return list;
    }
}