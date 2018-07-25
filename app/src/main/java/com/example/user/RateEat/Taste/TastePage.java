package com.example.user.RateEat.Taste;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.RateEat.ImagePicker;
import com.example.user.RateEat.Model.Listeners;
import com.example.user.RateEat.Model.Model;
import com.example.user.RateEat.Model.Restaurant;
import com.example.user.RateEat.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Activity for creating a new taste, must be called with restaurant's data.
 */

public class TastePage extends AppCompatActivity{
    private Restaurant currRest;

    private TextView _title;
    private EditTextDatePicker _date;
    private TextView _desc;
    private Button _button;
    private ImageButton _imgButton;
    private RatingBar _ratingBar;
    private ProgressBar _progressBar;

    Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taste_page);

        Intent intent = getIntent();
        currRest = (Restaurant)intent.getSerializableExtra("currRest");

        initView();
    }

    private void initView() {
        _title = (TextView)findViewById(R.id.taste_page_title);

        _progressBar = (ProgressBar)findViewById(R.id.taste_page_progressbar);
        _progressBar.setVisibility(View.INVISIBLE);

        _desc = (TextView)findViewById(R.id.taste_page_title_description);
        _imgButton = (ImageButton)findViewById(R.id.taste_page_img);
        _button = (Button)findViewById(R.id.taste_page_button);
        _button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNewTaste();
            }
        });

        _ratingBar = (RatingBar)findViewById(R.id.taste_page_stars);

        _date = new EditTextDatePicker(TastePage.this, R.id.taste_page_date);

        _imgButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), ImagePicker.class);
            startActivityForResult(intent, ImagePicker.GET_IMAGE);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ImagePicker.GET_IMAGE && resultCode == RESULT_OK) {
            image = BitmapFactory.decodeByteArray(data.getByteArrayExtra("image"),0,
                    data.getByteArrayExtra("image").length);
            _imgButton.setImageBitmap(image);
        }
    }

    private void saveNewTaste() {
        _progressBar.setVisibility(View.VISIBLE);

        if (!validate()) {
            Log.d(getClass().getName(), "invalid parameters");
            _button.setEnabled(true);
            _progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        _button.setEnabled(false);

        if (image != null) {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
            String datetime = dateformat.format(c.getTime());

            Model.getInstance().saveImage(image, "taste/" + datetime + FirebaseAuth.getInstance().getCurrentUser().getUid(), new Listeners.StatusListener<String>() {
                @Override
                public void onComplete(String item) {
                    if (item == null) {
                        Toast.makeText(TastePage.this, "Failed to save image", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Model.getInstance().tasteModel.insert(currRest.id, _title.getText().toString(), _desc.getText().toString(),
                            _ratingBar.getRating(), item, _date.getText());
                    setResult(RESULT_OK, null);
                }
            });
        } else {
            Model.getInstance().tasteModel.insert(currRest.id, _title.getText().toString(), _desc.getText().toString(),
                    _ratingBar.getRating(), null, _date.getText());
            setResult(RESULT_OK, null);
        }

        finish();
    }

    @Override
    public void finish() {
        _progressBar.setVisibility(View.INVISIBLE);
        super.finish();
    }

    private boolean validate() {
        boolean valid = true;

        String title = _title.getText().toString();
        String date  = _date.getText().toString();
        String desc = _desc.getText().toString();

        if (title.isEmpty() || title.length() <= 3) {
            _title.setError("at least 3 characters");
            valid = false;
        } else {
            _title.setError(null);
        }

        return valid;
    }
}