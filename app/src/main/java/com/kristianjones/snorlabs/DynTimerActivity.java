package com.kristianjones.snorlabs;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class DynTimerActivity extends AppCompatActivity {

    // Generic tag as Log identifier
    static final String TAG = com.kristianjones.snorlabs.DynTimerActivity.class.getName();

    Bundle bundle;

    Integer hours;
    Integer minutes;

    Intent dynIntent;

    NumberPicker hourPicker;
    NumberPicker minutePicker;

    Spinner settingSpinner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dtimer);

        // Declare all variables
        settingSpinner = findViewById(R.id.optionsSpinner2);
        hourPicker = findViewById(R.id.numberPickerHours);
        minutePicker = findViewById(R.id.numberPickerMins);

        // Set settings array adaptor, linked to the 'settings' string in strings.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.settings, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        //Set spinner to arrayAdaptor
        settingSpinner.setAdapter(adapter);

        //Set numberPicker default values of 8 hours and 0 mins
        hourPicker.setValue(8);
        minutePicker.setValue(0);

        //Set minimum and maximum values for the number and hour picker.
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(11);

        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
    }

    public void setTimer(View view) {
        //Get values from number picker
        hours = hourPicker.getValue();
        minutes = minutePicker.getValue();

        // For debug reasons, send to main activity
        dynIntent = new Intent(getApplicationContext(),AlarmActivity.class);
        bundle = new Bundle();
        bundle.putInt("Hours",hours);
        bundle.putInt("Mins",minutes);

        dynIntent.putExtras(bundle);
        startActivity(dynIntent);
    }
}
