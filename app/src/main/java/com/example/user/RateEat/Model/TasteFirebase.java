package com.example.user.RateEat.Model;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
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

public class TasteFirebase extends Database<Taste>{
    public final String TASTE_TABLE = "tastes";
    public final String USER_TASTES_TABLE = "user-tastes";
    public final String USER_FAV_TABLE = "user-favs";

    private DatabaseReference db;

    private final static TasteFirebase instance = new TasteFirebase();

    public static TasteFirebase getInstance(){
        return instance;
    }

    public TasteFirebase() {
        db = FirebaseDatabase.getInstance().getReference();
    }

    /* ============================== Read ============================== */

    @Override
    public void getAll(final Listeners.StatusListener<List<Taste>> listener, long lastUpdate) {
        Log.d(getClass().getName(), "getAll");

        DatabaseReference currRef = db.getRoot().child(TASTE_TABLE);

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Taste> list = new ArrayList<Taste>();

                for (DataSnapshot restSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot snapshot: restSnapshot.getChildren()) {
                        Taste taste = snapshot.getValue(Taste.class);
                        if(!taste.isDeleted)
                            list.add(taste);
                    }
                }

                listener.onComplete(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(getClass().getName(), "loadTastes:onCancelled", databaseError.toException());
            }
        };

        if (lastUpdate != 0) {
            Query query = currRef.orderByChild("lastUpdated").startAt(lastUpdate);
            query.addValueEventListener(eventListener);
        } else {
            currRef.addValueEventListener(eventListener);
        }
    }

    public void getByRest(Restaurant rest, final Listeners.StatusListener<List<Taste>> listener) {
        getByRest(rest, listener, 0);
    }

    public void getByRest(Restaurant rest, final Listeners.StatusListener<List<Taste>> listener, long lastUpdate) {
        Log.d(getClass().getName(), "getByRest");

        DatabaseReference currRef = db.getRoot().child(TASTE_TABLE).child(rest.id);

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Taste> list = new ArrayList<Taste>();

                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Taste taste = snapshot.getValue(Taste.class);
//                    if(!taste.isDeleted)
                        list.add(taste);
                }

                listener.onComplete(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(getClass().getName(), "loadTastes:onCancelled", databaseError.toException());
            }
        };

        if (lastUpdate != 0) {
            Query query = currRef.orderByChild("lastUpdated").startAt(lastUpdate);
            query.addValueEventListener(eventListener);
        } else {
            currRef.addValueEventListener(eventListener);
        }
    }

    public void getTasteById(String restId, String tasteId, final Listeners.StatusListener<Taste> listener) {
        Log.d(getClass().getName(), "getTasteById");

        DatabaseReference currRef = db.getRoot().child(TASTE_TABLE).child(restId).child(tasteId);

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onComplete(dataSnapshot.getValue(Taste.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(getClass().getName(), "loadTastes:onCancelled", databaseError.toException());
            }
        };

        currRef.addValueEventListener(eventListener);
    }

    public void getUserTastes(@NonNull String uid, final Listeners.StatusListener<List<Taste>> listener) {
        Log.d(getClass().getName(), "getUsersTastes");

        DatabaseReference currRef = db.getRoot().child(USER_TASTES_TABLE).child(uid);

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ArrayList<Taste> list = new ArrayList<>();

                // Final array to be accessed through the anonymous function, but still it's cell
                // is changeable
                final long[] pendingLoadCount = {0};

                // Going over the restaurants
                for (DataSnapshot restSnapshot : dataSnapshot.getChildren()) {
                    pendingLoadCount[0] += restSnapshot.getChildrenCount();

                    // Going over the restaurant's tastes
                    for (DataSnapshot tasteSnapshot : restSnapshot.getChildren()) {

                        getTasteById(restSnapshot.getKey(), tasteSnapshot.getKey(), new Listeners.StatusListener<Taste>() {
                            @Override
                            public void onComplete(Taste item) {
                                if(!item.isDeleted)
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
    public void insert(Taste item) {
        insert(item.restId, item.title, item.description, item.starCount, item.imageURL, item.date);
    }

    public void insert(String restId, String title, String description,
                       float stars, String imageURL, String date) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.w(getClass().getName(), "Tried to insert taste when user isn't logged in");
            return;
        }

        // Get key
        String key = db.child(TASTE_TABLE).child(restId).push().getKey();
        Taste taste = new Taste(key, restId, title,
                FirebaseAuth.getInstance().getCurrentUser().getUid(), FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                description, stars, imageURL, date);

        // Transform to JSON
        Map<String, Object> tasteJSON = taste.toMap();
        tasteJSON.put("lastUpdated", ServerValue.TIMESTAMP);

        // Update to DB
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(String.format("/%s/%s/%s",TASTE_TABLE, restId, key), tasteJSON);
        db.updateChildren(childUpdates);

        // Update User-taste
        Map<String, Object> userTasteJSON = new HashMap<>();
        userTasteJSON.put(String.format("/%s/%s/%s/%s",
                USER_TASTES_TABLE, FirebaseAuth.getInstance().getCurrentUser().getUid(), restId, key), true);
        db.updateChildren(userTasteJSON);

        if (stars == 5){
            Map<String, Object> userFavJSON = new HashMap<>();
            userFavJSON.put(String.format("/%s/%s/%s/%s",
                    USER_FAV_TABLE, FirebaseAuth.getInstance().getCurrentUser().getUid(), restId, key), true);
            db.updateChildren(userFavJSON);
        }
    }

    /* ============================== Delete ============================== */

    @Override
    public void delete(Taste taste) {
        //db.child(TASTE_TABLE).child(taste.restId).child(taste.id).removeValue();
        //db.child(USER_TASTES_TABLE).child(taste.authorId).child(taste.restId).child(taste.id).removeValue();
        //db.child(USER_FAV_TABLE).child(taste.authorId).child(taste.restId).child(taste.id).removeValue();

        Map<String, Object> values = taste.toMap();
        values.put("isDeleted", true);

        db.child(TASTE_TABLE).child(taste.restId).child(taste.id).setValue(values);
        db.child(USER_TASTES_TABLE).child(taste.authorId).child(taste.restId).child(taste.id).removeValue();
        db.child(USER_FAV_TABLE).child(taste.authorId).child(taste.restId).child(taste.id).removeValue();
    }

    @Override
    public void edit(Taste taste) {

        Map<String, Object> values = taste.toMap();
        values.put("lastUpdated", ServerValue.TIMESTAMP);
        db.child(TASTE_TABLE).child(taste.restId).child(taste.id).setValue(values);

        if (taste.starCount == 5){
            Map<String, Object> userFavJSON = new HashMap<>();
            userFavJSON.put(String.format("/%s/%s/%s/%s",
                    USER_FAV_TABLE, FirebaseAuth.getInstance().getCurrentUser().getUid(), taste.restId, taste.id), true);
            db.updateChildren(userFavJSON);
        }
        else {
//            db.child(USER_FAV_TABLE).child(taste.authorId).child(taste.restId).child(taste.id).removeValue();
        }

    }

}
