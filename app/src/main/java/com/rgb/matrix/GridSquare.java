package com.rgb.matrix;

import android.util.Log;
import android.util.SparseArray;

import com.rgb.matrix.interfaces.BoundedEntity;
import com.rgb.matrix.interfaces.OnSequenceFinished;
import com.rgb.matrix.modifiers.HeightModifier;

import org.andengine.entity.Entity;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.modifier.ColorModifier;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.modifier.ScaleAtModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.text.Text;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;
import org.andengine.util.modifier.IModifier;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by joseph on 4/21/14.
 */
public class GridSquare extends BoundedEntity {

    public static final int RED_BLOCK = 1;
    public static final int GREEN_BLOCK = 2;
    public static final int BLUE_BLOCK = 3;

    public static final int BUSTED = 4;
    public static final int MULTIPLIERX2 = 5;
    public static final int ERASER = 6;
    public static final int RED_REPEATER_BLOCK = 7;
    public static final int GREEN_REPEATER_BLOCK = 8;
    public static final int BLUE_REPEATER_BLOCK = 9;
    public static final int MULTIPLIERX4_COLORED = 10;
    public static final int EMPTY = 0;

    private static final int MAX_AGE = 4;
    public static final Color BUSTED_COLOR = new Color(36 / 255f, 0x36 / 255f, 0x36 / 255f);
    public static final Color P_INVALID_TILE_COLOR = new Color(0xef / 255f, 0xf0 / 255f, 0xeb / 255f);
    private static final String TAG = GridSquare.class.getName();
    private static final Color EMPTY_COLOR = Color.WHITE;
    public static final Color GRID_BORDER_COLOR = new Color(0xe6 / 255f, 0xe6 / 255f, 0xef / 255f);
    private final float originalOffsetX;
    private final float originalOffsetY;
    private int multiplierColor;
    private Rectangle innerRectangle;
    private Rectangle gameOverRect;
    private Text chainBonusRepeaterText;
    private Rectangle repeaterContainer;
    private Rectangle repeaterCenterContainer;

    public int getBoardPositionX() {
        return boardPositionX;
    }

    public int getBoardPositionY() {
        return boardPositionY;
    }

    private final int boardPositionX, boardPositionY;
    private final VertexBufferObjectManager vertexBuffer;
    private final HashMap<String, Font> mfont;
    private boolean multiplierConnector[] = {false, false, false, false};
    private Rectangle topConnector;
    private Rectangle leftConnector;
    private Rectangle rightConnector;
    private Rectangle bottomConnector;
    private Text valueText;
    private Text multiplierText;

    private Entity multiplierBorder;
    private Entity gridBorder;
    private Rectangle repeaterRectangle;
    private CharSequence currentPointValue = "";


    int tileType;
    int age;
    private int bonus;


    public ArrayList<GridSquare> getBonusSources() {
        return bonusSource;
    }

    private ArrayList<GridSquare> bonusSource = new ArrayList<GridSquare>();

    public int getTileType() {
        return tileType;
    }

    public void setTileType(int tileType) {
        this.tileType = tileType;
    }

    public boolean isEmpty() {
        if (tileType == EMPTY) return true;
        return false;
    }


    public GridSquare clone() {
        GridSquare gridSquare = new GridSquare(boardPositionX, boardPositionY, originalOffsetX, originalOffsetY, matrix, mfont, vertexBuffer);
        gridSquare.setMultiplierColor(getMultiplierColor());
        gridSquare.setTileType(getTileType());
        gridSquare.setAge(age);
        gridSquare.setBonus(getBonus());
        return gridSquare;
    }

    public boolean isBustedOrEmpty() {
        if ((tileType == EMPTY) || (tileType == BUSTED)) return true;
        return false;
    }


    MainGrid matrix;
    SparseArray<Rectangle> ageRectangles = new SparseArray<Rectangle>(5);
    Rectangle rectangle;

    public void incrementAge() {
        if (age < MAX_AGE) {
            age++;
        } else {
            setTileType(BUSTED);
            animateDie();
        }
        currentPointValue = getPointsString();
    }

