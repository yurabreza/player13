package com.example.yurab.player13;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.ArrayList;

import static com.example.yurab.player13.R.drawable.ic_player1;

/**
 * Created by yurab on 01.04.2016.
 */
public final class PlayerService extends Service implements MediaPlayer.OnCompletionListener {
    private String LOG_TAG = "Yura";
    public ArrayList<Track> trackList = new ArrayList<>();
    public MediaPlayer mediaPlayer;
    public int current;

    private MyBinder binder = new MyBinder();
    private RemoteViews views;




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseMP();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        play(next());
    }


    class MyBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction() != null && intent.getAction().equals(
                Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            stopForeground(true);
            stopSelf();

            Intent in = new Intent(Constants.ACTION.KILL);
            sendBroadcast(in);

        }
        return START_STICKY;
    }

    private void showNotification(String title, String artist) {
        views = new RemoteViews(getPackageName(), R.layout.status_bar);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        Intent closeIntent = new Intent(this, PlayerService.class);
        closeIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0);

        views.setOnClickPendingIntent(R.id.imgBtn_close_SB, pcloseIntent);

        views.setTextViewText(R.id.twTitle_SB, title);

        views.setTextViewText(R.id.twArtistSB, artist);

        Notification status = buildNotif();

        status.contentView = views;

        status.flags = Notification.FLAG_ONGOING_EVENT;
        status.icon = ic_player1;
        status.contentIntent = pendingIntent;


        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);


    }

    private Notification buildNotif() {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            return new Notification.Builder(this).getNotification();
        } else {
            return new Notification.Builder(this).build();
        }

    }

    public void setSong(String _title, String _artist) {

        showNotification(_title, _artist);


    }

    public void initTrackList(ArrayList<Track> tracks) {

        trackList = tracks;

    }

    public void releaseMP() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean play(int id) {
        current = id;
        releaseMP();

        try {
            //create mediaPlayer
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(trackList.get(id).getPath());
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(this);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;

        }
    }

    public int next() {
        int next = current + 1;
        if (next == trackList.size())
            next = 0;
        return next;
    }

    public int previous() {
        int previous = current - 1;
        if (previous < 0)
            previous = trackList.size() - 1;
        return previous;
    }
}
