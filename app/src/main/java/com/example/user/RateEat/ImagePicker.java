package com.example.user.RateEat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Dialog for picking image from gallery or camera
 */

public class ImagePicker extends Activity{
    public static final int GET_IMAGE = 1;

    private final int REQUEST_CAMERA = 1000, SELECT_FILE = 1001;
    private int userChosenTask;

    private Bitmap bitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature (Window.FEATURE_NO_TITLE);
        getWindow ().setBackgroundDrawableResource (android.R.color.transparent);

        final String[] items = getResources().getStringArray(R.array.add_photo);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(getResources().getString(R.string.add_photo_camera))) {
                    userChosenTask = R.string.add_photo_camera;
                    cameraIntent();
                } else if (items[item].equals(getResources().getString(R.string.add_photo_gallery))) {
                    userChosenTask = R.string.add_photo_gallery;
                    galleryIntent();
                }
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                finish();
            }
        });
        builder.show();
    }

    private void cameraIntent()
    {
        if (Utils.checkCameraPermission(ImagePicker.this)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_CAMERA);
        }
    }

    private void galleryIntent() {
        if (Utils.checkStoragePermission(ImagePicker.this)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case Utils.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                    if (userChosenTask == R.string.add_photo_camera)
                        cameraIntent();
                    break;
                case Utils.MY_PERMISSIONS_REQUEST_CAMERA:
                    if (userChosenTask == R.string.add_photo_gallery)
                        galleryIntent();
                    break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        bitmap = null;

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case SELECT_FILE:
                    onSelectFromGalleryResult(data);
                    break;
                case REQUEST_CAMERA:
                    onCaptureImageResult(data);
                    break;
            }

            if (bitmap != null) {
                Intent returnBitmap = new Intent();

                bitmap = scaleDownBitmap(bitmap, 100, getApplicationContext());
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, bs);

                returnBitmap.putExtra("image", bs.toByteArray());
                setResult(RESULT_OK, returnBitmap);
            } else {
                setResult(RESULT_CANCELED, null);
            }
        }

        finish();
    }

    public Bitmap scaleDownBitmap(Bitmap photo, int newHeight, Context context) {
        final float densityMultiplier = context.getResources().getDisplayMetrics().density;

        int h = (int) (newHeight*densityMultiplier);
        int w = (int) (h * photo.getWidth()/((double) photo.getHeight()));

        photo = Bitmap.createScaledBitmap(photo, w, h, true);

        return photo;
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        if (data != null) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(MyApplication.getMyContext().getContentResolver(), data.getData());
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void onCaptureImageResult(Intent data) {
        bitmap = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
    }
}