    public GridSquare(int boardPositionX, int boardPositionY, float offset_x, float offset_y, MainGrid matrix, HashMap<String, Font> mFont, VertexBufferObjectManager vertexBuffer) {
        super(offset_x, offset_y);
        this.originalOffsetX = offset_x;
        this.originalOffsetY = offset_y;
        this.tileType = EMPTY;
        this.age = 0;
        this.mfont = mFont;
        this.bonus = 0;
        this.boardPositionX = boardPositionX;
        this.boardPositionY = boardPositionY;
        this.matrix = matrix;
        this.vertexBuffer = vertexBuffer;
        setupEntities();
//        Color colors[] = {new Color(0xe0 / 255f, 0xe0 / 255f, 0xe0 / 255f),
//                new Color(0xa3 / 255f, 0xa3 / 255f, 0xa3 / 255f), new Color(0x70 / 255f, 0x70 / 255f, 0x70 / 255f),
//                new Color(0x8c / 255f, 0x00 / 255f, 0x00 / 255f)};
//
//        for (int i = 0; i < MAX_AGE; i++) {
//            Rectangle rect = new Rectangle(i * 7 + 2, 5, BAR_WIDTH, BAR_HEIGHT, vertexBuffer);
//            rect.setColor(colors[i]);
//            rect.setVisible(false);
//            ageRectangles.append(i, rect);
//            rectangle.attachChild(rect);
//        }
    }

    public void setAge(int age) {
        this.age = age;
        currentPointValue = getPointsString();
    }

    public void setupEntities() {

        setupRepeaterEntities();

        rectangle = new Rectangle(ObjectDimensions.szTitleBorderMargin, ObjectDimensions.szTitleBorderMargin, matrix.getRectangleTileSizeInPixels() - ObjectDimensions.szTitleBorderMargin * 2, matrix.getRectangleTileSizeInPixels() - ObjectDimensions.szTitleBorderMargin * 2, vertexBuffer);

        attachChild(rectangle);

        gameOverRect = new Rectangle(ObjectDimensions.szTitleBorderMargin, ObjectDimensions.szTitleBorderMargin, matrix.getRectangleTileSizeInPixels() - ObjectDimensions.szTitleBorderMargin * 2, matrix.getRectangleTileSizeInPixels() - ObjectDimensions.szTitleBorderMargin * 2, vertexBuffer);
        gameOverRect.setColor(Color.BLACK);
        gameOverRect.setVisible(false);

        gridBorder = drawRect(0, 0, matrix.getRectangleTileSizeInPixels(), matrix.getRectangleTileSizeInPixels(), GRID_BORDER_COLOR, 2);
        attachChild(gridBorder);

        topConnector = new Rectangle(matrix.getRectangleTileSizeInPixels() / 2 - ObjectDimensions.szConnectorWidth / 2, -ObjectDimensions.szConnectorHeight / 2, ObjectDimensions.szConnectorWidth, ObjectDimensions.szConnectorHeight, vertexBuffer);
        leftConnector = new Rectangle(-ObjectDimensions.szConnectorHeight / 2, matrix.getRectangleTileSizeInPixels() / 2 - ObjectDimensions.szConnectorWidth / 2, ObjectDimensions.szConnectorHeight, ObjectDimensions.szConnectorWidth, vertexBuffer);
        rightConnector = new Rectangle(matrix.getRectangleTileSizeInPixels() - ObjectDimensions.szConnectorHeight / 2, matrix.getRectangleTileSizeInPixels() / 2 - ObjectDimensions.szConnectorWidth / 2, ObjectDimensions.szConnectorHeight, ObjectDimensions.szConnectorWidth, vertexBuffer);
        bottomConnector = new Rectangle(matrix.getRectangleTileSizeInPixels() / 2 - ObjectDimensions.szConnectorWidth / 2, matrix.getRectangleTileSizeInPixels() - ObjectDimensions.szConnectorHeight / 2, ObjectDimensions.szConnectorWidth, ObjectDimensions.szConnectorHeight, vertexBuffer);


        float borderSize = matrix.getRectangleTileSizeInPixels() - ObjectDimensions.szMultiplierBorderMargin * 2;
        multiplierBorder = new HollowRectangle(matrix.getRectangleTileSizeInPixels() / 2 - borderSize / 2, matrix.getRectangleTileSizeInPixels() / 2 - borderSize / 2, matrix.getRectangleTileSizeInPixels() - 8, matrix.getRectangleTileSizeInPixels() - 8, Color.BLACK, 4, vertexBuffer);
        multiplierBorder.setVisible(false);
        attachChild(multiplierBorder);

        attachChild(topConnector);
        attachChild(leftConnector);
        attachChild(rightConnector);
        attachChild(bottomConnector);
        int points = age + 1;
        valueText = new Text(matrix.getRectangleTileSizeInPixels() - 35, matrix.getRectangleTileSizeInPixels() - 25, mfont.get("points"), "+000", vertexBuffer);
        valueText.setText("+" + points);
        valueText.setColor(Color.BLACK);

        multiplierText = new Text(0, 0, mfont.get("multiplier"), "X2", vertexBuffer);
        multiplierText.setX(matrix.getRectangleTileSizeInPixels() / 2 - multiplierText.getWidth() / 2);
        multiplierText.setY(matrix.getRectangleTileSizeInPixels() / 2 - multiplierText.getHeight() / 2);
        multiplierText.setColor(ColorConstants.RED);
        multiplierText.setVisible(false);


        if (getTileType() != EMPTY && getTileType() != BUSTED) {
            valueText.setVisible(true);
        } else {
            valueText.setVisible(false);
        }

        innerRectangle = new Rectangle(ObjectDimensions.szInnerRectThickness, ObjectDimensions.szInnerRectThickness, matrix.getRectangleTileSizeInPixels() - ObjectDimensions.szInnerRectThickness * 2, matrix.getRectangleTileSizeInPixels() - ObjectDimensions.szInnerRectThickness * 2, vertexBuffer);
        innerRectangle.setColor(Color.WHITE);

        attachChild(innerRectangle);

        attachChild(valueText);
        attachChild(multiplierText);
        attachChild(gameOverRect);
    }

