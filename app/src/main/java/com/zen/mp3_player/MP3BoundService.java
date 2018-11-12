package com.zen.mp3_player;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import java.io.File;


public class MP3BoundService extends Service {

    // create a MP3 player object
    MP3Player player = new MP3Player();
    private MusicBinder binder  = new MusicBinder();

    Intent notifyIntent;
    NotificationManager notifyManager;
    NotificationCompat.Builder mBuilder;

    String songName;
    private final String TAG = "MP3BoundService";

    public class MusicBinder extends Binder {

        public void load(String filePath) {
            player.load(filePath);
            Log.i(TAG, filePath);
        }

        public void play() {
            player.play();
            Log.i(TAG, "play song");
        }

        public void pause() {
            player.pause();
            Log.i(TAG, "pause song");
        }

        public void stop() {
            player.stop();
            Log.i(TAG, "stop song");
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
    public void onCreate() {
        super.onCreate();

        // create notification when song is playing
        notifyIntent = new Intent(this, MainActivity.class);
        notifyManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        mBuilder = new NotificationCompat.Builder(this)
                .setContentIntent(PendingIntent.getActivity(this, 0, notifyIntent, 0))
                .setSmallIcon(R.drawable.ic_library_music_black_24dp)
                .setContentTitle("MP3 Player")
                .setContentText("Song is playing!");

        notifyManager.notify(1, mBuilder.build());
        Log.i(TAG, "service onCreate");
        Log.i(TAG, "notification created");
    }


    // cancel notification when the music player service is disconnected
    @Override
    public boolean onUnbind(Intent intent) {
        notifyManager.cancel(1);
        player.stop();
        Log.i(TAG, "onUnbind");
        Log.i(TAG, "notification cancelled");
        return true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return binder;
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

