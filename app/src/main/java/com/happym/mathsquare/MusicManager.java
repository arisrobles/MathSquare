package com.happym.mathsquare;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicManager {
    private static MediaPlayer bgMediaPlayer;

    public static void playBGGame(Context context, String fileName) {
        if (bgMediaPlayer == null) { // Prevent multiple instances
            try {
                AssetFileDescriptor afd = context.getAssets().openFd(fileName);
                bgMediaPlayer = new MediaPlayer();
                bgMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                bgMediaPlayer.setLooping(true); // ✅ Ensure continuous play
                bgMediaPlayer.prepare();
                
                bgMediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void pause() {
        if (bgMediaPlayer != null && bgMediaPlayer.isPlaying()) {
            bgMediaPlayer.pause();
        }
    }

    public static void resume() {
        if (bgMediaPlayer != null && !bgMediaPlayer.isPlaying()) {
            bgMediaPlayer.start();
        }
    }

    public static void stop() {
        if (bgMediaPlayer != null) {
            bgMediaPlayer.stop();
            bgMediaPlayer.release();
            bgMediaPlayer = null;
        }
    }
}
