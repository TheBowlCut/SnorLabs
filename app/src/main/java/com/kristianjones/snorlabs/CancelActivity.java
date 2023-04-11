package com.kristianjones.snorlabs;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

import static android.app.PendingIntent.FLAG_MUTABLE;

public class CancelActivity extends AppCompatActivity {

    // Generic tag as Log identifier
    final String TAG = SleepActivity.class.getName();

    Button snoozeButton;
    Button finishButton;

    Integer hourNow;
    Integer minutesNow;
    Integer secondNow;

    TextView descTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel);

        finishButton = findViewById(R.id.finishButton);
        snoozeButton = findViewById(R.id.snoozeButton);
        descTextView = findViewById(R.id.descTextView4);

    }

    public void finishButton (View view) {
        finishAlarm();

        // Once service is shut down, the app will return to main activity.
        Intent cancelIntent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(cancelIntent);
    }

    public void finishAlarm() {
        // Cancels the countdown service
        try {
            Intent countdownIntent = new Intent(getApplicationContext(), CountdownService.class);
            getApplicationContext().stopService(countdownIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Cancels the regular alarm service. OnDestroy, the service will shut down.
        Intent intentService = new Intent(getApplicationContext(),AlarmService.class);
        getApplicationContext().stopService(intentService);

        // To make sure everything is cancelled, set up alarmManager, intent and pendingIntent identical to StartAlarm, in order to cancel it.
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent alertIntent = new Intent(getApplicationContext(), AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, alertIntent,
                PendingIntent.FLAG_CANCEL_CURRENT | FLAG_MUTABLE);
        alarmManager.cancel(pendingIntent);

        // Close notification
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);

    }

    @RequiresApi(api = 31)
    public void snoozeAlarm (View view) {
        // When pressed, snooze alarm will:
        // In debug mode, set a 10s snooze timer from current time
        // Not in dubeg mode, set a 10 minute timer.

        // Set description view to notify user alarm is snoozed.
        descTextView.setText(R.string.snoozeTextView);

        // First we have to cancel everything to make sure both alarms don't go off.
        // Cancels the countdown service
        try {
            Intent countdownIntent = new Intent(getApplicationContext(), CountdownService.class);
            getApplicationContext().stopService(countdownIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Cancels the service. OnDestroy, the service will shut down.
        Intent intentService = new Intent(getApplicationContext(),AlarmService.class);
        getApplicationContext().stopService(intentService);

        // To make sure everything is cancelled, set up alarmManager, intent and pendingIntent identical to StartAlarm, in order to cancel it.
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent alertIntent = new Intent(getApplicationContext(), AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, alertIntent,
                PendingIntent.FLAG_CANCEL_CURRENT | FLAG_MUTABLE);
        alarmManager.cancel(pendingIntent);

        // SNOOZE FUNCTION
        // Get the current time.
        Calendar calendar = Calendar.getInstance();

        hourNow = calendar.get(android.icu.util.Calendar.HOUR_OF_DAY);
        minutesNow = calendar.get(android.icu.util.Calendar.MINUTE);
        secondNow = calendar.get(android.icu.util.Calendar.SECOND);

        // Set the 10 second alarm (Initially for debug)
        secondNow = secondNow + 600;

        //Start new alarm with 10 min timer.
        StartAlarm alarm = new StartAlarm(getApplicationContext(),hourNow,minutesNow,secondNow);

        Toast.makeText(CancelActivity.this,R.string.snoozeToast,Toast.LENGTH_LONG).show();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAlarm();

        // Once service is shut down, the app will return to main activity.
        Intent cancelIntent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(cancelIntent);
    }
}
