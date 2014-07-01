package com.rgb.matrix.title;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;

import com.dayosoft.tiletron.app.MainActivity;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.GameHelper;
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
    private final MainActivity context;
    private boolean backedPressed = false;


    public TitleScreenManager(final MainActivity activity, float pX, float pY, float width, float height, List<String> lines, VertexBufferObjectManager vertexBufferObjectManager) {
        super(activity);
        this.titleScreen = new TitleScreen(pX, pY, width, height, lines, vertexBufferObjectManager);
        this.context = activity;
        titleScreen.addMenuItem("Story Mode", new OnMenuSelectedListener() {
            @Override
            public void onMenuItemSelected(MenuItem item) {
                activity.startStoryMode();
            }
        });

        titleScreen.addMenuItem("Endless Mode", new OnMenuSelectedListener() {
            @Override
            public void onMenuItemSelected(MenuItem item) {
                activity.startEndlessMode();
            }
        });

        titleScreen.addMinorMenuItem("Achievements", new OnMenuSelectedListener() {

            @Override
            public void onMenuItemSelected(MenuItem item) {
                context.runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                if (context.isSignedIn()) {
                                    context.startActivityForResult(Games.Achievements.getAchievementsIntent(context.getApiClient()),
                                            MainActivity.REQUEST_ACHIEVEMENTS);
                                } else {
                                    context.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            context.signInGoogle();
                                        }
                                    });

                                }
                            }
                        }
                );

            }
        });

        titleScreen.addMinorMenuItem("Credits", new OnMenuSelectedListener() {
            @Override
            public void onMenuItemSelected(MenuItem item) {

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                context);

                        // set title
                        alertDialogBuilder.setTitle("Credits");

                        // set dialog message
                        alertDialogBuilder
                                .setMessage("Credits\n\nDeveloper: Joseph Emmanuel Dayo\n\nAlso to those involved in testing\n\nElen and Eli")
                                .setCancelable(false)
                                .setPositiveButton("Ok. Cool.", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // if this button is clicked, close
                                        // current activity
                                        dialog.dismiss();
                                    }
                                });
                        // create alert dialog
                        AlertDialog alertDialog = alertDialogBuilder.create();

                        // show it
                        alertDialog.show();
                    }
                });

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
        context.showAds();
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
        if (!backedPressed) {
            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_LONG).show();
            backedPressed = true;
        } else {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        return true;
    }
}
