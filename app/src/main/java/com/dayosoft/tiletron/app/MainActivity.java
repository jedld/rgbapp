package com.dayosoft.tiletron.app;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.rgb.matrix.GameManager;
import com.rgb.matrix.Utils;
import com.rgb.matrix.endlessmode.EndlessMode;
import com.rgb.matrix.interfaces.OnSequenceFinished;
import com.rgb.matrix.intro.LogoTiles;
import com.rgb.matrix.storymode.StoryMode;
import com.rgb.matrix.title.TitleScreenManager;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.listeners.OnLoginListener;

import org.andengine.audio.music.Music;
import org.andengine.audio.music.MusicFactory;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.Engine;
import org.andengine.engine.FixedStepEngine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.ColorModifier;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.view.RenderSurfaceView;
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

    private static final String TAG = MainActivity.class.getName();
    public static float canvasWidth = 480;
    public static float canvasHeight = 800;
    private Camera mCamera;
    private Scene mScene;


    HashMap<String, SoundWrapper> soundAssets = new HashMap<String, SoundWrapper>();
    HashMap<String, TextureRegion> spriteAssets = new HashMap<String, TextureRegion>();
    private ArrayList<GameManager> foreground = new ArrayList<GameManager>();
    private HashMap<String, Font> fontHashMap;
    private ArrayList<String> logoLines = new ArrayList<String>();
    private List<String> titleLines = new ArrayList<String>();
    private LogoTiles logo;

    private boolean backedPressed = false;
    private SimpleFacebook mSimpleFacebook;


    OnLoginListener onLoginListener = new OnLoginListener() {
        @Override
        public void onLogin() {
            // change the state of the button or do whatever you want
            Log.i(TAG, "Logged in");
        }

        @Override
        public void onNotAcceptingPermissions(Permission.Type type) {
            // user didn't accept READ or WRITE permission
            Log.w(TAG, String.format("You didn't accept %s permissions", type.name()));
        }

        @Override
        public void onThinking() {

        }

        @Override
        public void onException(Throwable throwable) {

        }

        @Override
        public void onFail(String reason) {

        }

    /*
     * You can override other methods here:
     * onThinking(), onFail(String reason), onException(Throwable throwable)
     */
    };
    private TextureRegion mSpriteTextureRegion;
    private TitleScreenManager titleScreen;
    private StoryMode storyMode;
    private EndlessMode endlessMode;
    private AdView adView;
    private ArrayList<Music> trackList = new ArrayList<Music>();

    @Override
    protected void onSetContentView() {
        final FrameLayout frameLayout = new FrameLayout(this);
        //Creating its layout params, making it fill the screen.
        final FrameLayout.LayoutParams frameLayoutLayoutParams =
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT);
        this.mRenderSurfaceView = new RenderSurfaceView(this);
        this.mRenderSurfaceView.setRenderer(this.mEngine, this);

        //Adding the views to the frame layout.
        frameLayout.addView(this.mRenderSurfaceView, BaseGameActivity.createSurfaceViewLayoutParams());

        // Create an ad.
        adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-5223989576875261/3336354885");

        frameLayout.addView(adView);

        this.setContentView(frameLayout, frameLayoutLayoutParams);

    }

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);
    }

    @Override
    protected synchronized void onResume() {
        super.onResume();
        mSimpleFacebook = SimpleFacebook.getInstance(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSimpleFacebook.onActivityResult(this, requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public EngineOptions onCreateEngineOptions() {
        mCamera = new Camera(0, 0, canvasWidth, canvasHeight);
        EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(canvasWidth, canvasHeight), mCamera);
        engineOptions.getAudioOptions().setNeedsSound(true);
        engineOptions.getAudioOptions().setNeedsMusic(true);
        return engineOptions;
    }

    @Override
    public Engine onCreateEngine(EngineOptions pEngineOptions) {
        // Create a fixed step engine updating at 30 steps per second
        return new FixedStepEngine(pEngineOptions, 30);
    }

    private void loadSound(String name, String filename) {
        try {
            soundAssets.put(name, new SoundWrapper(this, SoundFactory.createSoundFromAsset(getSoundManager(), this, filename)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMusic(String filename) {
        try {

            Music music = MusicFactory.createMusicFromAsset(getMusicManager(), this, filename);

            trackList.add(music);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {

        loadLogoText(logoLines, "logo.txt");
        loadLogoText(titleLines, "title.txt");

        SoundFactory.setAssetBasePath("sfx/");
        MusicFactory.setAssetBasePath("sfx/");
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

        BuildableBitmapTextureAtlas mBitmapTextureAtlas = new BuildableBitmapTextureAtlas(mEngine.getTextureManager(), 256, 256,
                TextureOptions.BILINEAR);

        loadBitmapAsset(mBitmapTextureAtlas, "fb_icon");
        loadBitmapAsset(mBitmapTextureAtlas, "single_tap");
        loadBitmapAsset(mBitmapTextureAtlas, "ic_action_fast_forward");

        /* Build the bitmap texture atlas */
        try {
            mBitmapTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 1));
        } catch (ITextureAtlasBuilder.TextureAtlasBuilderException e) {
            e.printStackTrace();
        }

        mBitmapTextureAtlas.load();


        loadSound("place_tile", "place_tile.mp3");
        loadSound("cascade", "cascade.mp3");
        loadSound("super", "super_ready.mp3");
        loadSound("menu", "menu.mp3");
        loadSound("typing","keyboard.mp3");


        // Load our "music.mp3" file into a music object
        loadMusic("bg_1.mp3");
        loadMusic("bg_2.mp3");



        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/roboto_bold.ttf");

        Font mFont = FontFactory.create(getFontManager(), getTextureManager(), 256, 256, typeface, 24f, true, Color.WHITE_ABGR_PACKED_INT);
        mFont.load();
        mFont.prepareLetters("Score: 0123456789 High: +0123456789".toCharArray());

        Font mFontPoints = FontFactory.create(getFontManager(), getTextureManager(), 256, 256, typeface, 14f, true, Color.WHITE_ABGR_PACKED_INT);
        mFontPoints.load();
        mFontPoints.prepareLetters("+0123456789".toCharArray());

        Font mFontMultiplier = FontFactory.create(getFontManager(), getTextureManager(), 256, 256, typeface, 30f, true, Color.WHITE_ABGR_PACKED_INT);
        mFontMultiplier.load();
        mFontMultiplier.prepareLetters("x0123456789".toCharArray());

        Font titleScreenFont = FontFactory.create(getFontManager(), getTextureManager(), 256, 256, typeface, 40f, true, Color.WHITE_ABGR_PACKED_INT);
        titleScreenFont.load();
        titleScreenFont.prepareLetters("x0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray());

        fontHashMap = new HashMap<String, Font>();
        fontHashMap.put("score", mFont);
        fontHashMap.put("points", mFontPoints);
        fontHashMap.put("multiplier", mFontMultiplier);
        fontHashMap.put("menu", mFontMultiplier);
        fontHashMap.put("title", titleScreenFont);
        fontHashMap.put("story_text", mFont);
        fontHashMap.put("level_font", mFontMultiplier);
        fontHashMap.put("touch_indicator", mFont);
        
        pOnCreateResourcesCallback.onCreateResourcesFinished();
    }

    private void loadBitmapAsset(BuildableBitmapTextureAtlas mBitmapTextureAtlas, String name) {
        mSpriteTextureRegion = BitmapTextureAtlasTextureRegionFactory.
                createFromAsset(mBitmapTextureAtlas, this, name + ".png");
        spriteAssets.put(name, mSpriteTextureRegion);
    }


    @Override
    public synchronized void onGameCreated() {
        super.onGameCreated();
        Log.d(TAG, "onGameCreated() called");
        startMainSequence();
    }


    private void startMainSequence() {
        if (!Utils.hasShownIntro(this)) {
            showIntro();
            logo.startAnimationSequence(new OnSequenceFinished() {
                @Override
                public void completed() {
                    Utils.introShown(MainActivity.this);

                    logo.registerEntityModifier(new ColorModifier(0.5f, Color.BLACK, Color.WHITE, new IEntityModifier.IEntityModifierListener() {
                        @Override
                        public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {

                        }

                        @Override
                        public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                            logo.setVisible(false);
                            showTitleScreen();
                        }
                    }));
                }
            });
        } else {
            showTitleScreen();
        }
    }

    private GameManager getCurrentManager() {
        if (foreground.size() > 0) {
            return foreground.get(foreground.size() - 1);
        }
        return null;
    }

    private void setCurrentManager(GameManager manager) {
        mScene.detachChildren();
        if (foreground.size() > 0) {
            GameManager prev = foreground.get(foreground.size() - 1);
            prev.hide();
        }
        foreground.add(manager);
        manager.show(mScene);
        mScene.setOnSceneTouchListener(manager);
    }

    private void showTitleScreen() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adView.setVisibility(View.VISIBLE);
            }
        });
        setCurrentManager(titleScreen);
    }

    @Override
    protected void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    public synchronized void onResumeGame() {
        if (getCurrentManager()!=null) {
            getCurrentManager().onResumeGame();
        }

        super.onResumeGame();
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    public synchronized void onPauseGame() {
        getCurrentManager().onPauseGame();
        super.onPauseGame();
    }


    @Override
    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
        mScene = new Scene();

        mScene.setBackground(new Background(Color.WHITE));
        Utils.getInstance(this, spriteAssets, soundAssets, fontHashMap, getVertexBufferObjectManager());
        pOnCreateSceneCallback.onCreateSceneFinished(mScene);
    }

    @Override
    public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
        endlessMode = new EndlessMode(this, mScene, canvasWidth, canvasHeight, getVertexBufferObjectManager(),
                fontHashMap, soundAssets);
        endlessMode.setMusic(trackList);
        storyMode = new StoryMode(this, mScene, canvasWidth, canvasHeight, getVertexBufferObjectManager(),
                fontHashMap, soundAssets);

        titleScreen = new TitleScreenManager(this, 0, 0, canvasWidth, canvasHeight, titleLines, getVertexBufferObjectManager());
        pOnPopulateSceneCallback.onPopulateSceneFinished();
    }

    public void startStoryMode() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adView.setVisibility(View.GONE);
            }
        });
        setCurrentManager(storyMode);
    }

    private void loadLogoText(List<String> lines, String filename) throws IOException {
        lines.clear();
        InputStreamReader inputStreamReader = new InputStreamReader(getAssets().open(filename));
        BufferedReader reader = new BufferedReader(inputStreamReader);
        while (reader.ready()) {
            lines.add(reader.readLine());
        }
    }

    void showIntro() {
        mScene.detachChildren();
        logo = new LogoTiles(0, 0, canvasWidth, canvasHeight, logoLines, getVertexBufferObjectManager());
        mScene.attachChild(logo);
    }

    public void startEndlessMode() {
        setCurrentManager(endlessMode);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adView.setVisibility(View.GONE);
            }
        });
    }



    @Override
    public void onBackPressed() {
        if (getCurrentManager().onBackPressed()) {
            if (!backedPressed) {
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_LONG).show();
                backedPressed = true;
            } else {
                super.onBackPressed();
                finish();
            }
        } else {
            popCurrentManager();
        }

    }

    public void popCurrentManager() {
        mScene.detachChildren();
        if (foreground.size() > 1) {
            int currentIndex = foreground.size() - 1;
            if (foreground.size() > 0) {
                GameManager prev = foreground.get(currentIndex);
                prev.hide();
            }
            foreground.remove(currentIndex);
            getCurrentManager().show(mScene);
            mScene.setOnSceneTouchListener(getCurrentManager());
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Destroy the AdView.
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
        System.exit(0);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


}
