package com.example.user.RateEat.Model;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserFirebase extends Database<User> {
    public final String TABLE = "users";

    private DatabaseReference db;

    private final static UserFirebase instance = new UserFirebase();

    public static UserFirebase getInstance() {
        return instance;
    }

    public UserFirebase() {
        db = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void getAll(final Listeners.StatusListener<List<User>> listener, long lastUpdate) {
        Log.d(getClass().getName(), "getAll");

        DatabaseReference currRef = db.getRoot().child(TABLE);

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<User> list = new ArrayList<>();

                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    list.add(snapshot.getValue(User.class));
                }

                listener.onComplete(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(getClass().getName(), "loadUsers:onCancelled", databaseError.toException());
            }
        };

        currRef.addValueEventListener(eventListener);
    }

    public void getById(String userId, final Listeners.StatusListener<User> listener) {
        Log.d(getClass().getName(), "getById");

        DatabaseReference currRef = db.getRoot().child(TABLE).child(userId);

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onComplete(dataSnapshot.getValue(User.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(getClass().getName(), "loadUsers:onCancelled", databaseError.toException());
            }
        };

        currRef.addValueEventListener(eventListener);
    }

    @Override
    public void insert(User item) {
        Map<String, Object> json = item.toMap();

        // Update to DB
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(String.format("/%s/%s", TABLE, item.id), json);
        db.updateChildren(childUpdates);
    }

    @Override
    public void delete(User item) {
        if (item.imageURL != null) {
            FirebaseStorage.getInstance().getReferenceFromUrl(item.imageURL).delete();
        }

        db.child(TABLE).child(item.id).removeValue();
    }

    @Override
    public void edit(User item) {

    }
}