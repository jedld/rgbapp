package com.rgb.matrix;

import android.util.Log;
import android.util.SparseArray;

import com.rgb.matrix.modifiers.HeightModifier;

import org.andengine.entity.Entity;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.ColorModifier;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.modifier.ScaleAtModifier;
import org.andengine.entity.modifier.SingleValueChangeEntityModifier;
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
public class GridSquare extends Entity {

    private static final float BAR_WIDTH = 5;
    private static final float BAR_HEIGHT = 12;
    private static final int MAX_AGE = 4;
    private static final int CONNECTOR_WIDTH = 16;
    private static final int CONNECTOR_HEIGHT = 8;
    public static final Color BUSTED_COLOR = new Color(36 / 255f, 0x36 / 255f, 0x36 / 255f);
    public static final Color P_INVALID_TILE_COLOR = new Color(0xef / 255f, 0xf0 / 255f, 0xeb / 255f);
    private static final String TAG = GridSquare.class.getName();
    private static final float INNER_RECT_THICKNESS = 10;
    private static final Color EMPTY_COLOR = Color.WHITE;
    private int multiplierColor;
    private Rectangle innerRectangle;

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
        if (tileType == GameMatrix.EMPTY) return true;
        return false;
    }


    public boolean isBustedOrEmpty() {
        if ((tileType == GameMatrix.EMPTY) || (tileType == GameMatrix.BUSTED)) return true;
        return false;
    }




    MainGrid matrix;
    SparseArray<Rectangle> ageRectangles = new SparseArray<Rectangle>(5);
    Rectangle rectangle;

    public void incrementAge() {
        if (age < MAX_AGE) {
            age++;
        } else {
            setTileType(GameMatrix.BUSTED);
            animateDie();
        }
        currentPointValue = getPointsString();
    }

    public GridSquare(int boardPositionX, int boardPositionY, float offset_x, float offset_y, MainGrid matrix, HashMap<String, Font> mFont, VertexBufferObjectManager vertexBuffer) {
        super(offset_x, offset_y);
        this.tileType = GameMatrix.EMPTY;
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

        rectangle = new Rectangle(4, 4, MainGrid.RECT_SIZE - 8, MainGrid.RECT_SIZE - 8, vertexBuffer);
        attachChild(rectangle);

        gridBorder = drawRect(0, 0, MainGrid.RECT_SIZE, MainGrid.RECT_SIZE, new Color(0xe6 / 255f, 0xe6 / 255f, 0xef / 255f), 2);
        attachChild(gridBorder);

        topConnector = new Rectangle(MainGrid.RECT_SIZE / 2 - CONNECTOR_WIDTH / 2, -CONNECTOR_HEIGHT / 2, CONNECTOR_WIDTH, CONNECTOR_HEIGHT, vertexBuffer);
        leftConnector = new Rectangle(-CONNECTOR_HEIGHT / 2, MainGrid.RECT_SIZE / 2 - CONNECTOR_WIDTH / 2, CONNECTOR_HEIGHT, CONNECTOR_WIDTH, vertexBuffer);
        rightConnector = new Rectangle(MainGrid.RECT_SIZE - CONNECTOR_HEIGHT / 2, MainGrid.RECT_SIZE / 2 - CONNECTOR_WIDTH / 2, CONNECTOR_HEIGHT, CONNECTOR_WIDTH, vertexBuffer);
        bottomConnector = new Rectangle(MainGrid.RECT_SIZE / 2 - CONNECTOR_WIDTH / 2, MainGrid.RECT_SIZE - CONNECTOR_HEIGHT / 2, CONNECTOR_WIDTH, CONNECTOR_HEIGHT, vertexBuffer);



        multiplierBorder = drawRect(3, 3, MainGrid.RECT_SIZE - 6, MainGrid.RECT_SIZE - 6, Color.BLACK, 3);
        multiplierBorder.setVisible(false);
        attachChild(multiplierBorder);

        attachChild(topConnector);
        attachChild(leftConnector);
        attachChild(rightConnector);
        attachChild(bottomConnector);
        int points = age + 1;
        valueText = new Text(MainGrid.RECT_SIZE - 35, MainGrid.RECT_SIZE - 25, mfont.get("points"), "+00", vertexBuffer);
        valueText.setText("+" + points);
        valueText.setColor(Color.BLACK);

        multiplierText = new Text( (MainGrid.RECT_SIZE / 2) - 15, (MainGrid.RECT_SIZE / 2) - 7, mfont.get("multiplier"), "X2", vertexBuffer);
        multiplierText.setColor(Color.RED);
        multiplierText.setVisible(false);

        if (getTileType() != GameMatrix.EMPTY && getTileType() != GameMatrix.BUSTED) {
            valueText.setVisible(true);
        } else {
            valueText.setVisible(false);
        }


        innerRectangle = new Rectangle(INNER_RECT_THICKNESS, INNER_RECT_THICKNESS, MainGrid.RECT_SIZE - INNER_RECT_THICKNESS * 2, MainGrid.RECT_SIZE - INNER_RECT_THICKNESS * 2, vertexBuffer);
        innerRectangle.setColor(Color.WHITE);

        attachChild(innerRectangle);

        attachChild(valueText);
        attachChild(multiplierText);
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
            case GameMatrix.BLUE_BLOCK:
                return true;
            case GameMatrix.RED_BLOCK:
                return true;
            case GameMatrix.GREEN_BLOCK:
                return true;
        }
        return false;
    }

    public void animateColorFlip(int newTile) {
        Color newColor = toColor(newTile);
        rectangle.registerEntityModifier( new ColorModifier(0.5f, rectangle.getColor(), newColor, new IEntityModifier.IEntityModifierListener() {
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

    public void animateScore(int previous, final int current) {

        if (previous == 0) {
            previous = current;
        }
        final int initialValue = previous;
        valueText.registerEntityModifier(

                new ScaleAtModifier(0.2f, 1f, 1.5f, valueText.getWidth()/2, valueText.getHeight() /2, new IEntityModifier.IEntityModifierListener() {
                    @Override
                    public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
                        valueText.setText("+" + initialValue);
                    }

                    @Override
                    public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                        valueText.registerEntityModifier(new ScaleAtModifier(0.2f, 1.5f, 1f, valueText.getWidth()/2, valueText.getHeight() /2, new IEntityModifier.IEntityModifierListener() {

                            @Override
                            public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
                                valueText.setText("+" + current);
                            }

                            @Override
                            public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {

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

    public void updateSelf() {

        topConnector.setVisible(false);
        leftConnector.setVisible(false);
        rightConnector.setVisible(false);
        bottomConnector.setVisible(false);
        multiplierText.setVisible(false);
        multiplierBorder.setVisible(false);
        innerRectangle.setVisible(false);
        rectangle.setAlpha(1f);

        if (!isEmpty()) {

            if (isRepeater()) {
                rectangle.setX(16);
                rectangle.setY(16);
                rectangle.setHeight(MainGrid.RECT_SIZE - 32);
                rectangle.setWidth(MainGrid.RECT_SIZE - 32);
            } else {
                rectangle.setX(4);
                rectangle.setY(4);
                rectangle.setHeight(MainGrid.RECT_SIZE - 8);
                rectangle.setWidth(MainGrid.RECT_SIZE - 8);
            }

            switch (tileType) {
                case GameMatrix.BLUE_BLOCK:
                case GameMatrix.BLUE_REPEATER_BLOCK:
                    rectangle.setColor(Color.BLUE);
                    break;
                case GameMatrix.RED_BLOCK:
                case GameMatrix.RED_REPEATER_BLOCK:
                    rectangle.setColor(Color.RED);
                    break;
                case GameMatrix.GREEN_REPEATER_BLOCK:
                case GameMatrix.GREEN_BLOCK:
                    rectangle.setColor(Color.GREEN);
                    break;
                case GameMatrix.MULTIPLIERX2:
                    rectangle.setColor(Color.WHITE);
                    multiplierText.setVisible(true);
                    multiplierText.setColor(Color.RED);
                    multiplierText.setText("X2");
                    multiplierBorder.setVisible(true);
                    break;
                case GameMatrix.MULTIPLIERX4_COLORED:
                    rectangle.setColor(toColor(getMultiplierColor()));
                    rectangle.setAlpha(0.5f);
                    multiplierText.setVisible(true);
                    multiplierText.setText("X4");
                    multiplierText.setColor(Color.WHITE);
                    multiplierBorder.setVisible(true);
                    break;
                case GameMatrix.BUSTED:
                    rectangle.setColor(BUSTED_COLOR);
                    rectangle.setAlpha(0.5f);
                    break;
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
            } else if ( (getTileType() == GameMatrix.MULTIPLIERX2) || (getTileType() == GameMatrix.MULTIPLIERX4_COLORED)) {
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
            rectangle.setHeight(MainGrid.RECT_SIZE);
            rectangle.setWidth(MainGrid.RECT_SIZE);
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

            float rect_height = MainGrid.RECT_SIZE - INNER_RECT_THICKNESS * 2;
            final float current_height = (rect_height * age)/MAX_AGE;
            if (getBoardPositionX() >=0 && innerRectangle.getHeight() < current_height) {
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

    private Color toColor(Integer block) {
        if (block == GameMatrix.RED_BLOCK) {
            return Color.RED;
        }
        if (block == GameMatrix.BLUE_BLOCK) {
            return Color.BLUE;
        }
        if (block == GameMatrix.GREEN_BLOCK) {
            return Color.GREEN;
        }
        if (block == GameMatrix.BUSTED) {
            return Color.BLACK;
        }
        Log.d(TAG, "Unknown color " + block);
        return null;
    }

    private Color getConnectorColor() {
        if (getTileType() == GameMatrix.MULTIPLIERX2) {
            return Color.BLACK;
        }
        if (getTileType() == GameMatrix.MULTIPLIERX4_COLORED) {
            return  toColor(getMultiplierColor());
        } else {
            return rectangle.getColor();
        }
    }

    public boolean isRepeater() {
        switch(tileType) {
            case GameMatrix.RED_REPEATER_BLOCK:
                return true;
            case GameMatrix.BLUE_REPEATER_BLOCK:
                return true;
            case GameMatrix.GREEN_REPEATER_BLOCK:
                return  true;
        }
        return false;
    }

    private boolean checkGridLocation(int boardX, int boardY) {
        if (getTileType() == GameMatrix.MULTIPLIERX2) {
            return isColoredTile(matrix.world[boardX][boardY].getTileType());
        } else if (getTileType() == GameMatrix.MULTIPLIERX4_COLORED) {
            return matrix.world[boardX][boardY].getTileType()== getMultiplierColor();
        } else {
            return (matrix.world[boardX][boardY].getTileType() == tileType) || (matrix.world[boardX][boardY].getTileType() +6 == tileType) ||
                    (matrix.world[boardX][boardY].getTileType() == tileType + 6);
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
        tileType = GameMatrix.EMPTY;
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
}
