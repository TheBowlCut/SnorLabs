package com.kristianjones.snorlabs;

import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class AlarmActivity extends AppCompatActivity {

    // Generic tag as Log identifier
    static final String TAG = com.kristianjones.snorlabs.AlarmActivity.class.getName();

    Bundle bundle;
    Bundle finalBundle;

    Calendar calendar;

    Integer hourNow;
    Integer minutesNow;
    Integer alarmHour;
    Integer alarmMinute;
    Integer timerHour;
    Integer timerMinute;

    Intent dynIntent;
    Intent alarmIntent;

    Spinner settingSpinner;

    TimePicker timePicker;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        // Declare all variables
        settingSpinner = findViewById(R.id.optionsSpinner3);
        timePicker = findViewById(R.id.timePicker);

        // Set settings array adaptor, linked to the 'settings' string in strings.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.settings, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        //Set spinner to arrayAdaptor
        settingSpinner.setAdapter(adapter);

        // Check android build version code for minimum version
        // Set alarm value to current value.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            calendar = Calendar.getInstance();
            hourNow = calendar.get(Calendar.HOUR_OF_DAY);
            minutesNow = calendar.get(Calendar.MINUTE);
        }

        timePicker.setHour(hourNow);
        timePicker.setMinute(minutesNow);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setAlarm (View view) {

        // Pull timer values from bundle for next intent.
        dynIntent = getIntent();
        bundle = dynIntent.getExtras();

        timerHour = bundle.getInt("Hours");
        timerMinute = bundle.getInt("Mins");

        // Looking at timePicker values selected by user
        alarmHour = timePicker.getHour();
        alarmMinute = timePicker.getMinute();

        // Convert TimePicker values into normal integers
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            calendar.set(Calendar.HOUR_OF_DAY,alarmHour);
            calendar.set(Calendar.MINUTE,alarmMinute);
            calendar.set(Calendar.SECOND,0);
        }

        alarmIntent = new Intent(getApplicationContext(),SleepActivity.class);

        finalBundle = new Bundle();

        // Put all integers for timer and alarm into single bundle.
        finalBundle.putInt("timerH",timerHour);
        finalBundle.putInt("timerM",timerMinute);
        finalBundle.putInt("alarmH",alarmHour);
        finalBundle.putInt("alarmM",alarmMinute);

        Log.d(TAG,"timerHours = " + timerHour);

        alarmIntent.putExtras(finalBundle);
        startActivity(alarmIntent);

    }
}

