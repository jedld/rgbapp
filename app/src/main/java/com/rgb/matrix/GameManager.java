package com.rgb.matrix;

import android.media.MediaPlayer;

import com.dayosoft.tiletron.app.MainActivity;

import org.andengine.audio.music.Music;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joseph on 5/19/14.
 */
public abstract class GameManager implements IOnSceneTouchListener {
    protected final MainActivity context;
    int currentMusicTrack = 0;
    private List<Music> trackList = new ArrayList<Music>();

    public GameManager(MainActivity context) {
        this.context = context;
    }

    public abstract void show(Scene scene);

    public void setMusic(ArrayList<Music> musicTracks) {
        for (Music music : musicTracks) {
            MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    nextTrack();
                    currentTrack().play();
                }
            };
            music.setOnCompletionListener(listener);
            trackList.add(music);
        }
    }

    public void toggleMusic(boolean state) {
        Utils.saveMusicState(context, state);
        if (currentTrack() != null) {
            if (!state && currentTrack().isPlaying()) {
                currentTrack().pause();
            } else if (state && !currentTrack().isPlaying()) {
                currentTrack().play();
            }
        }
    }

    public void toggleSounds(boolean state) {
        Utils.saveSoundState(context, state);
    }

    public boolean getMusicState() {
        return Utils.getMusicState(context);
    }

    public boolean getSoundState() {
        return Utils.getSoundState(context);
    }

    protected void startMusic() {
        if (getMusicState() && currentTrack() != null && !currentTrack().isPlaying()) {
            currentTrack().play();
        }
    }

    protected void stopMusic() {
        if (getMusicState() && currentTrack() != null && currentTrack().isPlaying()) {
            currentTrack().pause();
        }
    }

    public abstract void hide();

    public abstract void onResumeGame();

    public abstract void onPauseGame();

    public abstract boolean onBackPressed();

    protected Music currentTrack() {
        return trackList.get(currentMusicTrack);
    }

    private void nextTrack() {
        currentMusicTrack++;
        if (currentMusicTrack >= trackList.size()) currentMusicTrack = 0;
    }
}
