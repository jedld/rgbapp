package com.rgb.matrix.intro;

import android.util.Pair;

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
import org.andengine.util.color.Color;
import org.andengine.util.modifier.IModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by joseph on 5/2/14.
 */
public class LogoTiles extends Entity {

    public static final int TILE_SIZE = 10;
    public static final int TILE_PADDING = 3;
    private static final int VERT_TILE_PADDING = 3;

    private final VertexBufferObjectManager vertexBufferObjectManager;
    private final Random random;
    private final float maxWidth;
    private final float maxHeight;

    ArrayList<Pair<Float, Float>> finalPositions = new ArrayList<Pair<Float, Float>>();
    ArrayList<Rectangle> rectangles = new ArrayList<Rectangle>();

    float maxTextWidth = 0;
    float maxTextHeight = 0;
    float textOffsetX = 0;
    float textOffsetY = 0;


    public LogoTiles(float pX, float pY, float maxWidth, float maxHeight, List<String> lines, VertexBufferObjectManager vertexBufferObjectManager) {
        super(pX, pY);

        this.vertexBufferObjectManager = vertexBufferObjectManager;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;

        random = new Random(System.nanoTime());
        float currentPosX = 0;
        float currentPosY = 0;

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
                    finalPositions.add(new Pair(currentPosX, currentPosY));
                }
                currentPosX += TILE_SIZE + TILE_PADDING;
            }
            currentPosY += TILE_SIZE + VERT_TILE_PADDING;
            currentPosX = 0;
        }

        maxTextHeight += TILE_SIZE;
        maxTextWidth += TILE_SIZE;

        textOffsetX = (maxWidth / 2) - (maxTextWidth / 2);
        textOffsetY = (maxHeight / 2) - (maxTextHeight / 2);

        for (int i = 0; i < finalPositions.size(); i++) {
            Rectangle rectangle = new Rectangle(getRandomPosX(), getRandomPosY(), TILE_SIZE, TILE_SIZE, vertexBufferObjectManager);
            rectangle.setColor(getRandomColor());
            rectangle.setAlpha(0.7f);
            attachChild(rectangle);
            rectangles.add(rectangle);
        }
    }

    public void easeOutSequence(final OnSequenceFinished listener) {
        for (Rectangle rect : rectangles) {
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

    public void startAnimationSequence(final OnSequenceFinished listener) {
        int index = 0;
        for (Rectangle rect : rectangles) {
            Pair<Float, Float> finalPost = finalPositions.get(index++);

            IEntityModifier.IEntityModifierListener entityModifierListener = new IEntityModifier.IEntityModifierListener() {
                @Override
                public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {

                }

                @Override
                public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                    easeOutSequence(listener);
                }
            };
            rect.registerEntityModifier(new MoveModifier(3f, rect.getX(), finalPost.first + textOffsetX,
                    rect.getY(), finalPost.second + textOffsetY, entityModifierListener
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
                return Color.RED;
            case 1:
                return Color.BLUE;
            case 2:
                return Color.GREEN;
        }
        return null;
    }


}
