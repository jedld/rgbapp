package com.rgb.matrix.storymode;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.rgb.matrix.ColorConstants;
import com.rgb.matrix.EmptyBoundedEntity;
import com.rgb.matrix.RectangleButton;
import com.rgb.matrix.Utils;
import com.rgb.matrix.interfaces.BoundedEntity;

import org.andengine.entity.modifier.ColorModifier;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by joseph on 5/8/14.
 */
public class LevelMenu extends BoundedEntity {

    private static final float LEVEL_TILE_SIZE = 120;
    private static final float LEVEL_TILE_MARGINS = 10;
    private static final String TAG = LevelMenu.class.getName();
    private final float sceneWidth;
    private final float sceneHeight;
    private final int columns;
    private final int rows;
    private final String episodeName;
    private final Activity context;


    public ArrayList<LevelInfo> getLevelInfos() {
        return levelInfos;
    }

    public void setLevelInfos(ArrayList<LevelInfo> levelInfos) {
        this.levelInfos = levelInfos;
    }

    private  ArrayList<LevelInfo> levelInfos;
    private final ArrayList<RectangleButton> rectangles = new ArrayList<RectangleButton>();
    private final HashMap<String, Font> mFont;
    private final VertexBufferObjectManager vertexBufferObjectManager;

    public OnLevelSelectedListener getListener() {
        return listener;
    }

    public void setListener(OnLevelSelectedListener listener) {
        this.listener = listener;
    }

    OnLevelSelectedListener listener;

    public LevelMenu(Activity context, float pX, float pY, float sceneWidth, float sceneHeight, int columns,
                     int rows,  HashMap<String, Font> mFont, Episode episode, VertexBufferObjectManager vertexBufferObjectManager) {
        super(pX, pY);
        this.context = context;
        this.sceneWidth = sceneWidth;
        this.sceneHeight = sceneHeight;
        this.columns = columns;
        this.rows = rows;
        this.levelInfos = episode.getLevels();
        this.episodeName = episode.getName();
        this.mFont = mFont;
        this.vertexBufferObjectManager = vertexBufferObjectManager;


        int levelSelect = 0;
        float gridMaxWidth = 0;
        float gridMaxHeight = 0;

        EmptyBoundedEntity grid = new EmptyBoundedEntity(0,0,0,0);
        final Text levelText = new Text(0, 10, mFont.get("title"), episodeName, vertexBufferObjectManager);
        levelText.setX(sceneWidth / 2 -  levelText.getWidth()/2);
        levelText.setColor(Color.BLACK);
        attachChild(levelText);
        for(int i = 0 ; i < rows; i++) {
            for(int i2 =0; i2 < columns; i2++) {
                float posY = i * (LEVEL_TILE_SIZE + LEVEL_TILE_MARGINS);
                float posX = i2 * (LEVEL_TILE_SIZE + LEVEL_TILE_MARGINS);
                if (levelSelect < levelInfos.size()) {
                    Log.d(TAG, "loading level");
                    LevelInfo info = levelInfos.get(levelSelect);
                    RectangleButton rectButton = new RectangleButton(posX, posY, LEVEL_TILE_SIZE, LEVEL_TILE_SIZE,
                            vertexBufferObjectManager, mFont.get("level_font"), info.getTitle());
                    rectButton.setTextColor(Color.WHITE);
                    if (info.isLocked()) {
                        rectButton.setColor(Color.BLACK);
                    } else {
                        rectButton.setColor(ColorConstants.BLUE);
                    }
                    rectButton.setTag(levelSelect);
                    rectangles.add(rectButton);
                    grid.attachChild(rectButton);
                    levelSelect++;
                }
                if (gridMaxHeight < posY + LEVEL_TILE_SIZE + LEVEL_TILE_MARGINS) {
                    gridMaxHeight = posY + LEVEL_TILE_SIZE + LEVEL_TILE_MARGINS;
                }
                if (gridMaxWidth < posX + LEVEL_TILE_SIZE + LEVEL_TILE_MARGINS) {
                    gridMaxWidth = posX + LEVEL_TILE_SIZE + LEVEL_TILE_MARGINS;
                }
            }
        }
        grid.setWidth(gridMaxWidth);
        grid.setHeight(gridMaxHeight);
        attachChild(grid);
        grid.centerInParent(BoundedEntity.CENTER_HORIZONTAL | BoundedEntity.CENTER_VERTICAL);
    }

    public void updateSelf() {
        int levelSelect = 0;
        for(int i = 0 ; i < rows; i++) {
            for(int i2 =0; i2 < columns; i2++) {
                if (levelSelect < levelInfos.size()) {
                    Log.d(TAG, "loading level");
                    RectangleButton rect = rectangles.get(levelSelect);
                    rect.setTextColor(Color.WHITE);
                    LevelInfo info = levelInfos.get(levelSelect);
                    if (info.isLocked()) {
                        rect.setColor(Color.BLACK);
                    } else {
                        rect.setColor(ColorConstants.BLUE);
                    }

                    levelSelect++;
                }
            }
        }
    }

    public void animateLevelUnlock(int levelSelect,IEntityModifier.IEntityModifierListener listener) {
        if ( levelSelect - 1 < rectangles.size()) {
            RectangleButton rect = rectangles.get(levelSelect - 1);
            LevelInfo info = levelInfos.get(levelSelect - 1);
            info.setLocked(false);
            Log.d(TAG,"animating level unlock");
            rect.registerEntityModifier(new ColorModifier(1f, Color.BLACK, ColorConstants.BLUE, listener));
        }
    }

    @Override
    public float getWidth() {
        return sceneWidth;
    }

    @Override
    public float getHeight() {
        return sceneHeight;
    }

    public boolean onTouch(TouchEvent pSceneTouchEvent) {
        for(RectangleButton button : rectangles) {
            if (Utils.withinTouchBounds(button, pSceneTouchEvent)) {
                LevelInfo info = levelInfos.get(button.getTag());
                if (info.isComingSoon()) {
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Coming Soon!", Toast.LENGTH_LONG).show();
                        }
                    });
                } else
                if (!info.isLocked()) {
                    listener.onLevelSelected(info.getId());
                    return true;
                }
            }
        }
        return false;
    }

    public void setEpisodeName(String name) {

    }
}
