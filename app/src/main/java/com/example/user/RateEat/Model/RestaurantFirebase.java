package com.example.user.RateEat.Model;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestaurantFirebase extends Database<Restaurant>{
    public final String TABLE = "rests";
    private final String TAG = "RestaurantFirebase";
    public final String USER_FAV_TABLE = "user-favs";

    private DatabaseReference db;

    public RestaurantFirebase() {
        db = FirebaseDatabase.getInstance().getReference();
    }

    /* ============================== Read ============================== */

    public void getAll(final Listeners.StatusListener<List<Restaurant>> listener, long lastUpdate) {
        Log.d(TAG, "getAll");

        DatabaseReference currRef = db.getRoot().child(TABLE);

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Restaurant> list = new ArrayList<Restaurant>();

                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    list.add(snapshot.getValue(Restaurant.class));
                }

                listener.onComplete(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadRests:onCancelled", databaseError.toException());
            }
        };

        if (lastUpdate != 0) {
            Query query = currRef.orderByChild("lastUpdated").startAt(lastUpdate);
            query.addValueEventListener(eventListener);
        } else {
            currRef.addValueEventListener(eventListener);
        }
    }

    public void getRestById(String restId, final Listeners.StatusListener<Restaurant> listener) {
        Log.d(getClass().getName(), "getRestById");

        DatabaseReference currRef = db.getRoot().child(TABLE).child(restId);

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onComplete(dataSnapshot.getValue(Restaurant.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(getClass().getName(), "loadRests:onCancelled", databaseError.toException());
            }
        };

        currRef.addValueEventListener(eventListener);
    }

    public void getUserFavs(@NonNull String uid, final Listeners.StatusListener<List<Restaurant>> listener) {
        Log.d(getClass().getName(), "getUsersFavs");

        DatabaseReference currRef = db.getRoot().child(USER_FAV_TABLE).child(uid);

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ArrayList<Restaurant> list = new ArrayList<>();

                final long[] pendingLoadCount = {0};
                pendingLoadCount[0] += dataSnapshot.getChildrenCount();

                // Going over the restaurants
                for (DataSnapshot restSnapshot : dataSnapshot.getChildren()) {

                    getRestById(restSnapshot.getKey(), new Listeners.StatusListener<Restaurant>() {
                        @Override
                        public void onComplete(Restaurant item) {
                            list.add(item);
                            pendingLoadCount[0]--;

                            if (pendingLoadCount[0] == 0)
                            {
                                listener.onComplete(list);
                            }
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting restaurant failed, log a message
                Log.w(getClass().getName(), "loadRests:onCancelled", databaseError.toException());
            }
        };

        currRef.addValueEventListener(eventListener);
    }

    /* ============================== Create ============================== */

    @Override
    public void insert(Restaurant item) {
        insert(item.id, item.name, item.location, item.imageURL);
    }

    public void insert(String name, String location) {
        // Get key
        String key = db.child(TABLE).push().getKey();

        insert(key, name, location, null);
    }

    public void insert(String id, String name, String location, String imageURL) {
        Restaurant rest = new Restaurant(id, name, location, imageURL);

        // Transform to JSON
        Map<String, Object> json = rest.toMap();
        json.put("lastUpdated", ServerValue.TIMESTAMP);

        // Update to DB
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(String.format("/%s/%s", TABLE, id), json);
        db.updateChildren(childUpdates);
    }

    @Override
    public void delete(Restaurant item) {
        if (item.imageURL != null) {
            FirebaseStorage.getInstance().getReferenceFromUrl(item.imageURL).delete();
        }

        db.child(TABLE).child(item.id).removeValue();

        AppLocalStore.db.restaurantDao().delete(item);
    }
}
