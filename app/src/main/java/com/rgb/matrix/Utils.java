package com.rgb.matrix;

import android.content.Context;
import android.content.SharedPreferences;

import com.dayosoft.tiletron.app.MainActivity;
import com.dayosoft.tiletron.app.R;
import com.dayosoft.tiletron.app.SoundWrapper;
import com.facebook.RequestBatch;
import com.google.android.gms.games.Games;
import com.rgb.matrix.interfaces.BoundedEntity;
import com.rgb.matrix.models.GameProgress;

import org.andengine.entity.Entity;
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
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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

    public static float getWidth(Entity shape) {
        if (shape instanceof BoundedEntity) {
            return((BoundedEntity) shape).getWidth();
        } else
            if (shape instanceof RectangularShape) {
                return ((RectangularShape)shape).getWidth();
            }
        return 0;
    }

    public static float getHeight(Entity shape) {
        if (shape instanceof BoundedEntity) {
            return((BoundedEntity) shape).getHeight();
        } else
        if (shape instanceof RectangularShape) {
            return ((RectangularShape)shape).getHeight();
        }
        return 0;
    }

    public static boolean withinTouchBounds(Entity button, TouchEvent pSceneTouchEvent) {
        float[] shareButtonCoords = button.getParent().convertLocalToSceneCoordinates(button.getX(), button.getY());
            if (shareButtonCoords[Constants.VERTEX_INDEX_X] < pSceneTouchEvent.getX() &&
                    shareButtonCoords[Constants.VERTEX_INDEX_Y] < pSceneTouchEvent.getY() &&
                    pSceneTouchEvent.getX() < shareButtonCoords[Constants.VERTEX_INDEX_X] + getWidth(button) &&
                    pSceneTouchEvent.getY() < shareButtonCoords[Constants.VERTEX_INDEX_Y] + getHeight(button)
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

    public static void saveAchievementState(Context context,  HashSet<String> cachedAchievements) {
        SharedPreferences prefs = context.getSharedPreferences("achievements", Context.MODE_PRIVATE);
        JSONArray array = new JSONArray();
        for(String achieve : cachedAchievements) {
            array.put(achieve);
        }
        prefs.edit().putString("cached_achievements", array.toString()).commit();
    }

    public static ArrayList<String> restoreAchievementState(Context context) {
        ArrayList<String> cachedAchievements = new ArrayList<String>();
        SharedPreferences prefs = context.getSharedPreferences("achievements", Context.MODE_PRIVATE);
        String achieveStr = prefs.getString("cached_achievements", null);
        if (achieveStr!=null) {
            try {
                JSONArray jsonArray = new JSONArray(achieveStr);
                for(int i = 0; i < jsonArray.length(); i++) {
                    cachedAchievements.add(jsonArray.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return cachedAchievements;
    }

    public static void unlock(MainActivity context, String achievementCode) {
        GameProgress progress = GameProgress.getInstance(context);
        if (context.getApiClient()!=null && context.getApiClient().isConnected() && context.isSignedIn()) {
            Games.Achievements.unlock(context.getApiClient(), achievementCode);
        } else {
            progress.saveAchievement(achievementCode);
        }
    }
}
