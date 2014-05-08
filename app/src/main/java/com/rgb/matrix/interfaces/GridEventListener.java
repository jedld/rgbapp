package com.rgb.matrix.interfaces;

import com.rgb.matrix.GameOver;
import com.rgb.matrix.MainGrid;
import com.rgb.matrix.NextObject;
import com.rgb.matrix.menu.MenuItem;

import org.andengine.entity.util.ScreenCapture;

import java.util.Vector;

/**
 * Created by joseph on 5/2/14.
 */
public interface GridEventListener {
    void toggleMusic(boolean state);

    void toggleSounds(boolean state);

    boolean getMusicState();

    boolean getSoundState();

    public void onScreenCaptureHighScore(GameOver gameOverText, ScreenCapture screenCapture);

    void onExitGrid(MenuItem item);

    void onSetupWorld(MainGrid mainGrid);

    void populateQueue(Vector<NextObject> blockQueue);

    void onRestart(MenuItem item);
}
