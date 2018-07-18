package com.example.user.RateEat.Model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.example.user.RateEat.MyApplication;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class RestaurantRepository {
    public static final RestaurantRepository instance = new RestaurantRepository();

    private RestaurantRepository(){ }

    private MutableLiveData<List<Restaurant>> listLiveData;

    public LiveData<List<Restaurant>> getAll() {
        synchronized (this) {
            if (listLiveData == null) {
                listLiveData = new MutableLiveData<List<Restaurant>>();

                //1. get the last update date
                long lastUpdateDate = 0;
                try {
                    lastUpdateDate = MyApplication.getMyContext()
                            .getSharedPreferences(getClass().getName(), MODE_PRIVATE).getLong("lastUpdateDate", 0);
                }catch (Exception e){

                }

                Listeners.StatusListener<List<Restaurant>> listener = new Listeners.StatusListener<List<Restaurant>>() {
                    @Override
                    public void onComplete(List<Restaurant> data) {
                        updateDataInLocalStorage(data);
                    }
                };

                //2. get all records that where updated since last update date
                Model.getInstance().restaurantModel.getAll(listener, lastUpdateDate);
            }
        }

        return listLiveData;
    }

    private void updateDataInLocalStorage(List<Restaurant> data) {
        Log.d(getClass().getName(), "got items from firebase: " + data.size());
        MyTask task = new MyTask();
        task.execute(data);
    }

    class MyTask extends AsyncTask<List<Restaurant>, String, List<Restaurant>> {
        @Override
        protected List<Restaurant> doInBackground(List<Restaurant>[] lists) {
            Log.d(getClass().getName(),"starting updateDataInLocalStorage in thread");

            if (lists.length > 0) {
                List<Restaurant> data = lists[0];
                long lastUpdateDate = 0;
                try {
                    lastUpdateDate = MyApplication.getMyContext()
                            .getSharedPreferences(getClass().getName(), MODE_PRIVATE).getLong("lastUpdateDate", 0);
                }catch (Exception e){

                }
                if (data != null && data.size() > 0) {
                    //3. update the local DB
                    long reacentUpdate = lastUpdateDate;
                    for (Restaurant item : data) {
                        AppLocalStore.db.restaurantDao().insertAll(item);
                        if (item.lastUpdated > reacentUpdate) {
                            reacentUpdate = item.lastUpdated;
                        }
                        Log.d(getClass().getName(), "updating: " + item.toString());
                    }
                    SharedPreferences.Editor editor = MyApplication.getMyContext().getSharedPreferences(getClass().getName(), MODE_PRIVATE).edit();
                    editor.putLong("lastUpdateDate", reacentUpdate);
                    editor.commit();
                }

                List<Restaurant> lst = AppLocalStore.db.restaurantDao().getAll();
                Log.d(getClass().getName(),"finish updateDataInLocalStorage in thread");

                return lst;
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Restaurant> list) {
            super.onPostExecute(list);
            listLiveData.setValue(list);
            Log.d(getClass().getName(),"update updateDataInLocalStorage in main thread");
            Log.d(getClass().getName(), "got items from local db: " + list.size());

        }
    }
}