    private void setupRepeaterEntities() {
        float szRepeater = matrix.getRectangleTileSizeInPixels() - ObjectDimensions.szTitleBorderMargin * 2;
        repeaterContainer = new Rectangle(ObjectDimensions.szTitleBorderMargin, ObjectDimensions.szTitleBorderMargin, szRepeater, szRepeater, vertexBuffer);
        repeaterContainer.setVisible(false);
        attachChild(repeaterContainer);

        repeaterCenterContainer = new Rectangle(ObjectDimensions.szRepeaterCenterContainerMargin,
                ObjectDimensions.szRepeaterCenterContainerMargin,
                szRepeater - ObjectDimensions.szRepeaterCenterContainerMargin * 2,
                szRepeater - ObjectDimensions.szRepeaterCenterContainerMargin * 2, vertexBuffer);
        repeaterCenterContainer.setColor(Color.WHITE);
        repeaterCenterContainer.setVisible(true);
        repeaterContainer.attachChild(repeaterCenterContainer);
    }

    public Entity drawRect(float x, float y, float x1, float y1, Color color, float pLineWidth) {
        Entity entity = new Entity(x, y);
        Line line1 = new Line(x, y, x, y1, vertexBuffer);
        line1.setLineWidth(pLineWidth);
        line1.setColor(color);

        Line line2 = new Line(x, y, x1, y, vertexBuffer);
        line2.setLineWidth(pLineWidth);
        line2.setColor(color);

        Line line3 = new Line(x1, y, x1, y1, vertexBuffer);
        line3.setLineWidth(pLineWidth);
        line3.setColor(color);

        Line line4 = new Line(x, y1, x1, y1, vertexBuffer);
        line4.setLineWidth(pLineWidth);
        line4.setColor(color);

        entity.attachChild(line1);
        entity.attachChild(line2);
        entity.attachChild(line3);
        entity.attachChild(line4);
        return entity;
    }

    public boolean isColoredTile() {
        return isColoredTile(tileType);
    }

    public boolean isColoredTile(int tileType) {
        switch (tileType) {
            case BLUE_BLOCK:
                return true;
            case RED_BLOCK:
                return true;
            case GREEN_BLOCK:
                return true;
            case MULTIPLIERX4_COLORED:
                return true;
        }
        return false;
    }

    public void animateColorFlip(int newTile) {
        Color newColor = toColor(newTile);
        rectangle.registerEntityModifier(new ColorModifier(0.5f, rectangle.getColor(), newColor, new IEntityModifier.IEntityModifierListener() {
            @Override
            public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {

            }

            @Override
            public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {

            }
        }));
    }

    public void animateDie() {
        rectangle.registerEntityModifier(new ColorModifier(1f, rectangle.getColor(), BUSTED_COLOR, new IEntityModifier.IEntityModifierListener() {
            @Override
            public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {

            }

            @Override
            public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {

            }
        }));
    }

