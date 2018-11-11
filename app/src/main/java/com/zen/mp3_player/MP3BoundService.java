package com.zen.mp3_player;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class MP3BoundService extends Service {

    // create a MP3 player object
    MP3Player player;
    private MusicBinder binder  = new MusicBinder();

    Intent notifyIntent;
    NotificationManager notifyManager;
    NotificationCompat.Builder notifyBuilder;

    private final String TAG = "music service";
    String songName;

    public class MusicBinder extends Binder {

        public void load(String filePath) {
            player.load(filePath);
            Log.i(TAG, filePath);
        }

        public void play() {
            player.play();
            Log.i(TAG, "play music");
        }

        public void pause() {
            player.pause();
            Log.i(TAG, "pause music");
        }

        public void stop() {
            player.stop();
            Log.i(TAG, "stop music");
        }

        public int getDuration() {
            return player.getDuration();
        }

        public int getProgress() {
            return player.getProgress();
        }

        public String getFilePath() {
            return player.getFilePath();
        }

        public MP3Player.MP3PlayerState getState() {
            return player.getState();
        }

        public void goTo(int seconds) {
            player.goTo(seconds);
        }

        public String setPlayName(String filePath) {

            int indexLastSlash = filePath.lastIndexOf("/");
            int indexLastDot = filePath.lastIndexOf(".");
            songName = filePath.substring(indexLastSlash + 1, indexLastDot);
            return songName;
        }

        public int getPrevOrNext(File list[], int songIndex, String prevOrNext) {

            int sumSongs = list.length;
            Log.i(TAG, "current song is " + list[songIndex]);

            if (prevOrNext.equals("prev")) {
                if ((songIndex - 1) < 0) songIndex = sumSongs - 1;
                else songIndex -= 1;
            } else {
                if ((songIndex + 1) > (sumSongs - 1)) songIndex = 0;
                else songIndex += 1;
            }
            return songIndex;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {

        Log.i(TAG, "onBind");
        return binder;
    }

    @Override
    public void onCreate() {

        super.onCreate();
        player = new MP3Player();

        notifyIntent = new Intent(this, MainActivity.class);
        notifyManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        notifyBuilder = new NotificationCompat.Builder(this)
                .setContentIntent(PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Song Playing now:")
                .setContentText("running");

        notifyManager.notify(1, notifyBuilder.build());
        Log.i(TAG, "service onCreate");
        Log.i(TAG, "notification created");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        notifyManager.cancel(1);
        player.stop();
        Log.i(TAG, "onUnbind");
        Log.i(TAG, "notification cancelled");
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG, "onRebind");
        super.onRebind(intent);
    }
}


//    @Override
//    public void onCreate() {
//    }
//
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return musicBinder;
//    }
//
////    public void setSong(int songIndex) {
////        songPosition = songIndex;
////
////        //Toast.makeText(this, "SongPosition: " + songPosition, Toast.LENGTH_SHORT).show();
////    }
//
//
//    public void playSong(String filepath) {
//
//        Toast.makeText(this, filepath, Toast.LENGTH_SHORT).show();
//
//       player.load(filepath);
//       player.play();
//    }
//
//
//
//
//    public class MyMusicBinder extends Binder {
//        MP3BoundService getService() {
//            return MP3BoundService.this;
//        }
//    }
//
//    @Override
//    public boolean onUnbind(Intent intent) {
//        player.stop();
//        // player.release()
//        return false;
//    }

