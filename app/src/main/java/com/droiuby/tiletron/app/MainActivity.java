package com.droiuby.tiletron.app;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.rgb.matrix.GameMatrix;

import org.andengine.engine.Engine;
import org.andengine.engine.FixedStepEngine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.ui.IGameInterface;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.color.Color;

import java.util.HashMap;


public class MainActivity extends BaseGameActivity {

    private static final float WIDTH = 480;
    private static final float HEIGHT = 800;
    private static final int BOARD_WIDTH = 8;
    private static final int BOARD_HEIGHT = 12;
    private Camera mCamera;
    private Scene mScene;
    private GameMatrix matrix;
    private Font mFont;
    private Font mFontPoints;
    private Font mFontMultiplier;

    @Override
    public EngineOptions onCreateEngineOptions() {
        mCamera = new Camera(0, 0, WIDTH, HEIGHT);
        EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(WIDTH,HEIGHT), mCamera);
        return engineOptions;
    }

    @Override
    public Engine onCreateEngine(EngineOptions pEngineOptions) {
        // Create a fixed step engine updating at 60 steps per second
        return new FixedStepEngine(pEngineOptions, 60);
    }

    @Override
    public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {
        Typeface typeface
                = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);

        mFont = FontFactory.create(getFontManager(), getTextureManager(), 256, 256, typeface, 24f, true, Color.WHITE_ABGR_PACKED_INT);
        mFont.load();
        mFont.prepareLetters("Score: 0123456789 High: +0123456789".toCharArray());

        mFontPoints = FontFactory.create(getFontManager(), getTextureManager(), 256, 256, typeface, 14f, true, Color.WHITE_ABGR_PACKED_INT);
        mFontPoints.load();
        mFontPoints.prepareLetters("+0123456789".toCharArray());

        mFontMultiplier = FontFactory.create(getFontManager(), getTextureManager(), 256, 256, typeface, 40f, true, Color.WHITE_ABGR_PACKED_INT);
        mFontMultiplier.load();
        mFontMultiplier.prepareLetters("x0123456789".toCharArray());

        pOnCreateResourcesCallback.onCreateResourcesFinished();
    }

    @Override
    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
        mScene = new Scene();
        int offset_x = (int)((WIDTH / 2) - ((BOARD_WIDTH * GameMatrix.RECT_SIZE) / 2));

        HashMap<String, Font> fontHashMap = new HashMap<String, Font>();
        fontHashMap.put("score", mFont);
        fontHashMap.put("points", mFontPoints);
        fontHashMap.put("multiplier", mFontMultiplier);

        matrix = GameMatrix.getInstance(this, mScene, fontHashMap, getVertexBufferObjectManager(), BOARD_WIDTH, BOARD_HEIGHT, offset_x, 10);
        mScene.registerUpdateHandler(matrix);
        mScene.setOnSceneTouchListener(new IOnSceneTouchListener() {

            @Override
            public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
                if (pSceneTouchEvent.isActionDown()) {
                    matrix.onTouch(pSceneTouchEvent);
                }

                return false;
            }
        });
        mScene.setBackground(new Background(Color.WHITE));

        pOnCreateSceneCallback.onCreateSceneFinished(mScene);
    }

    @Override
    public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
        matrix.drawWorld();
        pOnPopulateSceneCallback.onPopulateSceneFinished();
    }
}
