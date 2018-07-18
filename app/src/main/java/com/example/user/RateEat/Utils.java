package com.example.user.RateEat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.ImageView;

import com.example.user.RateEat.Model.Listeners;
import com.example.user.RateEat.Model.Model;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Common utilities used in variety of modules.
 */

public class Utils {
    public static void setImageView(final ImageView imgView, final String imageURL) {
        if (imgView == null || imageURL == null) {
            return;
        }

        imgView.setTag(imageURL);
        Utils.loadImage(imageURL, new Listeners.StatusListener<Bitmap>() {
            @Override
            public void onComplete(Bitmap item) {
                if (item == null) {
                    Log.d(Utils.class.getName(), "Failed to load image" + imageURL);
                    return;
                }

                if (((String)imgView.getTag()).equals(imageURL)) {
                    imgView.setImageBitmap(item);
                }
            }
        });
    }

    public static void saveImage(final Bitmap image, final String name, final Listeners.StatusListener<String> listener) {
        //1. save the image remotely
        Model.getInstance().saveImage(image, name, new Listeners.StatusListener<String>() {
            @Override
            public void onComplete(String item) {
                if (item != null) {
                    // 2. saving the file locally
                    String localName = getLocalImageFileName(name);
                    Log.d(Utils.class.getName(), "cache image: " + localName);
                    saveImageToFile(image, localName); // synchronously save image locally
                }

                listener.onComplete(item);
            }
        });
    }

    public static void deleteImage(final String url) {
        if (url == null) {
            return;
        }

        // Server
        FirebaseStorage.getInstance().getReferenceFromUrl(url).delete();

        // Local storage
        String localFileName = getLocalImageFileName(url);
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(dir, localFileName);
        imageFile.delete();
    }

    private static void loadImage(final String url, final Listeners.StatusListener<Bitmap> listener) {
        //1. first try to find the image on the device
        String localFileName = getLocalImageFileName(url);
        Bitmap image = loadImageFromFile(localFileName);

        if (image == null) { //if image not found - try downloading it from parse
            Model.getInstance().getImage(url, new Listeners.StatusListener<Bitmap>() {
                @Override
                public void onComplete(Bitmap item) {
                    if (item != null) {
                        //2. save the image locally
                        String localFileName = getLocalImageFileName(url);
                        Log.d(Utils.class.getName(), "save image to cache: " + localFileName);
                        saveImageToFile(item, localFileName);
                    }

                    //3. return the image using the listener
                    listener.onComplete(item);
                }
            });
        } else {
            Log.d(Utils.class.getName(),"OK reading cache image: " + localFileName);
            listener.onComplete(image);
        }
    }

    /* ============================== Files ============================== */

    private static Bitmap loadImageFromFile(String imageFileName){
        Bitmap bitmap = null;

        try {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File imageFile = new File(dir,imageFileName);
            InputStream inputStream = new FileInputStream(imageFile);
            bitmap = BitmapFactory.decodeStream(inputStream);
            Log.d(Utils.class.getName(),"got image from cache: " + imageFileName);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    private static void saveImageToFile(Bitmap imageBitmap, String imageFileName) {
        try {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            if (!dir.exists()) {
                dir.mkdir();
            }

            File imageFile = new File(dir, imageFileName);
            imageFile.createNewFile();

            OutputStream out = new FileOutputStream(imageFile);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();

            addPictureToGallery(imageFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addPictureToGallery(File imageFile){
        //add the picture to the gallery so we dont need to manage the cache size
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(imageFile);
        mediaScanIntent.setData(contentUri);
        MyApplication.getMyContext().sendBroadcast(mediaScanIntent);
    }

    private static String getLocalImageFileName(String url) {
        return URLUtil.guessFileName(url, null, null);
    }

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 124;

    public static boolean checkCameraPermission(final Context context) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[] {android.Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
            return false;
        }

        return true;
    }

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean checkStoragePermission(final Context context)
    {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>=android.os.Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("External storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
}
