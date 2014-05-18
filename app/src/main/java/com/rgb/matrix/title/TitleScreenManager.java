package com.rgb.matrix.title;

import com.dayosoft.tiletron.app.MainActivity;
import com.rgb.matrix.GameManager;
import com.rgb.matrix.menu.MenuItem;
import com.rgb.matrix.menu.OnMenuSelectedListener;

import org.andengine.entity.scene.Scene;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import java.util.List;

/**
 * Created by joseph on 5/19/14.
 */
public class TitleScreenManager extends GameManager {

    private final TitleScreen titleScreen;

    public TitleScreenManager(final MainActivity activity, float pX, float pY, float width, float height, List<String> lines, VertexBufferObjectManager vertexBufferObjectManager) {
        this.titleScreen = new TitleScreen( pX,  pY,  width,  height, lines, vertexBufferObjectManager);
        titleScreen.addMenuItem("Endless Mode", new OnMenuSelectedListener() {
            @Override
            public void onMenuItemSelected(MenuItem item) {
                activity.startEndlessMode();
            }
        });

        titleScreen.addMenuItem("Story Mode", new OnMenuSelectedListener() {
            @Override
            public void onMenuItemSelected(MenuItem item) {
                activity.startStoryMode();
            }
        });

    }

    @Override
    public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
        return titleScreen.onSceneTouchEvent(pScene, pSceneTouchEvent);
    }

    @Override
    public void show(Scene scene) {
        scene.attachChild(titleScreen);
        titleScreen.setVisible(true);
    }

    @Override
    public void hide() {

    }

    @Override
    public void onResumeGame() {

    }

    @Override
    public void onPauseGame() {

    }

    @Override
    public boolean onBackPressed() {
        return true;
    }
}
