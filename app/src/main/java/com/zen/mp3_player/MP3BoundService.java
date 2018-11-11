package com.zen.mp3_player;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MP3BoundService extends Service{

    private ArrayList songs;
    private int songPosition;

    // create a MP3 player object
    MP3Player player = new MP3Player();
    private final IBinder musicBinder = new MyMusicBinder();

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize song position in list
        songPosition = 0;
    }


    public void setList(ArrayList theSongs) {
        songs = theSongs;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBinder;
    }

    public void setSong(int songIndex) {
        songPosition = songIndex;

        //Toast.makeText(this, "SongPosition: " + songPosition, Toast.LENGTH_SHORT).show();
    }


    public void playSong() {
       Song playSong = (Song) songs.get(songPosition);
       long currentSong = playSong.getId();

        // set Uri
        Uri trackUri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currentSong);

        Uri path = Uri.parse("file:///" + trackUri.getPath());
        String temp = path.toString();
        player.load(temp);
        player.play();

       // Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
    }


    public class MyMusicBinder extends Binder {
        MP3BoundService getService() {
            return MP3BoundService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        // player.release()
        return false;
    }
}

