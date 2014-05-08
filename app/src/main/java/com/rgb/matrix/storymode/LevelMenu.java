package com.rgb.matrix.storymode;

import android.util.Log;

import com.rgb.matrix.ColorConstants;
import com.rgb.matrix.EmptyBoundedEntity;
import com.rgb.matrix.RectangleButton;
import com.rgb.matrix.Utils;
import com.rgb.matrix.interfaces.BoundedEntity;

import org.andengine.entity.primitive.Rectangle;
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

    public LevelMenu(float pX, float pY, float sceneWidth, float sceneHeight, int columns,
                     int rows,  HashMap<String, Font> mFont, ArrayList<LevelInfo> levelInfos, VertexBufferObjectManager vertexBufferObjectManager) {
        super(pX, pY);
        this.sceneWidth = sceneWidth;
        this.sceneHeight = sceneHeight;
        this.columns = columns;
        this.rows = rows;
        this.levelInfos = levelInfos;
        this.mFont = mFont;
        this.vertexBufferObjectManager = vertexBufferObjectManager;


        int levelSelect = 0;
        float gridMaxWidth = 0;
        float gridMaxHeight = 0;

        EmptyBoundedEntity grid = new EmptyBoundedEntity(0,0,0,0);

        for(int i = 0 ; i < rows; i++) {
            for(int i2 =0; i2 < columns; i2++) {
                float posY = i * (LEVEL_TILE_SIZE + LEVEL_TILE_MARGINS);
                float posX = i2 * (LEVEL_TILE_SIZE + LEVEL_TILE_MARGINS);
                if (levelSelect < levelInfos.size()) {
                    Log.d(TAG, "loading level");
                    LevelInfo info = levelInfos.get(levelSelect++);
                    RectangleButton rectButton = new RectangleButton(posX, posY, LEVEL_TILE_SIZE, LEVEL_TILE_SIZE,
                            vertexBufferObjectManager, mFont.get("level_font"), info.getTitle());
                    rectButton.setTextColor(Color.WHITE);
                    if (info.isLocked()) {
                        rectButton.setColor(Color.BLACK);
                    } else {
                        rectButton.setColor(ColorConstants.BLUE);
                    }
                    rectButton.setTag(info.getId());
                    rectangles.add(rectButton);
                    grid.attachChild(rectButton);
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
                listener.onLevelSelected(button.getTag());
                return true;
            }
        }
        return false;
    }
}