    public void animateScore(int previous, final int current, final int multiplierLevel) {

        if (previous == 0) {
            previous = current;
        }

        final int initialValue = previous;
        float toScale = 1.5f;

        if (multiplierLevel > 1) {
            valueText.setColor(ColorConstants.RED);
            toScale = 1.7f;
        } else {
            valueText.setColor(Color.BLACK);
        }

        final float finalToScale = toScale;
        valueText.registerEntityModifier(

                new ScaleAtModifier(0.2f, 1f, toScale, valueText.getWidth() / 2, valueText.getHeight() / 2, new IEntityModifier.IEntityModifierListener() {
                    @Override
                    public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {

                        valueText.setText("+" + initialValue * multiplierLevel);
                    }

                    @Override
                    public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                        valueText.registerEntityModifier(new ScaleAtModifier(0.2f, finalToScale, 1f, valueText.getWidth() / 2, valueText.getHeight() / 2, new IEntityModifier.IEntityModifierListener() {

                            @Override
                            public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
                                valueText.setText("+" + current);
                            }

                            @Override
                            public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                                valueText.setColor(Color.BLACK);
                            }
                        }));
                    }
                })
        );
    }


    public void setupMultiplier() {
        if (hasAdjacentTop()) {
            multiplierConnector[0] = true;
        } else {
            multiplierConnector[0] = false;
        }

        if (hasAdjacentLeft()) {
            multiplierConnector[1] = true;
        } else {
            multiplierConnector[1] = false;
        }

        if (hasAdjacentRight()) {
            multiplierConnector[2] = true;
        } else {
            multiplierConnector[2] = false;
        }

        if (hasAdjacentBottom()) {
            multiplierConnector[3] = true;
        } else {
            multiplierConnector[3] = false;
        }
    }

    public void animateGameOver() {
        gameOverRect.setVisible(true);
        gameOverRect.setAlpha(0);
        gameOverRect.registerEntityModifier(new AlphaModifier(1f, 0f, 0.5f));
    }

    public void animateRepeaterExpand(final OnSequenceFinished sequenceFinished) {
        rectangle.setScaleCenterX(MainGrid.getRepeaterSizeInPixels() / 2);
        rectangle.setScaleCenterY(MainGrid.getRepeaterSizeInPixels() / 2);
        repeaterContainer.registerEntityModifier(new AlphaModifier(1f, 1f, 0f, new IEntityModifier.IEntityModifierListener() {
            @Override
            public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {

            }

            @Override
            public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {

            }
        }));
        rectangle.registerEntityModifier(new ScaleModifier(1f, 1f, 1.5f, new IEntityModifier.IEntityModifierListener() {
            @Override
            public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {

            }

            @Override
            public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {

                sequenceFinished.completed();
            }
        }));
    }

    public void updateSelf() {
        gameOverRect.setVisible(false);
        topConnector.setVisible(false);
        leftConnector.setVisible(false);
        rightConnector.setVisible(false);
        bottomConnector.setVisible(false);
        multiplierText.setVisible(false);
        multiplierBorder.setVisible(false);
        repeaterContainer.setVisible(false);
        innerRectangle.setVisible(false);
        rectangle.setAlpha(1f);
        rectangle.setScale(1f);
        repeaterContainer.setAlpha(1f);


        if (!isEmpty()) {
            switch (tileType) {
                case BLUE_BLOCK:
                case BLUE_REPEATER_BLOCK:
                    rectangle.setColor(ColorConstants.BLUE);
                    break;
                case RED_BLOCK:
                case RED_REPEATER_BLOCK:
                    rectangle.setColor(ColorConstants.RED);
                    break;
                case GREEN_REPEATER_BLOCK:
                case GREEN_BLOCK:
                    rectangle.setColor(ColorConstants.GREEN);
                    break;
                case MULTIPLIERX2:
                    rectangle.setColor(Color.WHITE);
                    multiplierText.setVisible(true);
                    multiplierText.setColor(ColorConstants.RED);
                    multiplierText.setText("X2");
                    multiplierBorder.setVisible(true);
                    break;
                case MULTIPLIERX4_COLORED:
                    rectangle.setColor(toColor(getMultiplierColor()));
                    multiplierText.setVisible(true);
                    multiplierText.setText("X4");
                    multiplierText.setColor(Color.WHITE);
                    rectangle.setAlpha(0.7f);
                    break;
                case BUSTED:
                    rectangle.setColor(BUSTED_COLOR);
                    rectangle.setAlpha(0.5f);
                    break;
            }

            if (isRepeater()) {
                rectangle.setX(matrix.getRectangleTileSizeInPixels() / 2 - MainGrid.getRepeaterSizeInPixels() / 2);
                rectangle.setY(matrix.getRectangleTileSizeInPixels() / 2 - MainGrid.getRepeaterSizeInPixels() / 2);
                rectangle.setHeight(MainGrid.getRepeaterSizeInPixels());
                rectangle.setWidth(MainGrid.getRepeaterSizeInPixels());
                repeaterContainer.setColor(rectangle.getColor());
                repeaterContainer.setVisible(true);

            } else {
                rectangle.setX(ObjectDimensions.szTitleBorderMargin);
                rectangle.setY(ObjectDimensions.szTitleBorderMargin);
                rectangle.setHeight(matrix.getRectangleTileSizeInPixels() - ObjectDimensions.szTitleBorderMargin * 2);
                rectangle.setWidth(matrix.getRectangleTileSizeInPixels() - ObjectDimensions.szTitleBorderMargin * 2);
            }

            if (isColoredTile() || isRepeater()) {

                if (hasAdjacentTop()) {
                    topConnector.setColor(getConnectorColor());
                    topConnector.setVisible(true);
                }

                if (hasAdjacentLeft()) {
                    leftConnector.setColor(getConnectorColor());
                    leftConnector.setVisible(true);
                }

//                if (hasAdjacentRight()) {
//                    rightConnector.setColor(getConnectorColor());
//                    rightConnector.setVisible(true);
//                }

//                if (hasAdjacentBottom()) {
//                    bottomConnector.setColor(getConnectorColor());
//                    bottomConnector.setVisible(true);
//                }

                rectangle.setAlpha(0.7f);
            } else if ((getTileType() == MULTIPLIERX2) || (getTileType() == MULTIPLIERX4_COLORED)) {
                if (multiplierConnector[0]) {
                    topConnector.setColor(getConnectorColor());
                    topConnector.setVisible(true);
                }

                if (multiplierConnector[1]) {
                    leftConnector.setColor(getConnectorColor());
                    leftConnector.setVisible(true);
                }

                if (multiplierConnector[2]) {
                    rightConnector.setColor(getConnectorColor());
                    rightConnector.setVisible(true);
                }

                if (multiplierConnector[3]) {
                    bottomConnector.setColor(getConnectorColor());
                    bottomConnector.setVisible(true);
                }
            }
        } else {
            rectangle.setX(0);
            rectangle.setY(0);
            rectangle.setHeight(matrix.getRectangleTileSizeInPixels());
            rectangle.setWidth(matrix.getRectangleTileSizeInPixels());
            gridBorder.setVisible(true);
            if (matrix.isValid(boardPositionX, boardPositionY)) {
                rectangle.setColor(Color.WHITE);
            } else {
                rectangle.setColor(P_INVALID_TILE_COLOR);
            }
        }

//        for (int i = 0; i < MAX_AGE; i++) {
//            ageRectangles.get(i).setVisible(false);
//        }

        if (isColoredTile()) {
            innerRectangle.setVisible(true);
            innerRectangle.setColor(Utils.getLighter(getTileType()));

            float rect_height = matrix.getRectangleTileSizeInPixels() - ObjectDimensions.szInnerRectThickness * 2;
            final float current_height = computeHeight(rect_height);
            if (getBoardPositionX() >= 0 && innerRectangle.getHeight() < current_height) {
                innerRectangle.registerEntityModifier(new HeightModifier(1f, current_height - innerRectangle.getHeight(), new IEntityModifier.IEntityModifierListener() {
                    @Override
                    public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {

                    }

                    @Override
                    public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                        innerRectangle.setHeight(current_height);
                    }
                }));
            } else {
                innerRectangle.setHeight(current_height);
            }
//            for (int i = 0; i < age && i < 5; i++) {
//                ageRectangles.get(i).setVisible(true);
//            }
            if (getPoints() > 0) {
                valueText.setText(currentPointValue);
                valueText.setVisible(true);
            } else {
                valueText.setVisible(false);
            }
        } else {
            valueText.setVisible(false);
        }
    }

    private float computeHeight(float rect_height) {
        if (age > MAX_AGE) {
            return rect_height;
        }
        return (rect_height * age) / MAX_AGE;
    }

    private Color toColor(Integer block) {
        if (block == RED_BLOCK) {
            return ColorConstants.RED;
        }
        if (block == BLUE_BLOCK) {
            return ColorConstants.BLUE;
        }
        if (block == GREEN_BLOCK) {
            return ColorConstants.GREEN;
        }
        if (block == BUSTED) {
            return Color.BLACK;
        }
        Log.d(TAG, "Unknown color " + block);
        return null;
    }

    private Color getConnectorColor() {
        if (getTileType() == MULTIPLIERX2) {
            return Color.BLACK;
        }
        if (getTileType() == MULTIPLIERX4_COLORED) {
            return toColor(getMultiplierColor());
        } else {
            return rectangle.getColor();
        }
    }

    public boolean isRepeater() {
        switch (tileType) {
            case RED_REPEATER_BLOCK:
                return true;
            case BLUE_REPEATER_BLOCK:
                return true;
            case GREEN_REPEATER_BLOCK:
                return true;
        }
        return false;
    }

    private boolean checkGridLocation(int boardX, int boardY) {
        if (getTileType() == MULTIPLIERX2) {
            return isColoredTile(matrix.world[boardX][boardY].getTileType());
        } else if (getTileType() == MULTIPLIERX4_COLORED) {
            return (matrix.world[boardX][boardY].getTileType() == getMultiplierColor()) ||
                    (matrix.world[boardX][boardY].getTileType() == getMultiplierColor() + 6);
        } else {
            return (matrix.world[boardX][boardY].getTileType() == tileType) || (matrix.world[boardX][boardY].getTileType() + 6 == tileType) ||
                    (matrix.world[boardX][boardY].getTileType() == tileType + 6
                            ||
                            (matrix.world[boardX][boardY].getTileType() == MULTIPLIERX4_COLORED && matrix.world[boardX][boardY].getMultiplierColor() == tileType)
                    || (matrix.world[boardX][boardY].getTileType() == MULTIPLIERX4_COLORED && matrix.world[boardX][boardY].getMultiplierColor() + 6 == tileType)
                    );
        }
    }


    private boolean hasAdjacentTop() {
        if (boardPositionY > 0) {
            return checkGridLocation(boardPositionX, boardPositionY - 1);
        }
        return false;
    }

    private boolean hasAdjacentLeft() {
        if (boardPositionX > 0) {
            return checkGridLocation(boardPositionX - 1, boardPositionY);
        }
        return false;
    }


    private boolean hasAdjacentRight() {
        if (boardPositionX >= 0 && boardPositionX < matrix.getMaxPositionX() - 1) {
            return checkGridLocation(boardPositionX + 1, boardPositionY);
        }
        return false;
    }

    private boolean hasAdjacentBottom() {
        if (boardPositionY >= 0 && boardPositionY < matrix.getMaxPositionY() - 1) {
            return checkGridLocation(boardPositionX, boardPositionY + 1);
        }
        return false;
    }

    public void reset() {
        age = 0;
        bonus = 0;
        bonusSource = new ArrayList<GridSquare>();
        tileType = EMPTY;
    }

    public int getPoints() {
        return age;
    }

    public int getTotalPoints() {
        return getPoints() + getBonus();
    }

    public int addBonus(GridSquare bonusSource, int i) {
        if (bonus < 96) {
            this.bonus += i;
        }
        this.bonusSource.add(bonusSource);
        currentPointValue = getPointsString();
        return getPoints() + getBonus();
    }

    private String getPointsString() {
        return "+" + ((int) getPoints() + getBonus());
    }

    public int getBonus() {
        return this.bonus;
    }

    public void setBonus(int bonus) {
        this.bonus = bonus;
        currentPointValue = getPointsString();
    }

    public void setMultiplierColor(int color) {
        this.multiplierColor = color;
    }

    public int getMultiplierColor() {
        return multiplierColor;
    }

    public void animateEmpty() {
        rectangle.registerEntityModifier(new ColorModifier(1f, rectangle.getColor(), EMPTY_COLOR, new IEntityModifier.IEntityModifierListener() {
            @Override
            public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {

            }

            @Override
            public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {

            }
        }));
    }

    @Override
    public float getWidth() {
        return matrix.getRectangleTileSizeInPixels() - ObjectDimensions.szTitleBorderMargin * 2;
    }

    @Override
    public float getHeight() {
        return matrix.getRectangleTileSizeInPixels() - ObjectDimensions.szTitleBorderMargin * 2;
    }
}
