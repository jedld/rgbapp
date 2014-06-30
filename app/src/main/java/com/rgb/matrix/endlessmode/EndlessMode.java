package com.rgb.matrix.endlessmode;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dayosoft.tiletron.app.MainActivity;
import com.dayosoft.tiletron.app.R;
import com.dayosoft.tiletron.app.SoundWrapper;
import com.google.android.gms.games.Games;
import com.rgb.matrix.GameManager;
import com.rgb.matrix.GameMatrix;
import com.rgb.matrix.GameOver;
import com.rgb.matrix.MainGrid;
import com.rgb.matrix.MatrixOptions;
import com.rgb.matrix.NextObject;
import com.rgb.matrix.ObjectDimensions;
import com.rgb.matrix.Utils;
import com.rgb.matrix.interfaces.GridEventListener;
import com.rgb.matrix.menu.MainMenu;
import com.rgb.matrix.menu.MenuItem;
import com.rgb.matrix.menu.OnBackListener;
import com.rgb.matrix.menu.OnMenuSelectedListener;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Photo;
import com.sromku.simple.fb.entities.Privacy;
import com.sromku.simple.fb.entities.Score;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnPublishListener;

import org.andengine.audio.music.Music;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.util.ScreenCapture;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by joseph on 5/6/14.
 */
public class EndlessMode extends GameManager implements GridEventListener {
    private static final int BOARD_WIDTH = 8;
    private static final int BOARD_HEIGHT = 10;
    private static final String TAG = EndlessMode.class.getName();
    private static final String PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.dayosoft.tiletron.app";

    private final MainMenu mainMenu;
    private final float canvasWidth;
    private final float canvasHeight;
    private final SimpleFacebook mSimpleFacebook;
    private final HashMap<String, Font> fontDictionary;
    private final HashMap<String, SoundWrapper> soundAsssets;
    private final Scene mScene;
    private final VertexBufferObjectManager vertexBufferObjectManager;
    private final MainGrid grid;
    private GameMatrix matrix;
    private boolean playMusic;

    public EndlessMode(MainActivity context, Scene mScene, float canvasWidth, float canvasHeight, VertexBufferObjectManager vertexBufferObjectManager,
                       HashMap<String, Font> fontDictionary, HashMap<String, SoundWrapper> soundAssets) {
        super(context);
        this.mainMenu = new MainMenu(0, 0, fontDictionary, vertexBufferObjectManager);
        this.fontDictionary = fontDictionary;
        this.soundAsssets = soundAssets;
        this.mScene = mScene;
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.vertexBufferObjectManager = vertexBufferObjectManager;
        mSimpleFacebook = SimpleFacebook.getInstance(context);
        playMusic = false;
        MatrixOptions options = new MatrixOptions();
        ScreenCapture screenCapture = new ScreenCapture();
        matrix = new GameMatrix(context, this, mScene, mainMenu, fontDictionary, soundAsssets,
                vertexBufferObjectManager, BOARD_WIDTH, BOARD_HEIGHT, 0, 10, canvasWidth, canvasHeight,
                ObjectDimensions.ENDLESS_MODE_TILE_SIZE, options);

        mainMenu.clearItems();
        mainMenu.addMenuItem("Restart", new OnMenuSelectedListener() {
            @Override
            public void onMenuItemSelected(MenuItem item) {
                mainMenu.setVisible(false);
                onRestart(item);
//                newGame();
            }
        });

        mainMenu.addMenuItem("Exit to Title Screen", new OnMenuSelectedListener() {
            @Override
            public void onMenuItemSelected(MenuItem item) {
                onExitGrid(item);
            }
        });

        boolean defaultMusicState = true, defaultSoundState = true;

        defaultMusicState = getMusicState();
        defaultSoundState = getSoundState();

        mainMenu.addMenuItem("Music", true, defaultMusicState, new OnMenuSelectedListener() {

            @Override
            public void onMenuItemSelected(MenuItem item) {
                item.setState(!item.getState());
                toggleMusic(item.getState());
            }
        });

        mainMenu.addMenuItem("Sounds", true, defaultSoundState, new OnMenuSelectedListener() {

            @Override
            public void onMenuItemSelected(MenuItem item) {
                item.setState(!item.getState());
                toggleSounds(item.getState());
            }
        });

        mainMenu.setOnBackListener(new OnBackListener() {
            @Override
            public void onBackPressed(MainMenu mainMenu) {
                mainMenu.animateHide();
            }
        });

        grid = matrix.getMainGrid();
        mScene.attachChild(screenCapture);
    }

