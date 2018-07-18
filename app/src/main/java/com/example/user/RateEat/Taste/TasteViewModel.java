package com.example.user.RateEat.Taste;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.example.user.RateEat.Model.Restaurant;
import com.example.user.RateEat.Model.Taste;
import com.example.user.RateEat.Model.TasteRepository;

import java.util.List;

public class TasteViewModel extends ViewModel{
    private LiveData<List<Taste>> list;
    private TasteRepository repo;

    public TasteViewModel() {
        list = new MutableLiveData<List<Taste>>();
        repo = new TasteRepository();
    }

    public void setRest(Restaurant rest) {
        list = repo.getByRest(rest);
    }

    public LiveData<List<Taste>> getList() {
        return list;
    }
}
