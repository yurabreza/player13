package com.example.yurab.player13;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public final class MainActivity extends Activity implements EventHandler,
        MediaPlayer.OnPreparedListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    public ArrayList<Track> trackList = new ArrayList<>();
    private ImageButton ibPausePlay, ibForward, inPrevious;
    private TextView textView;
    private SeekBar seekBar;
    private MediaPlayer mediaPlayer;
    private int length;
    private AudioManager am;
    private int current;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
        setRecyclerView();

    }

    private void setRecyclerView() {
        //Initializing RecyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        //Creating RecyclerView adapter and putting to it data
        //by loading sort settings from SharedPreferences
        // todo: save shared prefs
        RvAdapter adapter = new RvAdapter(this, getTracks());

        //setting adapter
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initialize() {

        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        length = 0;

        //get track list
        trackList = getTracks();
        Log.d("Yura", String.valueOf(trackList.get(1).getPath()));
        //init
        seekBar = (SeekBar) findViewById((R.id.seekBar_AM));
        ibPausePlay = (ImageButton) findViewById(R.id.ibPausePlay_AM);
        textView = (TextView) findViewById(R.id.twCurTime_AM);

        //attaching listeners
        findViewById(R.id.ibNext_AM).setOnClickListener(this);
        findViewById(R.id.ibPrevious_AM).setOnClickListener(this);
        findViewById(R.id.ibfastforward_AM).setOnClickListener(this);
        findViewById(R.id.ibfastrewind_AM).setOnClickListener(this);
        ibPausePlay.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);

        startService(new Intent(this,PlayerService.class));
    }

    private String formatDuration(long millis) {
        return String.format("%02d:%02d ",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    @Override
    public ArrayList<Track> getTracks() {
        int counter = 0;
        ArrayList<Track> trackList1 = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor == null) {
            Log.d("Yura", " query failed, handle error.");
            return null;

        } else if (!cursor.moveToFirst()) {
            Log.d("Yura", "no media on the device");
        } else {
            int titleColumn = cursor
                    .getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistColumn = cursor
                    .getColumnIndex(MediaStore.Audio.Media.ARTIST);

            int audioDurationColumn = cursor
                    .getColumnIndex(MediaStore.Audio.Media.DURATION);
            int pathColumn = cursor
                    .getColumnIndex(MediaStore.Audio.Media.DATA);


            do {
                int thisId = counter;
                String thisTitle = cursor.getString(titleColumn);
                String thisArtist = cursor.getString(artistColumn);
                String thisDuration = formatDuration(cursor.getLong(audioDurationColumn));
                String thisPath = cursor.getString(pathColumn);
                //Creating new Track
                Track track = new Track(thisId, thisTitle, thisArtist, thisDuration, thisPath);
                counter++;
                //Adding to trackList
                trackList1.add(track);
                Log.d("Yura", ".process entry..." + String.valueOf(counter));
            } while (cursor.moveToNext());
        }
        return trackList1;
    }

    @Override
    public void play(int id) {
        //todo correct duration
        textView.setText(trackList.get(id).getDuration());
        current = id;
        releaseMP();
        try {//create mediaPlayer
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(trackList.get(id).getPath());
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepare();
            mediaPlayer.start();

            setBackground(ibPausePlay, ContextCompat.getDrawable(this, R.drawable.ic_pause));
        } catch (IOException e) {
            e.printStackTrace();

        }

    }


    private void changePlayPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) pausePlayer();
            else startPlayer();
        } else play(0);

    }

    private void pausePlayer() {
        setBackground(ibPausePlay, ContextCompat.getDrawable(this, R.drawable.ic_play));

        length = mediaPlayer.getCurrentPosition();
        mediaPlayer.pause();


    }

    private void startPlayer() {

        setBackground(ibPausePlay, ContextCompat.getDrawable(this, R.drawable.ic_pause));
        if (!mediaPlayer.isPlaying()) {
            if (length > 0) {
                mediaPlayer.seekTo(length);
                mediaPlayer.start();
            }

        }
    }


    private void setBackground(ImageButton ibPausePlay, Drawable drawable) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            ibPausePlay.setBackgroundDrawable(drawable);
        } else {
            ibPausePlay.setBackground(drawable);
        }
    }


    @Override
    public void onClick(View v) {


        switch (v.getId()) {
            case (R.id.ibNext_AM):
                int next = current + 1;
                play(next);
                break;
            case (R.id.ibPrevious_AM):
                int previous = current - 1;
                play(previous);
                break;
            case (R.id.ibPausePlay_AM):

                changePlayPause();
                break;
            case (R.id.ibfastforward_AM):
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 3000);
                break;
            case (R.id.ibfastrewind_AM):
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 3000);
                break;
        }
    }

    private void releaseMP() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMP();
    }
}
