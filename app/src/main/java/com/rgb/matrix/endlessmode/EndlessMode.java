package com.rgb.matrix.endlessmode;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Photo;
import com.sromku.simple.fb.entities.Score;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnPublishListener;

import org.andengine.audio.music.Music;
import org.andengine.entity.scene.IOnSceneTouchListener;
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

    private final MainActivity context;
    private final MainMenu mainMenu;
    private final float canvasWidth;
    private final float canvasHeight;
    private final SimpleFacebook mSimpleFacebook;
    private List<Music> trackList = new ArrayList<Music>();
    int currentMusicTrack = 0;
    private final HashMap<String, Font> fontDictionary;
    private final HashMap<String, SoundWrapper> soundAsssets;
    private final Scene mScene;
    private final VertexBufferObjectManager vertexBufferObjectManager;
    private final MainGrid grid;
    private GameMatrix matrix;
    private boolean playMusic;

    public EndlessMode(MainActivity context, Scene mScene, float canvasWidth, float canvasHeight, VertexBufferObjectManager vertexBufferObjectManager,
                     HashMap<String, Font> fontDictionary, HashMap<String, SoundWrapper> soundAssets) {
        this.context = context;
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
        matrix = new GameMatrix(context, this, mScene, mainMenu, fontDictionary, soundAsssets,
                vertexBufferObjectManager, BOARD_WIDTH, BOARD_HEIGHT, 0, 10, canvasWidth, canvasHeight,
                ObjectDimensions.ENDLESS_MODE_TILE_SIZE, options);
        grid = matrix.getMainGrid();
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
    public void show(Scene mScene) {
        mScene.attachChild(grid);
        //Reattach menu
        mainMenu.detachSelf();
        mainMenu.setVisible(false);
        mScene.attachChild(mainMenu);

        playMusic = true;
        if (Utils.getMusicState(context) && currentTrack() != null && !currentTrack().isPlaying()) {
            currentTrack().play();
        }
    }

    public void setMusic(ArrayList<Music> musicTracks) {
        for(Music music : musicTracks) {
            MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    nextTrack();
                    currentTrack().play();
                }
            };
            music.setOnCompletionListener(listener);
            trackList.add(music);
        }
    }

    @Override
    public void toggleMusic(boolean state) {
        Utils.saveMusicState(context, state);
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
        Utils.saveSoundState(context, state);
    }

    @Override
    public boolean getMusicState() {
        return Utils.getMusicState(context);
    }

    @Override
    public boolean getSoundState() {
        return Utils.getSoundState(context);
    }


    @Override
    public void onScreenCaptureHighScore(final GameOver gameOverText, ScreenCapture screenCapture) {
        String filename = null;
        try {

            filename = context.getCacheDir().getCanonicalPath() + File.separator + "rgb_" + System.currentTimeMillis() + ".bmp";
            final String outFilename = context.getCacheDir().getCanonicalPath() + File.separator +
                    "rgb_" + System.currentTimeMillis() + ".png";
            final String finalFilename = filename;
            screenCapture.capture((int)canvasWidth, (int)canvasHeight, filename,
                    new ScreenCapture.IScreenCaptureCallback() {

                        @Override
                        public void onScreenCaptured(String pFilePath) {
                            Log.d(TAG, "Screencap path" + pFilePath);
                            AsyncTask<Void, Void, Void> compressImageTask = new ScreenshotUploadTask(finalFilename, outFilename, gameOverText);
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
    public void hide() {
        if (currentTrack() != null && currentTrack().isPlaying()) {
            currentTrack().pause();
        }
    }

    @Override
    public void onResumeGame() {
        if (playMusic && Utils.getMusicState(context) && currentTrack() != null && !currentTrack().isPlaying()) {
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
        return false;
    }


    private Music currentTrack() {
        return trackList.get(currentMusicTrack);
    }

    private void nextTrack() {
        currentMusicTrack++;
        if (currentMusicTrack >= trackList.size()) currentMusicTrack = 0;
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
            text.setText("Are you sure you want to share this screenshot to Facebook?");
            ImageView image = (ImageView) confirmation.findViewById(R.id.imageView);
            image.setImageDrawable(new BitmapDrawable(context.getResources(), outBitmap));

            builder.setView(confirmation).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();

                    final ProgressDialog dialog = ProgressDialog.show(context, "Please Wait", "Uploading Screenshot.. please wait");
                    dialog.show();
                    Photo photo = new Photo.Builder()
                            .setImage(outBitmap)
                            .setName("Has played RGB and got a High Score of " + Utils.getHighScore(context))
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
                                    Log.e(TAG,"Unable to upload reason - " + reason);
                                    Toast.makeText(context, "Unable to upload screenshot to facebook.", Toast.LENGTH_LONG).show();
                                }

                            });
                        }

                        @Override
                        public void onFail(String reason) {
                            super.onFail(reason);
                            dialog.dismiss();
                            Log.e(TAG,"Unable to upload reason - " + reason);
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
                    Log.e(TAG,"Unable to upload reason - " + reason);
                    Toast.makeText(context, "Unable to login to facebook", Toast.LENGTH_LONG).show();
                }
            });
        }

    }
}
