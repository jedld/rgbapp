package com.dayosoft.tiletron.app;

import android.content.Context;

import com.rgb.matrix.Utils;

import org.andengine.audio.sound.Sound;

/**
 * Created by joseph on 5/3/14.
 */
public class SoundWrapper {
    private final Context context;
    private final Sound sound;

    public SoundWrapper(Context context, Sound sound) {
        this.context = context;
        this.sound = sound;
    }

    public void play() {
        if (Utils.getSoundState(context)) {
            sound.play();
        }
    }

    public void stop() {
        sound.stop();
    }
}
