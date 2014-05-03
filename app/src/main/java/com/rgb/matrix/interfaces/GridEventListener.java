package com.rgb.matrix.interfaces;

/**
 * Created by joseph on 5/2/14.
 */
public interface GridEventListener {
    void toggleMusic(boolean state);

    void toggleSounds(boolean state);

    boolean getMusicState();

    boolean getSoundState();
}
