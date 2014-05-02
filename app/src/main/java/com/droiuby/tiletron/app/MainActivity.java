package com.droiuby.tiletron.app;

import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;

import com.rgb.matrix.GameMatrix;
import com.rgb.matrix.MainGrid;
import com.rgb.matrix.Utils;
import com.rgb.matrix.interfaces.OnSequenceFinished;
import com.rgb.matrix.intro.LogoTiles;

import org.andengine.audio.music.Music;
import org.andengine.audio.music.MusicFactory;
import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.Engine;
import org.andengine.engine.FixedStepEngine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.color.Color;
import org.andengine.util.modifier.IModifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends BaseGameActivity {

    private static final float WIDTH = 480;
    private static final float HEIGHT = 800;
    private static final int BOARD_WIDTH = 8;
    private static final int BOARD_HEIGHT = 10;
    private Camera mCamera;
    private Scene mScene;
    private GameMatrix matrix;
    private Font mFont;
    private Font mFontPoints;
    private Font mFontMultiplier;
    private Sound mSound;
    int currentMusicTrack = 0;
    private List<Music> trackList = new ArrayList<Music>();
    HashMap<String, Sound> soundAssets = new HashMap<String, Sound>();
    private HashMap<String, Font> fontHashMap;
    private ArrayList<String> logoLines;
    private LogoTiles logo;
    private boolean playMusic;
    private MainGrid grid;


    @Override
    public EngineOptions onCreateEngineOptions() {
        mCamera = new Camera(0, 0, WIDTH, HEIGHT);
        EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(WIDTH, HEIGHT), mCamera);
        engineOptions.getAudioOptions().setNeedsSound(true);
        engineOptions.getAudioOptions().setNeedsMusic(true);
        return engineOptions;
    }

    @Override
    public Engine onCreateEngine(EngineOptions pEngineOptions) {
        // Create a fixed step engine updating at 60 steps per second
        return new FixedStepEngine(pEngineOptions, 60);
    }

    private void loadSound(String name, String filename) {
        try {
            soundAssets.put(name, SoundFactory.createSoundFromAsset(getSoundManager(), this, filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMusic(String filename) {
        try {
            MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    nextTrack();
                    currentTrack().play();
                }
            };
            Music music = MusicFactory.createMusicFromAsset(getMusicManager(), this, filename);
            music.setOnCompletionListener(listener);
            trackList.add(music);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {

        loadLogoText();

        SoundFactory.setAssetBasePath("sfx/");
        MusicFactory.setAssetBasePath("sfx/");

        loadSound("place_tile", "place_tile.mp3");
        loadSound("cascade", "cascade.mp3");
        loadSound("super", "super_ready.mp3");

        // Load our "music.mp3" file into a music object
        loadMusic("bg_1.mp3");
        loadMusic("bg_2.mp3");

        playMusic = false;
        Typeface typeface
                = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);

        mFont = FontFactory.create(getFontManager(), getTextureManager(), 256, 256, typeface, 24f, true, Color.WHITE_ABGR_PACKED_INT);
        mFont.load();
        mFont.prepareLetters("Score: 0123456789 High: +0123456789".toCharArray());

        Typeface typefaceMultiplier
                = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
        mFontPoints = FontFactory.create(getFontManager(), getTextureManager(), 256, 256, typefaceMultiplier, 14f, true, Color.WHITE_ABGR_PACKED_INT);
        mFontPoints.load();
        mFontPoints.prepareLetters("+0123456789".toCharArray());

        mFontMultiplier = FontFactory.create(getFontManager(), getTextureManager(), 256, 256, typeface, 30f, true, Color.WHITE_ABGR_PACKED_INT);
        mFontMultiplier.load();
        mFontMultiplier.prepareLetters("x0123456789".toCharArray());

        pOnCreateResourcesCallback.onCreateResourcesFinished();
    }

    private Music currentTrack() {
        return trackList.get(currentMusicTrack);
    }

    private void nextTrack() {
        currentMusicTrack++;
        if (currentMusicTrack >= trackList.size()) currentMusicTrack = 0;
    }

    @Override
    public synchronized void onGameCreated() {
        super.onGameCreated();
        if (!Utils.hasShownIntro(this)) {
            showIntro();
            logo.startAnimationSequence(new OnSequenceFinished() {
                @Override
                public void completed() {
                    Utils.introShown(MainActivity.this);

                    logo.registerEntityModifier(new AlphaModifier(1.0f, 0.0f, 1.0f, new IEntityModifier.IEntityModifierListener() {
                        @Override
                        public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {

                        }

                        @Override
                        public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                            logo.setVisible(false);
                            startEndlessMode();
                        }
                    }));
                }
            });
        } else {
            startEndlessMode();
        }
    }

    @Override
    public synchronized void onResumeGame() {
        if (playMusic && currentTrack() != null && !currentTrack().isPlaying()) {
            currentTrack().play();
        }

        super.onResumeGame();


    }

    @Override
    public synchronized void onPauseGame() {
        if (currentTrack() != null && currentTrack().isPlaying()) {
            currentTrack().pause();
        }
        super.onPauseGame();
    }


    @Override
    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
        mScene = new Scene();

        fontHashMap = new HashMap<String, Font>();
        fontHashMap.put("score", mFont);
        fontHashMap.put("points", mFontPoints);
        fontHashMap.put("multiplier", mFontMultiplier);

        mScene.setBackground(new Background(Color.WHITE));

        pOnCreateSceneCallback.onCreateSceneFinished(mScene);
    }

    @Override
    public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
        int offset_x = (int) ((WIDTH / 2) - ((BOARD_WIDTH * MainGrid.RECT_SIZE) / 2));
        matrix = GameMatrix.getInstance(this, mScene, fontHashMap, soundAssets, getVertexBufferObjectManager(), BOARD_WIDTH, BOARD_HEIGHT, offset_x, 10);
        grid = matrix.getMainGrid();
        pOnPopulateSceneCallback.onPopulateSceneFinished();
    }

    private void loadLogoText() throws IOException {
        logoLines = new ArrayList<String>();
        InputStreamReader inputStreamReader = new InputStreamReader(getAssets().open("logo.txt"));
        BufferedReader reader = new BufferedReader(inputStreamReader);
        while (reader.ready()) {
            logoLines.add(reader.readLine());
        }
    }

    void showIntro() {
        mScene.detachChildren();
        logo = new LogoTiles(0, 0, WIDTH, HEIGHT, logoLines, getVertexBufferObjectManager());
        mScene.attachChild(logo);
    }

    void startEndlessMode() {
        mScene.detachChildren();
        mScene.attachChild(matrix.getMainGrid());
        matrix.drawWorld();
        playMusic = true;

        if (currentTrack() != null && !currentTrack().isPlaying()) {
            currentTrack().play();
        }

        mScene.setOnSceneTouchListener(new IOnSceneTouchListener() {

            @Override
            public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
                if (pSceneTouchEvent.isActionDown()) {
                    matrix.onTouch(pSceneTouchEvent);
                }

                return false;
            }
        });


    }

}
