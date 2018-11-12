package com.zen.mp3_player;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.zen.mp3_player.MP3BoundService.MusicBinder;
import java.io.File;

/* This is the main activity for the app, all services are included in a file called
    MP3BoundService, which uses MP3Player functions to operate.

    All codes for extracting and displaying the songs are here in this file
 */
public class MainActivity extends AppCompatActivity {

    private static final int READ_PERMISSION_REQUEST = 1;

    private File songList[];
    private File selectedFromList;

    private ListView songView;

    // Create an object for MP3BoundService
    MusicBinder binder;
    private ServiceConnection musicServiceConnect = null;
    private Intent playIntent;
    boolean isBound = false;

    private TextView musicInfo;
    private SeekBar musicProcess;
    private int songIndex = 0;
    public String songName;
    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        musicInfo = (TextView) findViewById(R.id.musicInfo);
        musicProcess = (SeekBar) findViewById(R.id.musicProcess);
        playIntent = new Intent(this, MP3BoundService.class);

        // make sure the permission request is granted when the app is launched
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, READ_PERMISSION_REQUEST);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, READ_PERMISSION_REQUEST);
            }
        } else {

            trackSeekBar();
            showMusic();
        }
    }

    // show all music in the ListView
    public void showMusic() {
        songView = (ListView) findViewById(R.id.songView);
        getMusic();
    }

    // get all info of the music available on the device
    public void getMusic() {
        File musicDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Music/");
        songList = musicDir.listFiles();
        songView.setAdapter(new ArrayAdapter<File>(this, android.R.layout.simple_list_item_1, songList));

        songView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedFromList =(File) (songView.getItemAtPosition(position));
                getSongPosition();
                checkServiceConnection();
            }
        });
    }

    // Check whether services are binded successfully
    private void checkServiceConnection() {
        if (musicServiceConnect != null) unbindService(musicServiceConnect);

        musicServiceConnect = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder = (MP3BoundService.MusicBinder) service;
                binder.load(selectedFromList.getAbsolutePath());
                // service is connected
                isBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                // service disconnected
                isBound = false;
            }
        };

        bindService(playIntent, musicServiceConnect, Service.BIND_AUTO_CREATE);
        musicProcess.postDelayed(updateInfoPerSec, 500);
    }


    // if permission is not granted, no songs will be shown
    public void onRequestPermissionsResult(int requestcode, String[] permission, int[] grantResults) {
        switch (requestcode) {
            case READ_PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission is granted", Toast.LENGTH_SHORT).show();

                        trackSeekBar();
                        showMusic();
                    }
                } else {
                    // song list will be not be shown
                    Toast.makeText(this, "No Permission granted", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }



    // get the current index of the song in the Array
    private void getSongPosition() {
        for(int i = 0; i < songList.length; i++) {
            if(songList[i].getAbsolutePath().equals(selectedFromList.getAbsolutePath()))
                 songIndex = i;
        }
    }

    // Basic operations of MP3 player //
    //-----------------------------------
    public void play_song(View v) {
        if(binder != null) {
            binder.play();
        }
    }

    public void pause_song(View v) {
        if(binder != null)
            binder.pause();
    }

    public void stop_song(View v) {
        if(binder != null) {
            binder.stop();
            musicProcess.setProgress(0);
            musicInfo.setText("");
        }
    }

    public void prev_song(View v) {
        if(binder != null) {
            binder.stop();
            songIndex = binder.getPrevOrNext(songList, songIndex, "prev");
            binder.load(songList[songIndex].getAbsolutePath());
            musicProcess.postDelayed(updateInfoPerSec, 500);
        }
    }

    public void next_song(View v) {
        if(binder != null) {
            binder.stop();
            songIndex = binder.getPrevOrNext(songList, songIndex, "next");
            binder.load(songList[songIndex].getAbsolutePath());
            musicProcess.postDelayed(updateInfoPerSec, 500);
        }
    }

    //---------------------------------------------------

    private Runnable updateInfoPerSec = new Runnable() {

        @Override
        public void run() {
            if(musicProcess != null) {
                if(binder.getDuration() != 0) {
                    musicProcess.setProgress(binder.getProgress() * musicProcess.getMax() / binder.getDuration());
                    setMusicInfo(translateTimeFormat(binder.getProgress()), translateTimeFormat(binder.getDuration()));
                }

                if(binder.getState().equals(MP3Player.MP3PlayerState.PLAYING))
                    musicProcess.postDelayed(updateInfoPerSec, 500);
            }
        }
    };

    private void setMusicInfo(String songProgress, String songDuration) {

        songName = binder.setPlayName(binder.getFilePath());
        musicInfo.setText("Playing now: " + songName + "\n"
                        + "Time: " + songProgress + " / " + songDuration);
        }

    private String translateTimeFormat(long msec) {

        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (msec / (1000 * 60 * 60));
        int minutes = (int) (msec % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((msec % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    private void trackSeekBar() {

        musicProcess.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                binder.goTo(binder.getDuration() * musicProcess.getProgress() / seekBar.getMax());
            }
        });
    }

    @Override
    protected void onDestroy() {
        unbindService(musicServiceConnect);
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    public String getSongNameNotif() {
        return songName;
    }

}
