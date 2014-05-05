package com.rgb.matrix.interfaces;

import com.rgb.matrix.menu.MenuItem;

import org.andengine.entity.util.ScreenCapture;

/**
 * Created by joseph on 5/2/14.
 */
public interface GridEventListener {
    void toggleMusic(boolean state);

    void toggleSounds(boolean state);

    boolean getMusicState();

    boolean getSoundState();

    public void onScreenCaptureHighScore(ScreenCapture screenCapture);

    void onExitGrid(MenuItem item);
}
