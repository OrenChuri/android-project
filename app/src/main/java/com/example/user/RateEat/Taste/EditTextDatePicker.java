package com.example.user.RateEat.Taste;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * EditText opens a DatePicker and shows it's results.
 */

public class EditTextDatePicker  implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
    EditText _editText;
    private int _day;
    private int _month;
    private int _birthYear;
    private Context _context;

    private Calendar calendar;

    public EditTextDatePicker(Context context, int editTextViewID)
    {
        Activity act = (Activity)context;
        _editText = (EditText)act.findViewById(editTextViewID);
        _editText.setOnClickListener(this);
        _context = context;


        calendar = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy");
        _editText.setText(dateformat.format(calendar.getTime()));

    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        _birthYear = year;
        _month = monthOfYear;
        _day = dayOfMonth;
        updateDisplay();
    }

    @Override
    public void onClick(View v) {
        DatePickerDialog dialog = new DatePickerDialog(_context, this,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();

    }

    // updates the date in the birth date EditText
    private void updateDisplay() {
        // Month is 0 based so add 1
        _editText.setText(String.format("%02d/%02d/%d", _day, _month + 1, _birthYear));
    }

    public String getText() {
        return _editText.getText().toString();
    }
}