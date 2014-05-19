package com.rgb.matrix.intro;

import android.util.Pair;

import com.rgb.matrix.ColorConstants;
import com.rgb.matrix.GridSquare;
import com.rgb.matrix.NextObject;
import com.rgb.matrix.interfaces.OnSequenceFinished;

import org.andengine.entity.Entity;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.ColorModifier;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.modifier.MoveModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.color.Color;
import org.andengine.util.modifier.IModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class LogoTiles extends Entity {

    public static final int TILE_SIZE = 10;
    public static final int TILE_PADDING = 3;
    private static final int VERT_TILE_PADDING = 3;

    private VertexBufferObjectManager vertexBufferObjectManager;
    private Random random;
    private float maxWidth;
    private float maxHeight;
    private int tileSize;
    private int tilePadding;
    private int vertTilePadding;

    PositionsInfo finalPositions, finalPositionsNoGap;
    ArrayList<Rectangle> rectangles = new ArrayList<Rectangle>();

    public LogoTiles(float pX, float pY, float maxWidth, float maxHeight, List<String> lines,
                     VertexBufferObjectManager vertexBufferObjectManager) {
        super(pX, pY);
        setup(maxWidth, maxHeight, TILE_SIZE, TILE_PADDING, VERT_TILE_PADDING, lines, vertexBufferObjectManager);
    }


    public LogoTiles(float pX, float pY, float maxWidth, float maxHeight, int tileSize,
                     int tilePadding, int vertTilePadding, List<String> lines,
                     VertexBufferObjectManager vertexBufferObjectManager) {
        super(pX, pY);
        setup(maxWidth, maxHeight, tileSize, tilePadding, vertTilePadding, lines, vertexBufferObjectManager);
    }

    private void setup(float maxWidth, float maxHeight, int tileSize, int tilePadding, int vertTilePadding, List<String> lines, VertexBufferObjectManager vertexBufferObjectManager) {
        this.tileSize = tileSize;
        this.tilePadding = tilePadding;
        this.vertTilePadding = vertTilePadding;

        this.vertexBufferObjectManager = vertexBufferObjectManager;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;

        random = new Random(System.nanoTime());
        float currentPosX = 0;
        float currentPosY = 0;

        finalPositions = generateFinalPositions(tileSize, tilePadding, vertTilePadding, lines, currentPosX, currentPosY);
        finalPositionsNoGap = generateFinalPositions(tileSize, 0, 0, lines, currentPosX, currentPosY);

        for (int i = 0; i < finalPositions.getFinalPositions().size(); i++) {
            Rectangle rectangle = new Rectangle(getRandomPosX(), getRandomPosY(), tileSize, tileSize, vertexBufferObjectManager);
            rectangle.setColor(getRandomColor());
            rectangle.setAlpha(0.7f);
            attachChild(rectangle);
            rectangles.add(rectangle);
        }
    }

    private PositionsInfo generateFinalPositions(int tileSize, int tilePadding, int vertTilePadding, List<String> lines, float currentPosX, float currentPosY) {
        ArrayList<TargetPosition> finalPositions = new ArrayList<TargetPosition>();
        PositionsInfo positionsInfo = new PositionsInfo();
        float maxTextWidth = 0, maxTextHeight = 0, textOffsetX = 0, textOffsetY = 0;

        for (String line : lines) {
            for (int i = 0; i < line.length(); i++) {
                char pos = line.charAt(i);
                if (pos == '?') {
                    if (currentPosX > maxTextWidth) {
                        maxTextWidth = currentPosX;
                    }
                    if (currentPosY > maxTextHeight) {
                        maxTextHeight = currentPosY;
                    }
                    TargetPosition target = new TargetPosition();
                    target.setPosition(new Pair(currentPosX, currentPosY));
                    finalPositions.add(target);
                }
                currentPosX += tileSize + tilePadding;
            }
            currentPosY += tileSize + vertTilePadding;
            currentPosX = 0;
        }

        maxTextHeight += tileSize;
        maxTextWidth += tileSize;

        textOffsetX = (maxWidth / 2) - (maxTextWidth / 2);
        textOffsetY = (maxHeight / 2) - (maxTextHeight / 2);
        positionsInfo.setFinalPositions(finalPositions);
        positionsInfo.setMaxTextHeight(maxTextHeight);
        positionsInfo.setMaxTextWidth(maxTextWidth);
        positionsInfo.setTextOffsetX(textOffsetX);
        positionsInfo.setTextOffsetY(textOffsetY);
        return positionsInfo;
    }

    public void easeOutSequence(final OnSequenceFinished listener) {
        int index = 0;
        for (final Rectangle rect : rectangles) {
            TargetPosition finalPost = finalPositionsNoGap.getFinalPositions().get(index++);

            rect.registerEntityModifier(new MoveModifier(1f, rect.getX(), finalPost.getPosition().first + finalPositionsNoGap.getTextOffsetX(),
                    rect.getY(), finalPost.getPosition().second + finalPositionsNoGap.getTextOffsetY(), new IEntityModifier.IEntityModifierListener() {
                @Override
                public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {

                }

                @Override
                public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                    rect.registerEntityModifier(new ColorModifier(1f, rect.getColor(), Color.BLACK, new IEntityModifier.IEntityModifierListener() {

                        @Override
                        public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {

                        }

                        @Override
                        public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                            listener.completed();
                        }
                    }));
                }
            }
            ));


        }
    }

    @Override
    public void setAlpha(float alpha) {
        for (Rectangle rect : rectangles) {
            rect.setAlpha(alpha);
        }
    }

    @Override
    public void setColor(Color color) {
        for (Rectangle rect : rectangles) {
            rect.setColor(color);
        }
    }

    public void startAnimationSequence(final OnSequenceFinished listener) {
        int index = 0;
        for (Rectangle rect : rectangles) {
            TargetPosition finalPost = finalPositions.getFinalPositions().get(index++);

            IEntityModifier.IEntityModifierListener entityModifierListener = new IEntityModifier.IEntityModifierListener() {
                @Override
                public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {

                }

                @Override
                public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                    easeOutSequence(listener);
                }
            };
            rect.registerEntityModifier(new MoveModifier(2f, rect.getX(), finalPost.getPosition().first + finalPositions.getTextOffsetX(),
                    rect.getY(), finalPost.getPosition().second + finalPositions.getTextOffsetY(), entityModifierListener
            ));
        }
    }

    private float getRandomPosY() {
        int randomY = random.nextInt((int) maxHeight);
        return randomY;
    }

    private float getRandomPosX() {
        int randomX = random.nextInt((int) maxWidth);
        return randomX;

    }

    private Color getRandomColor() {
        int randColor = random.nextInt(3);
        switch (randColor) {
            case 0:
                return ColorConstants.RED;
            case 1:
                return ColorConstants.BLUE;
            case 2:
                return ColorConstants.GREEN;
        }
        return null;
    }


}
