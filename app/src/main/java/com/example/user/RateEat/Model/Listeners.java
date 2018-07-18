package com.example.user.RateEat.Model;

public class Listeners {
    public interface StatusListener<T> {
        void onComplete(T item);
    }
}