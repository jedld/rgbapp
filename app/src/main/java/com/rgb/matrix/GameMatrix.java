package com.rgb.matrix;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;

import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.modifier.ScaleAtModifier;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.color.Color;
import org.andengine.util.modifier.IModifier;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

class NextObject {

    private int tileType;
    private int age;

    public int getTileType() {
        return tileType;
    }

    public void setTileType(int tileType) {
        this.tileType = tileType;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}

public class GameMatrix implements IUpdateHandler {
    private static final String TAG = GameMatrix.class.getName();

    private final int gridWidth;
    private final int gridHeight;
    private final int offset_x;
    private final int offset_y;

    private final Random random;
    private final SharedPreferences sharedPrefs;
    private final Font mFont;
    private Text scoreText;
    private Text gameOverText;
    private boolean inProgress = false;
    private Random random2;

    public Scene getScene() {
        return scene;
    }

    public VertexBufferObjectManager getVertexBuffer() {
        return vertexBuffer;
    }

    private final Scene scene;
    private final VertexBufferObjectManager vertexBuffer;
    Context context;
    GridSquare world[][];
    Rectangle rectangles[][];
    GridSquare queueRectangles[] = new GridSquare[QUEUE_SIZE];
    RectangleButton newGameButton;
    public static final int RED_BLOCK = 1;
    public static final int BLUE_BLOCK = 2;
    public static final int GREEN_BLOCK = 3;
    public static final int BUSTED = 4;
    public static final int MULTIPLIERX4 = 5;
    public static final int ERASER = 6;
    public static final int RED_REPEATER_BLOCK = 7;
    public static final int BLUE_REPEATER_BLOCK = 8;
    public static final int GREEN_REPEATER_BLOCK = 9;

    public static final int QUEUE_SIZE = 6;
    public static final int RECT_SIZE = 54;
    public static final int EMPTY = 0;
    public int score = 0;
    String scoreTextString = "";

    Vector<NextObject> blockQueue = new Vector<NextObject>();
    HashMap<String, Font> fontDictionary = new HashMap<String, Font>();

    static GameMatrix instance;

    protected GameMatrix(Context context, Scene scene, HashMap<String, Font> fontDictionary, VertexBufferObjectManager vertexBuffer, int gridWidth, int gridHeight, int offset_x, int offset_y) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.offset_x = offset_x;
        this.offset_y = offset_y;
        this.scene = scene;
        this.context = context;
        this.fontDictionary = fontDictionary;
        this.vertexBuffer = vertexBuffer;
        this.mFont = fontDictionary.get("score");

        sharedPrefs = context.getSharedPreferences("high_score", Context.MODE_PRIVATE);
        world = new GridSquare[gridWidth][gridHeight];
        rectangles = new Rectangle[gridWidth][gridHeight];
        random = new Random(System.nanoTime());
        random2 = new Random(System.nanoTime() + 2);
        setupWorld();
        newGameButton = new RectangleButton(offset_x + (gridWidth * RECT_SIZE) - 170, offset_y + (gridHeight * RECT_SIZE) + 100, 170, 54, vertexBuffer, mFont, "New Game");
        scene.attachChild(newGameButton);
    }

    public void fillQueue() {
        int fillSize = QUEUE_SIZE - blockQueue.size();
        for (int i = 0; i < fillSize; i++) {
            blockQueue.add(getNextObject(false));
        }
    }

    public static GameMatrix getInstance(Context context, Scene scene, HashMap<String, Font> fontDictionary, VertexBufferObjectManager vertexBuffer, int width, int height, int offset_x, int offset_y) {
        if (instance == null) {
            instance = new GameMatrix(context, scene, fontDictionary, vertexBuffer, width, height, offset_x, offset_y);
        }
        return instance;
    }

    public void drawRect(float x, float y, float x1, float y1, Color color) {
        Line line1 = new Line(x, y, x, y1, vertexBuffer);
        line1.setLineWidth(5f);
        line1.setColor(color);

        Line line2 = new Line(x, y, x1, y, vertexBuffer);
        line2.setColor(color);
        line2.setLineWidth(5f);

        Line line3 = new Line(x1, y, x1, y1, vertexBuffer);
        line3.setColor(color);
        line3.setLineWidth(5f);

        Line line4 = new Line(x, y1, x1, y1, vertexBuffer);
        line4.setColor(color);
        line4.setLineWidth(5f);

        scene.attachChild(line1);
        scene.attachChild(line2);
        scene.attachChild(line3);
        scene.attachChild(line4);
    }

