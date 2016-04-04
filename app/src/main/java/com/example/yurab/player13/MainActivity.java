package com.example.yurab.player13;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public final class MainActivity extends Activity implements EventHandler, View.OnClickListener, SeekBar.OnSeekBarChangeListener, MediaPlayer.OnCompletionListener {
    public ArrayList<Track> trackList = new ArrayList<>();
    private ImageButton ibPausePlay;
    private TextView textViewDuration;
    private SeekBar seekBar;

    private int length;

    private int current;
    private Intent intent;
    private ServiceConnection sConn;
    private PlayerService playerService;
    private boolean bound = false;
    private LinearLayoutManager linearLayoutManager;
    private BroadcastReceiver receive;
    private SharedPreferences sPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
        setRecyclerView();
        bindPlayerService();


        receive = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                unbindService(sConn);
                finish();
            }
        };

        IntentFilter filter = new IntentFilter(Constants.ACTION.KILL);
        this.registerReceiver(receive, filter);
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    private void initSeekbar() {

        final Handler mHandler = new Handler();
        //Make sure you update Seekbar on UI thread
        MainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (playerService.mediaPlayer != null) {
                    int mCurrentPosition = playerService.mediaPlayer.getCurrentPosition();

                    seekBar.setProgress(mCurrentPosition);


                    textViewDuration.setText(formatDuration(trackList.get(current).getDuration() - mCurrentPosition));
                }
                mHandler.postDelayed(this, 1000);
            }
        });
    }

    private void initService() {
        playerService.initTrackList(trackList);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(intent, sConn, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!bound) return;
        unbindService(sConn);
        bound = false;
    }


    private void bindPlayerService() {
        intent = new Intent(this, PlayerService.class);
        startService(intent);
        sConn = new ServiceConnection() {

            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.d("Yura", "MainActivity onServiceConnected");
                playerService = ((PlayerService.MyBinder) binder).getService();
                bound = true;
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.d("Yura", "MainActivity onServiceDisconnected");
                bound = false;
            }
        };
    }

    private void setRecyclerView() {
        //Initializing RecyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        //Creating RecyclerView adapter and putting to it data
        //by loading sort settings from SharedPreferences

        RvAdapter adapter = new RvAdapter(this, trackList);

        //setting adapter
        recyclerView.setAdapter(adapter);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);


    }

    private void setRecyclerViewPlaying(int position) {
        int wantedPosition = position;


        int firstPosition = linearLayoutManager.findFirstVisibleItemPosition();
        int wantedChild = wantedPosition - firstPosition;

        if (wantedChild < 0 || wantedChild >= linearLayoutManager.getChildCount()) {
            Log.w("Yura", "Unable to get view for desired position, because it's not being displayed on screen.");
            return;
        }

        View wantedView = linearLayoutManager.getChildAt(wantedChild);
        ImageView imageButton = (ImageView) wantedView.findViewById(R.id.pausePlay_TCV);

        imageButton.setVisibility(View.VISIBLE);
        linearLayoutManager.scrollToPosition(position);

    }

    private void setRecyclerViewNotPlaying(int position) {
        int wantedPosition = position; // Whatever position you're looking for
        int firstPosition = linearLayoutManager.findFirstVisibleItemPosition();
        int wantedChild = wantedPosition - firstPosition;

        if (wantedChild < 0 || wantedChild >= linearLayoutManager.getChildCount()) {
            Log.w("Yura", "Unable to get view for desired position, because it's not being displayed on screen.");
            return;
        }

        View wantedView = linearLayoutManager.getChildAt(wantedChild);
        ImageView imageButton = (ImageView) wantedView.findViewById(R.id.pausePlay_TCV);

        imageButton.setVisibility(View.GONE);


    }

    private void initialize() {


        length = 0;

        //get track list
        trackList = getTracks();
        Log.d("Yura", String.valueOf(trackList.get(1).getPath()));
        //init
        seekBar = (SeekBar) findViewById((R.id.seekBar_AM));
        ibPausePlay = (ImageButton) findViewById(R.id.ibPausePlay_AM);
        textViewDuration = (TextView) findViewById(R.id.twCurTime_AM);

        //attaching listeners
        findViewById(R.id.ibNext_AM).setOnClickListener(this);
        findViewById(R.id.ibPrevious_AM).setOnClickListener(this);
        findViewById(R.id.ibfastforward_AM).setOnClickListener(this);
        findViewById(R.id.ibfastrewind_AM).setOnClickListener(this);
        ibPausePlay.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);


    }

    private String formatDuration(int millis) {
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
                int thisDuration = (int) cursor.getLong(audioDurationColumn);
                String thisPath = cursor.getString(pathColumn);
                //Creating new Track
                Track track = new Track(thisId, thisTitle, thisArtist, thisDuration, thisPath);
                counter++;
                //Adding to trackList
                trackList1.add(track);
                Log.d("Yura", ".process entry..." + String.valueOf(counter));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return trackList1;
    }

    @Override
    public void play(int id) {

        if (playerService.mediaPlayer != null)
            setRecyclerViewNotPlaying(current);
        setRecyclerViewPlaying(id);
        initService();
        initSeekbar();
        //setting notification title& artist
        playerService.setSong(trackList.get(id).getTitle(), trackList.get(id).getArtist());
        Log.d("Yura", "play" + trackList.get(id).getTitle());
        textViewDuration.setText(formatDuration(trackList.get(id).getDuration()));
        current = id;

        if (playerService.play(id)) {
            setBackground(ibPausePlay, ContextCompat.getDrawable(this, R.drawable.ic_pause));
            seekBar.setMax(playerService.mediaPlayer.getDuration());
            playerService.mediaPlayer.setOnCompletionListener(this);
            Log.d("yura", String.valueOf(playerService.mediaPlayer.getDuration()));

        }
    }


    private void changePlayPause() {
        if (playerService.mediaPlayer != null) {
            if (playerService.mediaPlayer.isPlaying()) pausePlayer();
            else startPlayer();
        } else play(0);

    }

    private void pausePlayer() {
        setBackground(ibPausePlay, ContextCompat.getDrawable(this, R.drawable.ic_play));

        length = playerService.mediaPlayer.getCurrentPosition();
        playerService.mediaPlayer.pause();


    }

    private void startPlayer() {

        setBackground(ibPausePlay, ContextCompat.getDrawable(this, R.drawable.ic_pause));
        if (!playerService.mediaPlayer.isPlaying()) {
            if (length > 0) {
                playerService.mediaPlayer.seekTo(length);
                playerService.mediaPlayer.start();
            }

        }
    }


    private void setBackground(View ibPausePlay, Drawable drawable) {
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

                play(playerService.next());
                break;
            case (R.id.ibPrevious_AM):

                play(playerService.previous());
                break;
            case (R.id.ibPausePlay_AM):

                changePlayPause();
                break;
            case (R.id.ibfastforward_AM):
                playerService.mediaPlayer.seekTo(playerService.mediaPlayer.getCurrentPosition() + 3000);
                break;
            case (R.id.ibfastrewind_AM):
                playerService.mediaPlayer.seekTo(playerService.mediaPlayer.getCurrentPosition() - 3000);
                break;
        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if (playerService.mediaPlayer != null && fromUser) {
            playerService.mediaPlayer.seekTo(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(receive);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        play(playerService.next());
    }


}
