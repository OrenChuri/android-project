package com.example.user.RateEat.Model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

/**
 * Global singleton connects all the Firebase modules.
 */

public class Model {
    private final static Model instance = new Model();
    public RestaurantFirebase restaurantModel;
    public TasteFirebase tasteModel;
    public UserFirebase userModel;

    public static Model getInstance(){
        return instance;
    }

    private Model() {
        restaurantModel = new RestaurantFirebase();
        tasteModel = new TasteFirebase();
        userModel = new UserFirebase();
    }

    public void saveImage(final Bitmap image, final String name, final Listeners.StatusListener<String> listener) {
        StorageReference imagesRef = FirebaseStorage.getInstance().getReference().child("images").child(name);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imagesRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                listener.onComplete(null);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                listener.onComplete(downloadUrl.toString());
            }
        });
    }

    public void getImage(final String url, final Listeners.StatusListener<Bitmap> listener){
        StorageReference httpsReference = FirebaseStorage.getInstance().getReferenceFromUrl(url);
        final long ONE_MEGABYTE = 1024 * 1024;

        httpsReference.getBytes(3* ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap image = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                listener.onComplete(image);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                Log.d("TAG",exception.getMessage());
                listener.onComplete(null);
            }
        });
    }
}
