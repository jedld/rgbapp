package com.rgb.matrix.endlessmode;

import android.app.Activity;
import android.content.Context;

import com.dayosoft.tiletron.app.MainActivity;
import com.dayosoft.tiletron.app.SoundWrapper;
import com.rgb.matrix.GameMatrix;
import com.rgb.matrix.GameOver;
import com.rgb.matrix.MainGrid;
import com.rgb.matrix.MatrixOptions;
import com.rgb.matrix.interfaces.GridEventListener;
import com.rgb.matrix.menu.MainMenu;
import com.rgb.matrix.menu.MenuItem;

import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.util.ScreenCapture;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import java.util.HashMap;

/**
 * Created by joseph on 5/6/14.
 */
public class EndlessMode {
    private static final int BOARD_WIDTH = 8;
    private static final int BOARD_HEIGHT = 10;

    private final MainActivity context;
    private final MainMenu mainMenu;
    private final HashMap<String, Font> fontDictionary;
    private final HashMap<String, SoundWrapper> soundAsssets;
    private final Scene mScene;
    private final VertexBufferObjectManager vertexBufferObjectManager;
    private final MainGrid grid;
    private GameMatrix matrix;

    public EndlessMode(MainActivity context, Scene mScene, float canvasWidth, float canvasHeight, VertexBufferObjectManager vertexBufferObjectManager,
                     MainMenu mainMenu, HashMap<String, Font> fontDictionary, HashMap<String, SoundWrapper> soundAssets) {
        this.context = context;
        this.mainMenu = mainMenu;
        this.fontDictionary = fontDictionary;
        this.soundAsssets = soundAssets;
        this.mScene = mScene;
        this.vertexBufferObjectManager = vertexBufferObjectManager;
        MatrixOptions options = new MatrixOptions();
        matrix = new GameMatrix(context, context, mScene, mainMenu, fontDictionary, soundAsssets,
                vertexBufferObjectManager, BOARD_WIDTH, BOARD_HEIGHT, 0, 10, canvasWidth, canvasHeight, options);
        grid = matrix.getMainGrid();
    }

    public void startEndlessMode() {
        mScene.detachChildren();
        mScene.attachChild(grid);
        mScene.setOnSceneTouchListener(new IOnSceneTouchListener() {

            @Override
            public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
                if (pSceneTouchEvent.isActionDown()) {
                    matrix.onTouch(pSceneTouchEvent);
                }
                return false;
            }
        });

        //Reattach menu
        mainMenu.detachSelf();
        mainMenu.setVisible(false);
        mScene.attachChild(mainMenu);
    }

}
