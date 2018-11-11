package com.zen.mp3_player;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IllegalFormatCodePointException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int READ_PERMISSION_REQUEST = 1;
    private static final int MY_PERMISSION_CODE = 2;

    private ArrayList<Song> songList;
    private ListView songView;

    // Create an object for MP3BoundService
    private MP3BoundService musicService;
    private Intent playIntent;
    boolean isBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        songView = (ListView) findViewById(R.id.songList);

        // make sure the permission request is granted
        // Requests permission for devices with versions Marshmallow (M)/API 23 or above.
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
            songList = new ArrayList<Song>();

            getMusic();
            sortMusic();
        }

        SongAdapter songAdapter = new SongAdapter(this, songList);
        songView.setAdapter(songAdapter);

        Button endBtn = (Button) findViewById(R.id.action_end);
        endBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(playIntent);
                musicService=null;
                System.exit(0);
            }
        });
    }

    @Override
    // start the service on start
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MP3BoundService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    // get all info of the music available on the device
    public void getMusic() {
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri, null, null, null, null, null);

        if (songCursor != null && songCursor.moveToFirst()) {
            int idColumn = songCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

            do {
                long currentID = songCursor.getLong(idColumn);
                String currentTitle = songCursor.getString(songTitle);
                String currentArtist = songCursor.getString(songArtist);
                songList.add(new Song(currentID, currentTitle, currentArtist));
            } while (songCursor.moveToNext());
        }
    }

    // sort the music
    public void sortMusic() {
        Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song first, Song second) {
                return first.getTitle().compareTo(second.getTitle());
            }
        });
    }

    public void onRequestPermissionsResult(int requestcode, String[] permission, int[] grantResults) {
        switch (requestcode) {
            case READ_PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission is granted", Toast.LENGTH_SHORT).show();

                        songList = new ArrayList<Song>();

                        getMusic();
                        sortMusic();
                    }
                } else {
                    Toast.makeText(this, "No Permission granted", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }


    public void songPicked(View view) {
           musicService.setSong(Integer.parseInt(view.getTag().toString()));
           musicService.playSong();
    }


    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicService=null;
        super.onDestroy();
    }

    // Check whether services are binded successfully
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MP3BoundService.MyMusicBinder binder = (MP3BoundService.MyMusicBinder) service;
            musicService = binder.getService();

            //pass song list
            musicService.setList(songList);
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };
}
