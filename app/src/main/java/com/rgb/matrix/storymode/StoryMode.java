package com.rgb.matrix.storymode;

import android.content.Context;
import android.util.JsonReader;

import com.dayosoft.tiletron.app.SoundWrapper;
import com.rgb.matrix.GameMatrix;
import com.rgb.matrix.GameOver;
import com.rgb.matrix.MainGrid;
import com.rgb.matrix.interfaces.GridEventListener;
import com.rgb.matrix.menu.MainMenu;
import com.rgb.matrix.menu.MenuItem;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.util.ScreenCapture;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.vbo.VertexBufferObject;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.events.Event;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by joseph on 5/5/14.
 */
public class StoryMode implements GridEventListener {

    public static final String FIRST_LEVEL = "level_1";

    private final Context context;
    private final MainMenu mainMenu;
    private final HashMap<String, Font> fontDictionary;
    private final HashMap<String, SoundWrapper> soundAsssets;
    private final Scene mScene;
    private final VertexBufferObjectManager vertexBufferObjectManager;
    private final int offset_x;
    private GameMatrix matrix;

    public StoryMode(Context context, Scene mScene, int offset_x, VertexBufferObjectManager vertexBufferObjectManager,
                     MainMenu mainMenu, HashMap<String, Font> fontDictionary, HashMap<String, SoundWrapper> soundAssets) {
        this.context = context;
        this.mainMenu = mainMenu;
        this.fontDictionary = fontDictionary;
        this.soundAsssets = soundAssets;
        this.mScene = mScene;
        this.offset_x = offset_x;
        this.vertexBufferObjectManager = vertexBufferObjectManager;
    }

    public void renderLevel(Level level, Context context) {
        mScene.detachChildren();
        matrix = new GameMatrix(context, this, mScene, mainMenu, fontDictionary, soundAsssets, vertexBufferObjectManager, level.getGridWidth(), level.getGridHeight(), offset_x, 10);
        MainGrid grid = matrix.getMainGrid();
        mScene.attachChild(grid);
    }

    public Level loadLevel() {
        return loadLevel(FIRST_LEVEL);
    }

    public Level loadLevel(String levelName) {
        Level level = new Level();

        try {
            InputStream is = context.getAssets().open("levels/" + levelName + ".json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("levels/" + levelName + ".json")));
            StringBuilder levelString = new StringBuilder();

            while(reader.ready()) {
                levelString.append(reader.readLine());
            }
            reader.close();
            is.close();

            JSONObject jsonObject = new JSONObject(levelString.toString());
            level.setName(jsonObject.getString("title"));
            level.setId(jsonObject.getInt("id"));
            level.setNextLevel(jsonObject.getInt("next_level"));
            level.setGridWidth(jsonObject.getInt("grid_width"));
            level.setGridHeight(jsonObject.getInt("grid_height"));
            return level;
        } catch (IOException e) {
            e.printStackTrace();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void toggleMusic(boolean state) {

    }

    @Override
    public void toggleSounds(boolean state) {

    }

    @Override
    public boolean getMusicState() {
        return false;
    }

    @Override
    public boolean getSoundState() {
        return false;
    }

    @Override
    public void onScreenCaptureHighScore(GameOver gameOverText, ScreenCapture screenCapture) {

    }

    @Override
    public void onExitGrid(MenuItem item) {

    }
}
