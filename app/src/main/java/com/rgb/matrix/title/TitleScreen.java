package com.rgb.matrix.title;

import android.util.Log;
import android.util.Pair;

import com.rgb.matrix.GridSquare;
import com.rgb.matrix.intro.PositionsInfo;
import com.rgb.matrix.intro.TargetPosition;
import com.rgb.matrix.menu.MenuAttributes;
import com.rgb.matrix.menu.MenuEntity;
import com.rgb.matrix.menu.OnMenuSelectedListener;

import org.andengine.entity.Entity;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;
import org.andengine.util.modifier.IModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TitleScreen extends MenuEntity {
    private static final int TITLE_SCREEN_TILE_SIZE = 15, TITLE_SCREEN_PADDING = 4, TITLE_SCREEN_VERT_PADDING = 4;
    private static final float TITLE_LOGO_MARGIN_TOP = 200;
    private static final int TITLE_TILES_WIDTH = 10, TITLE_TILES_HEIGHT = 20, TITLE_TILES_SIZE = 54;
    private static final String TAG = TitleScreen.class.getName();
    private final VertexBufferObjectManager vertexBufferObjectManager;
    private final List<String> lines;
    private final float width;
    private final Random random;
    ArrayList<Rectangle> backgroundRect = new ArrayList<Rectangle>();
    private final Entity parentLogoEntity;
    float maxTextWidth = 0, maxTextHeight = 0;

    ArrayList<Rectangle> rectangles = new ArrayList<Rectangle>();

    public TitleScreen(float pX, float pY, float width, float height, List<String> lines, VertexBufferObjectManager vertexBufferObjectManager) {
        super(pX, pY, vertexBufferObjectManager);
        this.width = width;
        this.vertexBufferObjectManager = vertexBufferObjectManager;
        this.lines = lines;
        random = new Random(System.nanoTime());

        Rectangle backgroundRectangle = new Rectangle(0, 0, width, height, vertexBufferObjectManager);
        backgroundRectangle.setColor(Color.WHITE);

        attachChild(backgroundRectangle);

        PositionsInfo positionsInfo = generateFinalPositions(TITLE_SCREEN_TILE_SIZE, TITLE_SCREEN_PADDING, TITLE_SCREEN_VERT_PADDING,
                0, 0,
                lines);
        parentLogoEntity = new Entity();
        parentLogoEntity.setPosition(width / 2 - maxTextWidth / 2, TITLE_LOGO_MARGIN_TOP);

        for (int i = 0; i < positionsInfo.getFinalPositions().size(); i++) {
            TargetPosition position = positionsInfo.getFinalPositions().get(i);
            Rectangle rectangle = new Rectangle(position.getPosition().first, position.getPosition().second, TITLE_SCREEN_TILE_SIZE, TITLE_SCREEN_TILE_SIZE, vertexBufferObjectManager);
            rectangle.setColor(position.getColor());
            rectangle.setAlpha(0.7f);
            parentLogoEntity.attachChild(rectangle);
            rectangles.add(rectangle);
        }

        menuOffsetY = maxTextHeight + 250;

        for (int i = 0; i < TITLE_TILES_WIDTH; i++) {
            Line line = new Line(i * TITLE_TILES_SIZE, TITLE_TILES_SIZE, i * TITLE_TILES_SIZE, height, vertexBufferObjectManager);
            line.setColor(GridSquare.GRID_BORDER_COLOR);
            attachChild(line);
        }

        for (int i2 = 0; i2 < TITLE_TILES_HEIGHT; i2++) {
            Line line = new Line(0, i2 * TITLE_TILES_SIZE, width, i2 * TITLE_TILES_SIZE, vertexBufferObjectManager);
            line.setColor(GridSquare.GRID_BORDER_COLOR);
            attachChild(line);
        }

        randomTilePlacement();

        attachChild(parentLogoEntity);
    }

    public Entity getBackgroundRectangle() {
        return parentLogoEntity;
    }

    public float getMenuWidth() {
        return width;
    }

    private void randomTilePlacement() {
        for (int i = 0; i < TITLE_TILES_WIDTH; i++) {
            for (int i2 = 0; i2 < TITLE_TILES_HEIGHT; i2++) {

                float backgroundTileMargin = 4;
                final Rectangle backgroundTile = new Rectangle(i * TITLE_TILES_SIZE + backgroundTileMargin,
                        i2 * TITLE_TILES_SIZE + backgroundTileMargin, TITLE_TILES_SIZE - backgroundTileMargin * 2,
                        TITLE_TILES_SIZE - backgroundTileMargin * 2, vertexBufferObjectManager);
                backgroundTile.setColor(getRandomColor());
                backgroundTile.setAlpha(0.2f);
                backgroundRect.add(backgroundTile);
                attachChild(backgroundTile);
            }
        }

        for(int i = 0; i < backgroundRect.size(); i++) {
            int shouldAnim = random.nextInt(4);
            if (shouldAnim == 1) {
                setRandomAnim();
            }
        }
    }

    private void setRandomAnim() {
        final Rectangle backgroundTile = backgroundRect.get(random.nextInt(backgroundRect.size()));
        backgroundTile.registerEntityModifier(new AlphaModifier(1f, 0.2f, 0f, new IEntityModifier.IEntityModifierListener() {

            @Override
            public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {

            }

            @Override
            public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                backgroundTile.setColor(getRandomColor());
                backgroundTile.setAlpha(0.0f);
                backgroundTile.registerEntityModifier(new AlphaModifier(1f, 0f, 0.2f, new IEntityModifier.IEntityModifierListener() {
                    @Override
                    public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {

                    }

                    @Override
                    public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {

                        setRandomAnim();
                    }
                }));
            }
        }));
    }

    private Color getRandomColor() {
        int randColor = random.nextInt(3);

        switch (randColor) {
            case 0:
                return Color.RED;
            case 1:
                return Color.BLUE;
            case 2:
                return Color.GREEN;
        }
        return Color.RED;
    }

    public String getFontKey() {
        return "title";
    }

    public void addMenuItem(String label, OnMenuSelectedListener listener) {
        MenuAttributes attributes = new MenuAttributes();
        attributes.setColor(Color.BLACK);
        attributes.setBackgroundColor(Color.WHITE);
        attributes.setAlpha(0f);
        addMenuItem(label, false, false, attributes, listener);
    }

    private PositionsInfo generateFinalPositions(int tileSize, int tilePadding, int vertTilePadding,
                                                 float textOffsetX, float textOffsetY, List<String> lines) {
        ArrayList<TargetPosition> finalPositions = new ArrayList<TargetPosition>();
        PositionsInfo positionsInfo = new PositionsInfo();
        float currentPosX = 0, currentPosY = 0;


        for (String line : lines) {
            for (int i = 0; i < line.length(); i++) {
                TargetPosition position = new TargetPosition();
                char pos = line.charAt(i);
                if (pos != ' ') {
                    if (currentPosX > maxTextWidth) {
                        maxTextWidth = currentPosX;
                    }
                    if (currentPosY > maxTextHeight) {
                        maxTextHeight = currentPosY;
                    }

                    position.setPosition(new Pair(currentPosX, currentPosY));
                    switch (pos) {
                        case '1':
                            position.setColor(Color.RED);
                            break;
                        case '2':
                            position.setColor(Color.BLUE);
                            break;
                        case '3':
                            position.setColor(Color.GREEN);
                            break;
                    }
                    finalPositions.add(position);
                }
                currentPosX += tileSize + tilePadding;
            }
            currentPosY += tileSize + vertTilePadding;
            currentPosX = 0;
        }

        maxTextHeight += tileSize;
        maxTextWidth += tileSize;

        textOffsetX = (width / 2) - (maxTextWidth / 2);
        positionsInfo.setFinalPositions(finalPositions);
        positionsInfo.setMaxTextHeight(maxTextHeight);
        positionsInfo.setMaxTextWidth(maxTextWidth);
        positionsInfo.setTextOffsetX(textOffsetX);
        positionsInfo.setTextOffsetY(textOffsetY);
        return positionsInfo;
    }

}
