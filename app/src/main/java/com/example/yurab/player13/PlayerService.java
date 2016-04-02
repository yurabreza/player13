package com.example.yurab.player13;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by yurab on 01.04.2016.
 */
public class PlayerService extends Service {
    private String LOG_TAG = "Yura";
    public ArrayList<Track> trackList = new ArrayList<>();
    MediaPlayer mediaPlayer;
    private int current;
    Context context;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        showNotification();
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();

        if (intent.getAction() != null && intent.getAction().equals(
                Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();

            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    private void showNotification() {
        RemoteViews views = new RemoteViews(getPackageName(),
                R.layout.status_bar);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);


        Intent closeIntent = new Intent(this, PlayerService.class);
        closeIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0);
        views.setOnClickPendingIntent(R.id.imgBtn_close_SB, pcloseIntent);

        views.setTextViewText(R.id.twTitle_SB, "Song Title");

        views.setTextViewText(R.id.twArtistSB, "Artist Name");

        Notification status;
        status = new Notification.Builder(this).build();
        status.contentView = views;

        status.flags = Notification.FLAG_ONGOING_EVENT;
        status.icon = R.drawable.ic_play;
        status.contentIntent = pendingIntent;

        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);


    }



}
