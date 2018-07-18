package com.example.user.RateEat.Restaurant;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.user.RateEat.Model.Model;
import com.example.user.RateEat.R;

/**
 * Create a new restaurant activity.
 */

public class CreateRest extends Activity {

    private EditText _name;
    private EditText _location;
    private Button _submit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rest_new);

        setWindow();

        _name = (EditText)findViewById(R.id.create_rest_name);
        _location = (EditText)findViewById(R.id.create_rest_address);
        _submit = (Button)findViewById(R.id.create_rest_submit);

        _submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();

            }
        });
    }

    private void submit() {
        if (!validate()) {
            _submit.setEnabled(true);
            return;
        }

        _submit.setEnabled(false);

        Model.getInstance().restaurantModel.insert(_name.getText().toString(), _location.getText().toString());

        finish();
    }

    private void setWindow() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width * 0.8), (int)(height * 0.3));
    }

    public boolean validate() {
        boolean valid = true;

        String name = _name.getText().toString();
        String location = _location.getText().toString();

        if (name.isEmpty()) {
            _name.setError("enter the restaurant title");
            valid = false;
        } else {
            _name.setError(null);
        }

        if (location.isEmpty()) {
            _location.setError("enter an address");
            valid = false;
        } else {
            _location.setError(null);
        }

        return valid;
    }
}
