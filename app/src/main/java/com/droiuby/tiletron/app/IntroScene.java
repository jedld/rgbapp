package com.droiuby.tiletron.app;

import android.content.Intent;

import com.rgb.matrix.interfaces.OnSequenceFinished;
import com.rgb.matrix.intro.LogoTiles;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.color.Color;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class IntroScene extends BaseGameActivity {

    private static final float WIDTH = 480;
    private static final float HEIGHT = 800;
    private Scene mScene;
    private LogoTiles logo;
    private ArrayList<String> logoLines;

    @Override
    public EngineOptions onCreateEngineOptions() {
        Camera mCamera = new Camera(0, 0, WIDTH, HEIGHT);
        EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(WIDTH,HEIGHT), mCamera);
        engineOptions.getAudioOptions().setNeedsSound(true);
        engineOptions.getAudioOptions().setNeedsMusic(true);
        return engineOptions;
    }

    @Override
    public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {
        loadLogoText();
        pOnCreateResourcesCallback.onCreateResourcesFinished();
    }

    private void loadLogoText() throws IOException {
        logoLines = new ArrayList<String>();
        InputStreamReader inputStreamReader = new InputStreamReader(getAssets().open("logo.txt"));
        BufferedReader reader = new BufferedReader(inputStreamReader);
        while (reader.ready()) {
            logoLines.add(reader.readLine());
        }
    }

    @Override
    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
        mScene = new Scene();
        pOnCreateSceneCallback.onCreateSceneFinished(mScene);
    }

    @Override
    public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
        mScene.setBackground(new Background(Color.WHITE));
        logo = new LogoTiles(0, 0, WIDTH, HEIGHT, logoLines, getVertexBufferObjectManager());
        pScene.attachChild(logo);
        pOnPopulateSceneCallback.onPopulateSceneFinished();
    }

    @Override
    public synchronized void onResumeGame() {
        super.onResumeGame();
        logo.startAnimationSequence(new OnSequenceFinished() {
            @Override
            public void completed() {
                finish();
                Intent intent = new Intent(IntroScene.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