    public void restartGame() {
        matrix.getMainGrid().newGame();
    }

    public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
        if (pSceneTouchEvent.isActionDown()) {
            matrix.onTouch(pSceneTouchEvent);
        }
        return false;
    }

    @Override
    public void show(final Scene mScene) {
        mScene.attachChild(grid);
        //Reattach menu
        context.getEngine().runOnUpdateThread(new Runnable() {
            @Override
            public void run() {
                mainMenu.detachSelf();
                mainMenu.setVisible(false);
                mScene.attachChild(mainMenu);

                playMusic = true;
                startMusic();
            }
        });

    }


    @Override
    public void onScreenCaptureHighScore(final GameOver gameOverText, ScreenCapture screenCapture) {
        String filename = null;
        try {
            Log.d(TAG,"onScreencapture " + context.getSurfaceWidth()+ " " + context.getSurfaceHeight());


            filename = context.getCacheDir().getCanonicalPath() + File.separator + "rgb_" + System.currentTimeMillis() + ".bmp";
            final String outFilename = context.getCacheDir().getCanonicalPath() + File.separator +
                    "rgb_" + System.currentTimeMillis() + ".png";
            final String finalFilename = filename;
            screenCapture.capture((int) context.getSurfaceWidth(), (int) context.getSurfaceHeight(), filename,
                    new ScreenCapture.IScreenCaptureCallback() {

                        @Override
                        public void onScreenCaptured(String pFilePath) {
                            Log.d(TAG, "Screencap path" + pFilePath);
                            AsyncTask<Void, Void, Void> compressImageTask = new ScreenshotUploadTask(finalFilename, outFilename, gameOverText);
                            compressImageTask.execute();
                        }

                        @Override
                        public void onScreenCaptureFailed(String pFilePath, Exception pException) {
                            Log.d(TAG, "Screencap failed " + pException.getMessage());
                            pException.printStackTrace();;
                        }
                    }
            );


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onExitGrid(MenuItem item) {
        hide();
        context.popCurrentManager();
    }

    @Override
    public void onSetupWorld(MainGrid mainGrid) {

    }

    @Override
    public void populateQueue(Vector<NextObject> blockQueue) {

    }

    @Override
    public void onRestart(MenuItem item) {
        restartGame();
    }

    @Override
    public void onGameOver() {
        if (context.isSignedIn()) {
            Games.Leaderboards.submitScore(context.getApiClient(), context.getResources().getString(R.string.leaderboard_id), Utils.getHighScore(context));
        }
        grid.showGameOverPopup();
    }

    @Override
    public void onLevelUp(int level) {
        if (context.isSignedIn()) {
            if (level == 10) {
                Games.Achievements.unlock(context.getApiClient(), context.getResources().getString(R.string.level10_achieve));
            }
        }
    }

    @Override
    public void onChainStarted(int multiplier) {
        if (context.isSignedIn()) {
            if (multiplier == 2) {
                Games.Achievements.unlock(context.getApiClient(), context.getResources().getString(R.string.chain2x_achieve));
            }
            if (multiplier == 5) {
                Games.Achievements.unlock(context.getApiClient(), context.getResources().getString(R.string.chain5x_achieve));
            }
        }
    }

    @Override
    public void onAddScore(int score, int previous) {
        if (score + previous > 10000) {
            Games.Achievements.unlock(context.getApiClient(), context.getResources().getString(R.string.points10000_achieve));
        }
    }


    @Override
    public void hide() {
        if (currentTrack() != null && currentTrack().isPlaying()) {
            currentTrack().pause();
        }
    }

    @Override
    public void onResumeGame() {
        if (playMusic && getMusicState() && currentTrack() != null && !currentTrack().isPlaying()) {
            currentTrack().play();
        }

    }

    @Override
    public void onPauseGame() {
        if (currentTrack() != null && currentTrack().isPlaying()) {
            currentTrack().pause();
        }
    }

    @Override
    public boolean onBackPressed() {
        if (mainMenu.isVisible()) {
            mainMenu.animateHide();
        } else {
            mainMenu.animateShow();
        }
        return true;
    }


    private class ScreenshotUploadTask extends AsyncTask<Void, Void, Void> {

        private final String finalFilename;
        private final String outFilename;
        private final GameOver gameOverText;
        public ProgressDialog dialog;

        public ScreenshotUploadTask(String finalFilename, String outFilename, GameOver gameOverText) {
            this.finalFilename = finalFilename;
            this.outFilename = outFilename;
            this.gameOverText = gameOverText;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

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
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            final Bitmap outBitmap = BitmapFactory.decodeFile(finalFilename);
            View confirmation = context.getLayoutInflater().inflate(R.layout.confirmation_dialog, null);
            TextView text = (TextView) confirmation.findViewById(R.id.message);
            text.setText("Share this screenshot to Facebook?");
            ImageView image = (ImageView) confirmation.findViewById(R.id.imageView);
            image.setMaxWidth(400);
            image.setMaxHeight(800);
            image.setImageDrawable(new BitmapDrawable(context.getResources(), outBitmap));

            builder.setView(confirmation).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();

                    final ProgressDialog dialog = ProgressDialog.show(context, "Please Wait", "Uploading Screenshot.. please wait");
                    dialog.show();

                    Privacy privacy = new Privacy.Builder()
                            .setPrivacySettings(Privacy.PrivacySettings.FRIENDS_OF_FRIENDS)
                            .build();
                    Photo photo = new Photo.Builder().setPrivacy(privacy)
                            .setImage(outBitmap)
                            .setName("Has played RGB and got a High Score of " + Utils.getHighScore(context) + "! Play now at " + PLAY_STORE_URL)
                            .build();
                    mSimpleFacebook.publish(photo, new OnPublishListener() {

                        @Override
                        public void onComplete(String id) {
                            Score score = new Score.Builder()
                                    .setScore(Utils.getHighScore(context))
                                    .build();
                            mSimpleFacebook.publish(score, new OnPublishListener() {
                                @Override
                                public void onComplete(String response) {
                                    dialog.dismiss();
                                    Toast.makeText(context, "upload complete!", Toast.LENGTH_LONG).show();
                                    gameOverText.hideShare();
                                }

                                @Override
                                public void onFail(String reason) {
                                    super.onFail(reason);
                                    dialog.dismiss();
                                    Log.e(TAG, "Unable to upload reason - " + reason);
                                    Toast.makeText(context, "Unable to upload screenshot to facebook.", Toast.LENGTH_LONG).show();
                                }

                            });
                        }

                        @Override
                        public void onFail(String reason) {
                            super.onFail(reason);
                            dialog.dismiss();
                            Log.e(TAG, "Unable to upload reason - " + reason);
                            Toast.makeText(context, "Unable to upload screenshot to facebook.", Toast.LENGTH_LONG).show();
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
                    Log.e(TAG, "Unable to upload reason - " + reason);
                    Toast.makeText(context, "Unable to login to facebook", Toast.LENGTH_LONG).show();
                }
            });
        }

    }
}