    public void resetWorld() {
        score = 0;
        scoreTextString = "Score: 0000";
        for (int i = 0; i < gridWidth; i++) {
            for (int i2 = 0; i2 < gridHeight; i2++) {
                world[i][i2].reset();
            }
        }
        blockQueue.clear();
        gameOverText.setVisible(false);
        fillQueue();
    }

    public int getHighScore() {
        return sharedPrefs.getInt("high_score", 0);
    }

    public void saveHighScore(int highScore) {
        sharedPrefs.edit().putInt("high_score", highScore).commit();
    }

    public void setupWorld() {
        for (int i = 0; i < gridWidth; i++) {
            for (int i2 = 0; i2 < gridHeight; i2++) {
                float rect_x = offset_x + (i * GameMatrix.RECT_SIZE);
                float rect_y = offset_y + (i2 * GameMatrix.RECT_SIZE);
                GridSquare gridSquare = new GridSquare(i, i2, rect_x, rect_y, this, fontDictionary, vertexBuffer);
                scene.attachChild(gridSquare);
                world[i][i2] = gridSquare;
            }
        }

        for (int i = 0; i < QUEUE_SIZE; i++) {

            float rect_x = offset_x + (i * RECT_SIZE);
            float rect_y = offset_y + (gridHeight * RECT_SIZE) + 20;
            float rect_x1 = rect_x + RECT_SIZE;
            float rect_y1 = rect_y + RECT_SIZE;
            GridSquare gridSquare = new GridSquare(-1, -1, rect_x + 4, rect_y + 4, this, fontDictionary, vertexBuffer);
            if (i == 0) {
                drawRect(rect_x, rect_y, rect_x1, rect_y1, new Color(0xbd / 255f, 0xbd / 255f, 0xbd / 255f));
            }

            queueRectangles[i] = gridSquare;
            scene.attachChild(gridSquare);
        }

        int textOffset = offset_y + (gridHeight * RECT_SIZE) + 75;
        String highScore = StringUtils.leftPad(Integer.toString(getHighScore()), 4, "0");
        scoreText = new Text(0, textOffset, mFont, "Score: 0000 High: " + highScore, vertexBuffer);
        scoreText.setText("Score: 0000 High: " + highScore);
        scoreText.setColor(Color.BLACK);
        scene.attachChild(scoreText);

        textOffset = offset_y + (gridHeight * RECT_SIZE) / 2;

        gameOverText = new Text(0, textOffset, mFont, "GAME OVER", vertexBuffer);
        gameOverText.setColor(Color.RED);
        gameOverText.setVisible(false);
        scene.attachChild(gameOverText);

        blockQueue.clear();
        fillQueue();
        this.score = 0;
        populateInitial();

    }

    private void populateInitial() {
        int totalInitial = random.nextInt(3) + 3;
        for (int i2 = 0; i2 < totalInitial; i2++) {
            int x = random.nextInt(gridWidth);
            int y = random.nextInt(gridHeight);
            updateWorld(x, y, getNextObject(true), true);
        }
    }

    private int[][] getVisitMap() {
        int newMap[][] = new int[gridWidth][gridHeight];
        for (int i = 0; i < gridWidth; i++) {
            for (int i2 = 0; i2 < gridHeight; i2++) {
                newMap[i][i2] = 0;
            }
        }
        return newMap;
    }

    private int[][] cloneMap(int map[][]) {
        int clonedMap[][] = new int[gridWidth][gridHeight];
        for (int i = 0; i < gridWidth; i++) {
            for (int i2 = 0; i2 < gridHeight; i2++) {
                clonedMap[i][i2] = map[i][i2];
            }
        }
        return clonedMap;
    }

