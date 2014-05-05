package com.dayosoft.tiletron.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rgb.matrix.GameMatrix;
import com.rgb.matrix.MainGrid;
import com.rgb.matrix.Utils;
import com.rgb.matrix.interfaces.GridEventListener;
import com.rgb.matrix.interfaces.OnSequenceFinished;
import com.rgb.matrix.intro.LogoTiles;
import com.rgb.matrix.menu.MainMenu;
import com.rgb.matrix.menu.MenuItem;
import com.rgb.matrix.menu.OnMenuSelectedListener;
import com.rgb.matrix.title.TitleScreen;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Photo;
import com.sromku.simple.fb.entities.Privacy;
import com.sromku.simple.fb.entities.Score;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnPublishListener;

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
import org.andengine.entity.modifier.ColorModifier;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.ScreenCapture;
import org.andengine.input.touch.TouchEvent;
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
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.color.Color;
import org.andengine.util.modifier.IModifier;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseGameActivity implements GridEventListener {

    private static final String TAG = MainActivity.class.getName();
    public static float canvasWidth = 480;
    public static float canvasHeight = 800;
    private static final int BOARD_WIDTH = 8;
    private static final int BOARD_HEIGHT = 10;
    private Camera mCamera;
    private Scene mScene;
    private GameMatrix matrix;
    private Sound mSound;
    int currentMusicTrack = 0;
    private List<Music> trackList = new ArrayList<Music>();
    HashMap<String, SoundWrapper> soundAssets = new HashMap<String, SoundWrapper>();
    HashMap<String, Sprite> spriteAssets = new HashMap<String, Sprite>();
    private HashMap<String, Font> fontHashMap;
    private ArrayList<String> logoLines = new ArrayList<String>();
    private List<String> titleLines = new ArrayList<String>();
    private LogoTiles logo;
    private boolean playMusic;
    private MainGrid grid;
    private MainMenu mainMenu;
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
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
//
//        if (width > canvasWidth) {
//            canvasWidth = width;
//        }
//
//        if (height > canvasHeight) {
//            canvasHeight = height;
//        }

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

        loadLogoText(logoLines, "logo.txt");
        loadLogoText(titleLines, "title.txt");

        SoundFactory.setAssetBasePath("sfx/");
        MusicFactory.setAssetBasePath("sfx/");
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

        BuildableBitmapTextureAtlas mBitmapTextureAtlas = new BuildableBitmapTextureAtlas(mEngine.getTextureManager(), 256, 256,
                TextureOptions.BILINEAR);

        mSpriteTextureRegion = BitmapTextureAtlasTextureRegionFactory.
                createFromAsset(mBitmapTextureAtlas, this, "fb_icon.png");

        /* Build the bitmap texture atlas */
        try {
            mBitmapTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 1));
        } catch (ITextureAtlasBuilder.TextureAtlasBuilderException e) {
            e.printStackTrace();
        }

        mBitmapTextureAtlas.load();

        Sprite mSprite = new Sprite(0, 0,
                mSpriteTextureRegion, mEngine.getVertexBufferObjectManager());

        spriteAssets.put("fb_icon", mSprite);

        loadSound("place_tile", "place_tile.mp3");
        loadSound("cascade", "cascade.mp3");
        loadSound("super", "super_ready.mp3");
        loadSound("menu", "menu.mp3");


        // Load our "music.mp3" file into a music object
        loadMusic("bg_1.mp3");
        loadMusic("bg_2.mp3");

        playMusic = false;

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
//                            startEndlessMode();
                        }
                    }));
                }
            });
        } else {
//            startEndlessMode();
            showTitleScreen();
        }
    }

    private void showTitleScreen() {
        mScene.detachChildren();
        final TitleScreen titleScreen = new TitleScreen(0, 0, canvasWidth, canvasHeight, titleLines, getVertexBufferObjectManager());
        titleScreen.addMenuItem("Endless Mode", new OnMenuSelectedListener() {
            @Override
            public void onMenuItemSelected(MenuItem item) {
                startEndlessMode();
            }
        });

        titleScreen.addMenuItem("Story Mode", new OnMenuSelectedListener() {
            @Override
            public void onMenuItemSelected(MenuItem item) {

            }
        });

        mScene.attachChild(titleScreen);

        mScene.setOnSceneTouchListener(new IOnSceneTouchListener() {

            @Override
            public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
                if (pSceneTouchEvent.isActionDown()) {
                    titleScreen.handleOnTouch(pSceneTouchEvent);
                }

                return false;
            }
        });
    }

    @Override
    public synchronized void onResumeGame() {
        if (playMusic && Utils.getMusicState(this) && currentTrack() != null && !currentTrack().isPlaying()) {
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

        mScene.setBackground(new Background(Color.WHITE));
        Utils.getInstance(this, spriteAssets, soundAssets, fontHashMap);
        pOnCreateSceneCallback.onCreateSceneFinished(mScene);
    }

    @Override
    public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
        int offset_x = (int) ((canvasWidth / 2) - ((BOARD_WIDTH * MainGrid.getRectangleTileSizeInPixels()) / 2));

        mainMenu = new MainMenu(0, 0, fontHashMap, getVertexBufferObjectManager());
        mainMenu.setVisible(false);

        matrix = GameMatrix.getInstance(this, this, mScene, mainMenu, fontHashMap, soundAssets, getVertexBufferObjectManager(), BOARD_WIDTH, BOARD_HEIGHT, offset_x, 10);
        grid = matrix.getMainGrid();

        pOnPopulateSceneCallback.onPopulateSceneFinished();
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

    void startEndlessMode() {
        mScene.detachChildren();
        mScene.attachChild(matrix.getMainGrid());
        mScene.attachChild(mainMenu);
        matrix.drawWorld();
        playMusic = true;

        if (Utils.getMusicState(this) && currentTrack() != null && !currentTrack().isPlaying()) {
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

    @Override
    public void toggleMusic(boolean state) {
        Utils.saveMusicState(this, state);
        if (currentTrack() != null) {
            if (!state && currentTrack().isPlaying()) {
                currentTrack().pause();
            } else {
                currentTrack().play();
            }
        }
    }

    @Override
    public void toggleSounds(boolean state) {
        Utils.saveSoundState(this, state);
    }

    @Override
    public boolean getMusicState() {
        return Utils.getMusicState(this);
    }

    @Override
    public boolean getSoundState() {
        return Utils.getSoundState(this);
    }


    @Override
    public void onScreenCaptureHighScore(ScreenCapture screenCapture) {
        String filename = null;
        try {
            filename = getCacheDir().getCanonicalPath() + File.separator + "rgb_" + System.currentTimeMillis() + ".bmp";
            final String outFilename = getCacheDir().getCanonicalPath() + File.separator +
                    "rgb_" + System.currentTimeMillis() + ".png";
            final String finalFilename = filename;
            screenCapture.capture(mRenderSurfaceView.getWidth(), mRenderSurfaceView.getHeight(), filename,
                    new ScreenCapture.IScreenCaptureCallback() {

                        @Override
                        public void onScreenCaptured(String pFilePath) {
                            Log.d(TAG, "Screencap path" + pFilePath);
                            AsyncTask<Void, Void, Void> compressImageTask = new AsyncTask<Void, Void, Void>() {

                                @Override
                                protected Void doInBackground(Void... voids) {

                                    try {
                                        Bitmap bitmap = BitmapFactory.decodeFile(finalFilename);

                                        File outFile = new File(outFilename);
                                        outFile.createNewFile();
                                        FileOutputStream out = new FileOutputStream(outFile);
                                        bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                                        out.flush();
                                        out.close();
                                        Log.d(TAG, "compress image on " + outFilename);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    return null;
                                }

                                private void postScreenshot() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    final Bitmap outBitmap = BitmapFactory.decodeFile(finalFilename);
                                    View confirmation = getLayoutInflater().inflate(R.layout.confirmation_dialog, null);
                                    TextView text = (TextView) confirmation.findViewById(R.id.message);
                                    text.setText("Are you sure you want to share this screenshot to Facebook?");
                                    ImageView image = (ImageView) confirmation.findViewById(R.id.imageView);
                                    image.setImageDrawable(new BitmapDrawable(getResources(), outBitmap));

                                    builder.setView(confirmation).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();

                                            final ProgressDialog dialog = ProgressDialog.show(MainActivity.this, "Please Wait", "Uploading Screenshot.. please wait");
                                            dialog.show();
                                            Photo photo = new Photo.Builder()
                                                    .setImage(outBitmap)
                                                    .setName("Has played RGB and got a High Score of " + Utils.getHighScore(MainActivity.this))
                                                    .build();
                                            mSimpleFacebook.publish(photo, new OnPublishListener() {

                                                @Override
                                                public void onComplete(String id) {
                                                    Score score = new Score.Builder()
                                                            .setScore(Utils.getHighScore(MainActivity.this))
                                                            .build();
                                                    mSimpleFacebook.publish(score, new OnPublishListener() {
                                                        @Override
                                                        public void onComplete(String response) {
                                                            dialog.dismiss();
                                                        }

                                                        @Override
                                                        public void onFail(String reason) {
                                                            super.onFail(reason);
                                                            dialog.dismiss();
                                                            Toast.makeText(MainActivity.this, "Unable to upload screenshot to facebook.", Toast.LENGTH_LONG).show();
                                                        }

                                                    });
                                                }

                                                @Override
                                                public void onFail(String reason) {
                                                    super.onFail(reason);
                                                    dialog.dismiss();
                                                    Toast.makeText(MainActivity.this, "Unable to upload screenshot to facebook.", Toast.LENGTH_LONG).show();
                                                }
                                            });

                                        }
                                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).show();

                                }

                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    super.onPostExecute(aVoid);

                                    mSimpleFacebook.login(new OnLoginListener() {
                                        @Override
                                        public void onLogin() {
                                            postScreenshot();
                                        }

                                        @Override
                                        public void onNotAcceptingPermissions(Permission.Type type) {

                                        }

                                        @Override
                                        public void onThinking() {

                                        }

                                        @Override
                                        public void onException(Throwable throwable) {

                                        }

                                        @Override
                                        public void onFail(String reason) {
                                            Toast.makeText(MainActivity.this, "Unable to login to facebook", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }

                            };
                            compressImageTask.execute();
                        }

                        @Override
                        public void onScreenCaptureFailed(String pFilePath, Exception pException) {

                        }
                    }
            );


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onExitGrid(MenuItem item) {
        mScene.detachChildren();
        mScene.setOnSceneTouchListener(null);
        showTitleScreen();
    }

    @Override
    public void onBackPressed() {
        if (mainMenu.isVisible()) {
            mainMenu.setVisible(false);
        } else {
            if (!backedPressed) {
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_LONG).show();
                backedPressed = true;
            } else {
                super.onBackPressed();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}
