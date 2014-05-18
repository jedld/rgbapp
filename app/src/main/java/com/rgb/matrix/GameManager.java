package com.rgb.matrix;

import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;

/**
 * Created by joseph on 5/19/14.
 */
public abstract class GameManager implements IOnSceneTouchListener {
    public abstract void show(Scene scene);

    public abstract void hide();

    public abstract void onResumeGame();

    public abstract void onPauseGame();

    public abstract boolean onBackPressed();
}