    public void updateWorld(final int x, final int y, final NextObject object, final boolean placeOnly) {
        final GridSquare currentTile = world[x][y];
        if (placeOnly) {
            currentTile.setTileType(object.getTileType());
            currentTile.setAge(object.getAge());
            currentTile.getBonusSources().clear();
            applyEntityUpdate(x, y, object, placeOnly);
        } else {
            if (inProgress) return;
            inProgress = true;
            currentTile.registerEntityModifier(new ScaleAtModifier(0.3f, 0f, 1f, GameMatrix.RECT_SIZE / 2, GameMatrix.RECT_SIZE / 2, new IEntityModifier.IEntityModifierListener() {
                @Override
                public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
                    currentTile.setTileType(object.getTileType());
                    currentTile.setAge(object.getAge());
                    currentTile.getBonusSources().clear();
                    drawWorld();
                }

                @Override
                public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                    applyEntityUpdate(x, y, object, placeOnly);
                    inProgress = false;
                }
            }));
        }
    }

    private ArrayList<GridSquare> getAdjacentTiles(int x, int y) {
        ArrayList<GridSquare> results = new ArrayList<GridSquare>();

        if (x > 0 && !world[x - 1][y].isBustedOrEmpty()) {
            results.add(world[x - 1][y]);
        }

        if (y > 0 && !world[x][y - 1].isBustedOrEmpty()) {
            results.add(world[x][y - 1]);
        }

        if (x < gridWidth - 1 && !world[x + 1][y].isBustedOrEmpty()) {
            results.add(world[x + 1][y]);
        }

        if (y < gridHeight - 1 && !world[x][y + 1].isBustedOrEmpty()) {
            results.add(world[x][y + 1]);
        }

        return results;
    }

    private void applyEntityUpdate(int x, int y, NextObject object, boolean placeOnly) {


        if (!placeOnly) {
            if (object.getTileType() == GameMatrix.MULTIPLIERX4) {
                ArrayList<Pair<Integer, Integer>> updateList = new ArrayList<Pair<Integer, Integer>>();
                updateAdjacent(x, y, GameMatrix.BLUE_BLOCK, 1, updateList, getVisitMap());
                updateAdjacent(x, y, GameMatrix.GREEN_BLOCK, 1, updateList, getVisitMap());
                updateAdjacent(x, y, GameMatrix.RED_BLOCK, 1, updateList, getVisitMap());
                world[x][y].setupMultiplier();
                for (Pair p : updateList) {
                    Log.d(TAG, "Updating " + p.first + " " + p.second);
                    GridSquare gridSquare = world[(Integer) p.first][(Integer) p.second];
                    if (gridSquare.isColoredTile()) {
                        gridSquare.addBonus(world[x][y], gridSquare.getPoints() * 4);
                    }
                }
            } else if (world[x][y].isColoredTile()) {
                if (isRGB(x, y, object.getTileType())) {
                    processRGBSequence(x, y, object);
                } else {
                    for (GridSquare square : getAdjacentTiles(x, y)) {
                        if (square.isColoredTile() && square.getTileType() != object.getTileType()) {
                            if (isRGB(square.getBoardPositionX(), square.getBoardPositionY(), square.getTileType())) {
                                NextObject proxyNextObject = new NextObject();
                                proxyNextObject.setTileType(square.getTileType());
                                proxyNextObject.setAge(square.age);
                                processRGBSequence(square.getBoardPositionX(), square.getBoardPositionY(), proxyNextObject);
                                break;
                            }
                        }
                    }
                }


            }
        }

        Log.d(TAG, "score = " + score);
        String highScore = StringUtils.leftPad(Integer.toString(getHighScore()), 4, "0");
        scoreTextString = "Score: " + StringUtils.leftPad(Integer.toString(score), 4, "0") + " High: " + highScore;
        drawWorld();
    }

    private void processRGBSequence(int x, int y, NextObject object) {
        ArrayList<Pair<Integer, Integer>> updateList = new ArrayList<Pair<Integer, Integer>>();
        score += 1;
        updateAdjacent(x, y, object.getTileType(), 0, updateList, getVisitMap());

        ArrayList<GridSquare> secondaryEvents = new ArrayList<GridSquare>();

        for (Pair p : updateList) {
            Log.d(TAG, "Updating " + p.first + " " + p.second);
            GridSquare gridSquare = world[(Integer) p.first][(Integer) p.second];
            if (gridSquare.isColoredTile()) {
                score += gridSquare.getPoints() + gridSquare.getBonus();
                gridSquare.setTileType(object.getTileType());
                gridSquare.incrementAge();
                if (!gridSquare.getBonusSources().isEmpty()) {
                    for (GridSquare bonusSource : gridSquare.getBonusSources()) {
                        bonusSource.setTileType(GameMatrix.BUSTED);
                    }
                    gridSquare.getBonusSources().clear();
                }
            } else if (gridSquare.isRepeater()) {
                secondaryEvents.add(gridSquare);
            }
        }

        if (score > getHighScore()) {
            saveHighScore(score);
        }

        for (GridSquare secondaryTriggers : secondaryEvents) {
            NextObject generatedObject = new NextObject();
            generatedObject.setTileType(secondaryTriggers.getTileType() - 6);
            generatedObject.setAge(0);
            inProgress = false;
            updateWorld(secondaryTriggers.getBoardPositionX(), secondaryTriggers.getBoardPositionY(),
                    generatedObject, false);
        }
    }

    private void updateAdjacent(int x, int y, int squareColor, int level, ArrayList<Pair<Integer, Integer>> updateList, int visitMap[][]) {
        updateTargetSquare(squareColor, level, updateList, visitMap, x - 1, y);
        updateTargetSquare(squareColor, level, updateList, visitMap, x + 1, y);
        updateTargetSquare(squareColor, level, updateList, visitMap, x, y - 1);
        updateTargetSquare(squareColor, level, updateList, visitMap, x, y + 1);
    }

    private void updateTargetSquare(int parentObjectColor, int level, ArrayList<Pair<Integer, Integer>> updateList, int[][] visitMap, int target_x, int target_y) {
        int color = parentObjectColor;

        if (target_x < 0) return;
        if (target_x >= gridWidth) return;
        if (target_y < 0) return;
        if (target_y >= gridHeight) return;

        GridSquare currentCell = world[target_x][target_y];
        if (currentCell.isBustedOrEmpty()) return;

        if (level == 0) {
            color = currentCell.getTileType();

            if (currentCell.isRepeater()) {
                if (currentCell.getTileType() == parentObjectColor + 6) return;
            }

            if (parentObjectColor == color) return;
        } else {

            if (currentCell.isRepeater()) {

                if (currentCell.getTileType() != parentObjectColor + 6) return;

                if (visitMap[target_x][target_y] == 1) return;
                visitMap[target_x][target_y] = 1;

                updateList.add(new Pair(target_x, target_y));
                return;
            }

            if (currentCell.getTileType() != parentObjectColor) return;
        }

        if (visitMap[target_x][target_y] == 1) return;
        visitMap[target_x][target_y] = 1;

        updateList.add(new Pair(target_x, target_y));

        updateAdjacent(target_x, target_y, color, level + 1, updateList, visitMap);
    }

    private boolean isRGB(int x, int y, int color) {
        Log.d(TAG, "isRGB = " + color);
        ArrayList<GridSquare> adjacentSquares = new ArrayList<GridSquare>();
        if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {
            if (x > 0 && world[x - 1][y].isColoredTile()) {
                adjacentSquares.add(world[x - 1][y]);
            }

            if (x < gridWidth - 1 && world[x + 1][y].isColoredTile()) {
                adjacentSquares.add(world[x + 1][y]);
            }

            if (y > 0 && world[x][y - 1].isColoredTile()) {
                adjacentSquares.add(world[x][y - 1]);
            }

            if (y < gridHeight - 1 && world[x][y + 1].isColoredTile()) {
                adjacentSquares.add(world[x][y + 1]);
            }

        }
        int colorTest = 0;
        for (GridSquare adjacentColor : adjacentSquares) {
            Log.d(TAG, "test = " + adjacentColor);
            if (adjacentColor.getTileType() != color) {
                if (colorTest == 0) {
                    colorTest = adjacentColor.getTileType();
                } else {
                    if (colorTest != adjacentColor.getTileType()) return true;
                }
            }
        }
        return false;
    }

    public NextObject getNextObject(boolean noMultiplier) {
        int tileType = random.nextInt(3) + 1;
        NextObject nextObject = new NextObject();
        nextObject.setTileType(tileType);
        int useMaxAge = random.nextInt(4);

        if (useMaxAge == 1) {
            nextObject.setAge(2);
        } else {
            nextObject.setAge(0);
        }

        if (!noMultiplier) {
            int multiplier = random2.nextInt(20);
            if (multiplier == 1) {
                nextObject.setTileType(GameMatrix.MULTIPLIERX4);
                nextObject.setAge(0);
            } else if (multiplier == 2 || multiplier == 3) {
                nextObject.setTileType(tileType + 6);
                nextObject.setAge(0);
            }
        }

        return nextObject;
    }

    public synchronized void drawWorld() {
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                world[x][y].updateSelf();
            }
        }

        //Draw queue
        for (int i = 0; i < blockQueue.size(); i++) {
            GridSquare rectangle = queueRectangles[i];
            rectangle.setTileType(blockQueue.get(i).getTileType());
            rectangle.setAge(blockQueue.get(i).getAge());
            rectangle.setBonus(0);
            rectangle.updateSelf();
        }

        scoreText.setText(scoreTextString);
    }

    private Color toColor(Integer block) {
        if (block == RED_BLOCK) {
            return Color.RED;
        }
        if (block == BLUE_BLOCK) {
            return Color.BLUE;
        }
        if (block == GREEN_BLOCK) {
            return Color.GREEN;
        }
        if (block == BUSTED) {
            return Color.BLACK;
        }
        return null;
    }

    public void onTouch(TouchEvent pSceneTouchEvent) {
        Log.d(TAG, "onTouch " + pSceneTouchEvent.getX() + " " + pSceneTouchEvent.getY());
        if (newGameButton.isAreaTouched(pSceneTouchEvent)) {

            resetWorld();
            populateInitial();

        } else if (offset_x <= pSceneTouchEvent.getX() && offset_y <= pSceneTouchEvent.getY() &&
                offset_x + gridWidth * RECT_SIZE >= pSceneTouchEvent.getX() && offset_y + gridHeight * RECT_SIZE >= pSceneTouchEvent.getY()) {

            float normalized_x = pSceneTouchEvent.getX() - offset_x;
            float normalized_y = pSceneTouchEvent.getY() - offset_y;

            int grid_x = (int) normalized_x / RECT_SIZE;
            int grid_y = (int) normalized_y / RECT_SIZE;

            if (isValid(grid_x, grid_y)) {

                NextObject object = blockQueue.remove(0);
                fillQueue();
                Log.d(TAG, "updating world " + grid_x + " , " + grid_y + " = " + object);
                updateWorld(grid_x, grid_y, object, false);

                if (!hasValidMoves()) {
                    gameOverText.setVisible(true);
                }
            }
        }
    }

    public boolean hasValidMoves() {
        for (int i = 0; i < gridWidth; i++) {
            for (int i2 = 0; i2 < gridHeight; i2++) {
                if (world[i][i2].getTileType() == EMPTY) return true;
            }
        }
        return false;
    }

    public boolean isEmpty(int x, int y) {
        if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {
            return world[x][y].isEmpty();
        }
        return true;
    }

    public boolean isValid(int grid_x, int grid_y) {

        if (!world[grid_x][grid_y].isEmpty()) return false;

        if (grid_x >= 0 && grid_y >= 0 && grid_x < gridWidth && grid_y < gridHeight) {

            if (!isEmpty(grid_x - 1, grid_y)) {
                return true;
            } else if (!isEmpty(grid_x + 1, grid_y)) {
                return true;
            } else if (!isEmpty(grid_x, grid_y - 1)) {
                return true;
            } else if (!isEmpty(grid_x, grid_y + 1)) {
                return true;
            } else if (!isEmpty(grid_x - 1, grid_y - 1)) {
                return true;
            } else if (!isEmpty(grid_x + 1, grid_y + 1)) {
                return true;
            } else if (!isEmpty(grid_x + 1, grid_y - 1)) {
                return true;
            } else if (!isEmpty(grid_x - 1, grid_y + 1)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onUpdate(float pSecondsElapsed) {
        drawWorld();
    }

    @Override
    public void reset() {

    }

    public void prepareResources(BaseGameActivity activity) {


    }

    public int getMaxPositionX() {
        return gridWidth;
    }

    public int getMaxPositionY() {
        return gridHeight;
    }
}
