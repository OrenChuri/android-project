package com.example.user.RateEat.Model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

import com.example.user.RateEat.MyApplication;
import com.example.user.RateEat.Taste.TasteListAdapter;
import com.example.user.RateEat.Utils;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class TasteRepository {
    private MutableLiveData<List<Taste>> listLiveData;
    private String preferencesName;

    public LiveData<List<Taste>> getByRest(final Restaurant rest) {
        preferencesName = getClass().getName() + rest.id;

        synchronized (this) {
            if (listLiveData == null) {
                listLiveData = new MutableLiveData<List<Taste>>();

                //1. get the last update date
                long lastUpdateDate = 0;
                try {
                    lastUpdateDate = MyApplication.getMyContext()
                            .getSharedPreferences(preferencesName, MODE_PRIVATE).getLong("lastUpdateDate", 0);
                }catch (Exception e){

                }

                Listeners.StatusListener<List<Taste>> listener = new Listeners.StatusListener<List<Taste>>() {
                    @Override
                    public void onComplete(List<Taste> data) {
                        updateDataInLocalStorage(data, rest.id, null, true);
                    }
                };

                //2. get all records that where updated since last update date
                Model.getInstance().tasteModel.getByRest(rest, listener, lastUpdateDate);
            }
        }

        return listLiveData;
    }

    public LiveData<List<Taste>> getByUser(final String uid) {
        preferencesName = getClass().getName();

        synchronized (this) {
            if (listLiveData == null) {
                listLiveData = new MutableLiveData<List<Taste>>();

                //1. get the last update date
                long lastUpdateDate = 0;
                try {
                    lastUpdateDate = MyApplication.getMyContext()
                            .getSharedPreferences(preferencesName, MODE_PRIVATE).getLong("lastUpdateDate", 0);
                }catch (Exception e){

                }

                Listeners.StatusListener<List<Taste>> listener = new Listeners.StatusListener<List<Taste>>() {
                    @Override
                    public void onComplete(List<Taste> data) {
                        updateDataInLocalStorage(data, null, uid, false);
                    }
                };

                //2. get all records that where updated since last update date
                Model.getInstance().tasteModel.getUserTastes(uid, listener);
            }
        }

        return listLiveData;
    }

    static public void delete(final Taste taste) {
        Model.getInstance().tasteModel.delete(taste);

        Utils.deleteImage(taste.imageURL);

        DeleteTask task = new DeleteTask();
        task.execute(taste);

    }

    static class DeleteTask extends AsyncTask<Taste, String, Boolean> {
        @Override
        protected Boolean doInBackground(Taste... tastes) {
            for (Taste taste : tastes) {
                taste.isDeleted = true;
                AppLocalStore.db.tasteDao().update(taste);
            }

            return true;
        }
    }

    static public void update(final Taste taste) {
        Model.getInstance().tasteModel.edit(taste);

        UpdateTask task = new UpdateTask();
        task.execute(taste);
    }

    static class UpdateTask extends AsyncTask<Taste, String, Boolean> {
        @Override
        protected Boolean doInBackground(Taste... tastes) {
            for (Taste taste : tastes) {
                AppLocalStore.db.tasteDao().update(taste);
            }
            return true;
        }
    }

    private void updateDataInLocalStorage(List<Taste> data, String restId, String userId, boolean isByRest) {
        Log.d(getClass().getName(), "got items from firebase: " + data.size());
        MyTask task = new MyTask(restId, userId, isByRest);
        task.execute(data);
    }

    class MyTask extends AsyncTask<List<Taste>, String, List<Taste>> {
        private String restId;
        private String userId;
        private boolean isByRest;

//        MyTask(String id) { this.restId = id; }
        MyTask(String rid, String uid, boolean isByRest) {
            this.restId = rid;
            this.userId = uid;
            this.isByRest = isByRest;
        }

        @Override
        protected List<Taste> doInBackground(List<Taste>[] lists) {
            Log.d(getClass().getName(),"starting updateDataInLocalStorage in thread");

            if (lists.length > 0) {
                List<Taste> data = lists[0];
                long lastUpdateDate = 0;
                try {
                    lastUpdateDate = MyApplication.getMyContext()
                            .getSharedPreferences(preferencesName, MODE_PRIVATE).getLong("lastUpdateDate", 0);
                }catch (Exception e){

                }
                if (data != null && data.size() > 0) {
                    //3. update the local DB
                    long reacentUpdate = lastUpdateDate;
                    for (Taste item : data) {
                        if (item.isDeleted == false){
                            AppLocalStore.db.tasteDao().insertAll(item);
                        }
                        else {
                            AppLocalStore.db.tasteDao().delete(item);
                        }
                        if (item.lastUpdated > reacentUpdate) {
                            reacentUpdate = item.lastUpdated;
                        }
                        Log.d(getClass().getName(), "updating: " + item.toString());
                    }
                    SharedPreferences.Editor editor = MyApplication.getMyContext().getSharedPreferences(preferencesName, MODE_PRIVATE).edit();
                    editor.putLong("lastUpdateDate", reacentUpdate);
                    editor.commit();
                }

                List<Taste> lst;

                if (isByRest) {
                    lst = AppLocalStore.db.tasteDao().getByRestId(restId);
                }
                else {
                    lst = AppLocalStore.db.tasteDao().getByUserId(userId);
                }
                Log.d(getClass().getName(),"finish updateDataInLocalStorage in thread");

                return lst;
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Taste> list) {
            super.onPostExecute(list);
            listLiveData.setValue(list);
            Log.d(getClass().getName(),"update updateDataInLocalStorage in main thread");
            Log.d(getClass().getName(), "got items from local db: " + list.size());

        }
    }
}
