package com.example.user.RateEat.Model;

import java.util.List;

/**
 * Generic database each module database needs to implement.
 */

public abstract class Database<T> {
    public void getAll(final Listeners.StatusListener<List<T>> listener) {
        getAll(listener, 0);
    }

    abstract public void getAll(final Listeners.StatusListener<List<T>> listener, long lastUpdate);

    abstract public void insert(T item);

    abstract public void delete(T item);

    abstract public void edit(T item);
}
