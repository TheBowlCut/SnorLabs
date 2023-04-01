package com.kristianjones.snorlabs;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class AlarmService extends Service {

    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialises media and vibrators
        mediaPlayer = MediaPlayer.create(this,R.raw.bell);
        mediaPlayer.setLooping(true);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public int onStartCommand(Intent intent, int flags, int startId){

        // Sets up notification builder, adds in notification tab
        Log.i("AlarmService","onStartCommand");

        NotificationHelper notificationHelper = new NotificationHelper(this);
        NotificationCompat.Builder nb = notificationHelper.getChannel1Notification("SnorLabs","Alarm Complete");
        notificationHelper.getManager().notify(1,nb.build());

        mediaPlayer.start();

        long[] pattern = { 0, 100, 1000 };
        vibrator.vibrate(pattern,0);

        //Intent to send to CancelActivity.java
        Intent notificationIntent = new Intent(this,CancelActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(notificationIntent);

        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mediaPlayer.stop();
        vibrator.cancel();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
