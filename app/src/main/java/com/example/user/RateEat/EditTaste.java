package com.example.user.RateEat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.user.RateEat.Model.Listeners;
import com.example.user.RateEat.Model.Model;
import com.example.user.RateEat.Model.Restaurant;
import com.example.user.RateEat.Model.Taste;
import com.example.user.RateEat.Model.TasteRepository;
import com.example.user.RateEat.Taste.TastePage;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class EditTaste extends Activity {
    private Bitmap image;
    String author;
    String authorId;
    String date;
    String description;
    String id;
    String imageURL;
    String restId;
    float starCount;
    String title;
    ImageView imageView;
    EditText titleET;
    RatingBar ratingBar;
    EditText descriptionET;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_taste);

        Intent intent = getIntent();
        author = (String) intent.getSerializableExtra("author");
        authorId = (String) intent.getSerializableExtra("authorId");
        date = (String) intent.getSerializableExtra("date");
        description = (String) intent.getSerializableExtra("description");
        id = (String) intent.getSerializableExtra("id");
        imageURL = (String) intent.getSerializableExtra("imageURL");
        restId = (String) intent.getSerializableExtra("restId");
        starCount = (float) intent.getSerializableExtra("starCount");
        title = (String) intent.getSerializableExtra("title");

        titleET = findViewById(R.id.taste_edit_title);
        titleET.setText(title);

        ratingBar = findViewById(R.id.taste_edit_stars);
        ratingBar.setRating(starCount);

        descriptionET = findViewById(R.id.taste_edit_title_description);
        descriptionET.setText(description);

        imageView = findViewById(R.id.taste_edit_img);
        if (imageURL != null) {
            Utils.setImageView(imageView, imageURL);
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ImagePicker.class);
                startActivityForResult(intent, ImagePicker.GET_IMAGE);
            }
        });

        progressBar = findViewById(R.id.taste_edit_progressbar);
        progressBar.setVisibility(View.INVISIBLE);

        final Button button = findViewById(R.id.taste_edit_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);

                if (!validate()) {
                    Log.d(getClass().getName(), "invalid parameters");
                    button.setEnabled(true);
                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }

                description = descriptionET.getText().toString();
                title = titleET.getText().toString();
                starCount = ratingBar.getRating();

                if (image != null) {
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
                    String datetime = dateformat.format(c.getTime());

                    Model.getInstance().saveImage(image, "taste/" + datetime + FirebaseAuth.getInstance().getCurrentUser().getUid(), new Listeners.StatusListener<String>() {
                        @Override
                        public void onComplete(String item) {
                            if (item == null) {
                                Toast.makeText(EditTaste.this, "Failed to save image", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            imageURL = item;
                            setResult(RESULT_OK, null);
                        }
                    });
                }

                final Taste taste = new Taste(id, restId, title, authorId, author, description,
                        starCount, imageURL, date);
                TasteRepository.update(taste);
                finish();
            }
        });
    }


    @Override
    public void finish() {
        progressBar.setVisibility(View.INVISIBLE);
        super.finish();
    }

    private boolean validate() {
        boolean valid = true;

        String title = titleET.getText().toString();

        if (title.isEmpty() || title.length() < 3) {
            titleET.setError("at least 3 characters");
            valid = false;
        } else {
            titleET.setError(null);
        }

        return valid;
    }

    private void saveImage() {
        Utils.saveImage(image, "taste/" + id, new Listeners.StatusListener<String>() {
            @Override
            public void onComplete(String item) {
                if (item == null) {
                    Toast.makeText(getApplicationContext(), "Failed to save image", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImagePicker.GET_IMAGE && resultCode == RESULT_OK) {
            image = BitmapFactory.decodeByteArray(data.getByteArrayExtra("image"), 0,
                    data.getByteArrayExtra("image").length);
            imageView.setImageBitmap(image);
        }
    }
}