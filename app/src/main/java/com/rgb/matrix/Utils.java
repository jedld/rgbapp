package com.rgb.matrix;

import android.content.Context;
import android.content.SharedPreferences;

import com.dayosoft.tiletron.app.MainActivity;
import com.dayosoft.tiletron.app.SoundWrapper;
import com.facebook.RequestBatch;
import com.rgb.matrix.interfaces.BoundedEntity;

import org.andengine.entity.shape.RectangularShape;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.IFont;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.Constants;
import org.andengine.util.color.Color;

import java.util.HashMap;

/**
 * Created by joseph on 5/1/14.
 */
public class Utils {

    static Utils instance;
    private final Context context;
    private final HashMap<String, TextureRegion> spriteAssets;
    private final HashMap<String, SoundWrapper> soundAssets;
    private final HashMap<String, Font> fontAssets;
    private final VertexBufferObjectManager vertexBufferObjectManager;


    protected Utils(Context context, HashMap<String, TextureRegion> spriteAssets, HashMap<String,
            SoundWrapper> soundAssets, HashMap<String, Font> fontAssets, VertexBufferObjectManager manager) {
        this.context = context;
        this.spriteAssets = spriteAssets;
        this.soundAssets = soundAssets;
        this.fontAssets = fontAssets;
        this.vertexBufferObjectManager = manager;
    }

    public static Utils getInstance(Context context, HashMap<String, TextureRegion> spriteAssets, HashMap<String,
            SoundWrapper> soundAssets, HashMap<String, Font> fontAssets, VertexBufferObjectManager manager) {
        if (instance == null) {
            instance = new Utils(context, spriteAssets, soundAssets, fontAssets, manager);
        }
        return instance;
    }

    public static Utils getInstance() {
        return instance;
    }

    public Sprite getSprite(String name) {
        Sprite mSprite = new Sprite(0, 0,
                spriteAssets.get(name), vertexBufferObjectManager);
        return mSprite;
    }

    public SoundWrapper getSound(String name) {
        return soundAssets.get(name);
    }

    public IFont getFont(String name) {
        return fontAssets.get(name);
    }

    public static int getHighScore(Context context) {
        SharedPreferences sharedPrefs = context.getSharedPreferences("high_score", Context.MODE_PRIVATE);
        return sharedPrefs.getInt("high_score", 0);
    }

    public static boolean hasShownIntro(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE);
        return prefs.getBoolean("shown_intro", false);
//        return false;
    }

    public static void introShown(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("shown_intro", true).commit();
    }

    public static boolean getMusicState(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE);
        return prefs.getBoolean("music", true);

    }

    public static boolean getSoundState(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE);
        return prefs.getBoolean("sound", true);
    }

    public static void saveMusicState(Context context, boolean state) {
        SharedPreferences prefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("music", state).commit();
    }

    public static void saveSoundState(Context context, boolean state) {
        SharedPreferences prefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("sound", state).commit();
    }

    public static boolean withinTouchBounds(BoundedEntity button, TouchEvent pSceneTouchEvent) {
        float[] shareButtonCoords = button.getParent().convertLocalToSceneCoordinates(button.getX(), button.getY());
        if (shareButtonCoords[Constants.VERTEX_INDEX_X] < pSceneTouchEvent.getX() &&
                shareButtonCoords[Constants.VERTEX_INDEX_Y] < pSceneTouchEvent.getY() &&
                pSceneTouchEvent.getX() < shareButtonCoords[Constants.VERTEX_INDEX_X] + button.getWidth() &&
                pSceneTouchEvent.getY() < shareButtonCoords[Constants.VERTEX_INDEX_Y] + button.getHeight()
                ) {
            return true;
        }
        return false;
    }

    public static Color getLighter(int tileType) {
        if (tileType == GridSquare.RED_BLOCK) {
            return ColorConstants.LIGHT_RED;
        } else if (tileType == GridSquare.BLUE_BLOCK) {
            return ColorConstants.LIGHT_BLUE;
        } else if (tileType == GridSquare.GREEN_BLOCK) {
            return ColorConstants.LIGHT_GREEN;
        }
        return ColorConstants.WHITE;
    }

    public static boolean isLocked(BaseGameActivity context, int id) {
        SharedPreferences prefs = context.getSharedPreferences("progress", Context.MODE_PRIVATE);
        return prefs.getBoolean("level_" + id, true);
    }

    public static void setLocked(Context context, int nextLevel, boolean b) {
        SharedPreferences prefs = context.getSharedPreferences("progress", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("level_" + nextLevel, b).commit();
    }
}
