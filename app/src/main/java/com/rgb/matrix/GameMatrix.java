package com.rgb.matrix;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;

import com.droiuby.tiletron.app.SoundWrapper;
import com.rgb.matrix.interfaces.GridEventListener;
import com.rgb.matrix.menu.MainMenu;

import org.andengine.audio.sound.Sound;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.modifier.ScaleAtModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.modifier.IModifier;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class GameMatrix implements IUpdateHandler {
    private static final String TAG = GameMatrix.class.getName();

    private final int gridWidth;
    private final int gridHeight;
    private final Scene scene;
    private final HashMap<String, SoundWrapper> soundAssets;
    private final MainMenu mainMenu;
    private final GridEventListener listener;
    private int totalMoves = 0;
    private final Random random;
    private final SharedPreferences sharedPrefs;
    private boolean inProgress = false;
    private Random random2;

    public VertexBufferObjectManager getVertexBuffer() {
        return vertexBuffer;
    }

    private final VertexBufferObjectManager vertexBuffer;
    Context context;
    Rectangle rectangles[][];

    RectangleButton newGameButton;
    public static final int RED_BLOCK = 1;
    public static final int BLUE_BLOCK = 2;
    public static final int GREEN_BLOCK = 3;
    public static final int BUSTED = 4;
    public static final int MULTIPLIERX2 = 5;
    public static final int ERASER = 6;
    public static final int RED_REPEATER_BLOCK = 7;
    public static final int BLUE_REPEATER_BLOCK = 8;
    public static final int GREEN_REPEATER_BLOCK = 9;
    public static final int MULTIPLIERX4_COLORED = 10;

    public static final int EMPTY = 0;

    Vector<NextObject> blockQueue = new Vector<NextObject>();
    HashMap<String, Font> fontDictionary = new HashMap<String, Font>();

    static GameMatrix instance;

    MainGrid mainGrid;

    protected GameMatrix(Context context, GridEventListener listener, Scene scene, MainMenu mainMenu, HashMap<String, Font> fontDictionary, HashMap<String, SoundWrapper> soundAssets, VertexBufferObjectManager vertexBuffer, int gridWidth, int gridHeight, int offset_x, int offset_y) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.context = context;
        this.scene = scene;
        this.mainMenu = mainMenu;
        this.soundAssets = soundAssets;
        this.listener = listener;
        sharedPrefs = context.getSharedPreferences("high_score", Context.MODE_PRIVATE);
//
//        Resources r = context.getResources();
//        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MainGrid.TILE_SIZE_IN_DIP, r.getDisplayMetrics());
//        MainGrid.setRectangleTileSizeInPixels(px);

        this.mainGrid = new MainGrid(offset_x, offset_y + 10, gridWidth, gridHeight, this, mainMenu, fontDictionary, soundAssets, vertexBuffer, listener);


        this.fontDictionary = fontDictionary;
        this.vertexBuffer = vertexBuffer;

        random = new Random(System.nanoTime());
        random2 = new Random(System.nanoTime() + 2);
        setupWorld();

    }

    public void fillQueue() {
        int fillSize = MainGrid.QUEUE_SIZE - blockQueue.size();
        for (int i = 0; i < fillSize; i++) {
            blockQueue.add(getNextObject(false));
        }
    }

    public static GameMatrix getInstance(Context context, GridEventListener listener,  Scene scene, MainMenu mainMenu, HashMap<String, Font> fontDictionary, HashMap<String, SoundWrapper> soundAssets, VertexBufferObjectManager vertexBuffer, int width, int height, int offset_x, int offset_y) {
        if (instance == null) {
            instance = new GameMatrix( context, listener, scene, mainMenu, fontDictionary, soundAssets, vertexBuffer, width, height, offset_x, offset_y);
        }
        return instance;
    }

    public void resetWorld() {
        mainGrid.resetWorldState();
        blockQueue.clear();
        fillQueue();
    }

    public int getHighScore() {
        return sharedPrefs.getInt("high_score", 0);
    }

    public void saveHighScore(int highScore) {
        sharedPrefs.edit().putInt("high_score", highScore).commit();
    }

    public void setupWorld() {
        blockQueue.clear();
        fillQueue();
        this.mainGrid.setScore(0);
        populateInitial();
    }

    public void populateInitial() {
        int totalInitial = random.nextInt(3) + 3;
        for (int i2 = 0; i2 < totalInitial; i2++) {
            int x = random.nextInt(gridWidth);
            int y = random.nextInt(gridHeight);
            updateWorld(x, y, getNextObject(true), 1, true);
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

    public void clearBustedSquares() {
        for (int i = 0; i < gridWidth; i++) {
            for (int i2 = 0; i2 < gridHeight; i2++) {
                GridSquare square = mainGrid.getSquareAt(i, i2);
                if (square.getTileType() == BUSTED) {
                    square.reset();
                    square.animateEmpty();
                }
            }
        }
    }

    class ScoreIncrementer extends TimerHandler {

        int prevScore = 0;
        int targetScore = 0;
        int highScore = 0;
        Scene scene;

        public ScoreIncrementer(Scene scene, float pTimerSeconds, int prevScore, int targetScore, int highScore, ITimerCallback pTimerCallback) {
            super(pTimerSeconds, pTimerCallback);
            this.prevScore = prevScore;
            this.highScore = highScore;
            this.targetScore = targetScore;
            this.scene = scene;
        }

        public void onTimePassed(final TimerHandler pTimer) {
            //Check if it reached the score you need to display
            if (prevScore < targetScore) {
                //If not, increase the variable...
                prevScore++;

                //...set the new text with the new value...
                mainGrid.getScoreText().setText(mainGrid.formatScoreString(prevScore, StringUtils.leftPad(Integer.toString(highScore), 4, "0")));

                //...and reset the timer
                pTimer.reset();
            } else
                //If it reached the score, unregister the timer
                scene.unregisterUpdateHandler(pTimer);
        }
    }

    public void updateWorld(final int x, final int y, final NextObject object, int multiplierLevel, final boolean placeOnly) {
        if (multiplierLevel > 5) {
            multiplierLevel = 5;
        }

        final GridSquare currentTile = mainGrid.getSquareAt(x, y);
        if (placeOnly) {
            currentTile.setTileType(object.getTileType());
            currentTile.setMultiplierColor(object.getMultiplierColor());
            currentTile.setAge(object.getAge());
            currentTile.getBonusSources().clear();
            applyEntityUpdate(x, y, object, multiplierLevel, placeOnly);
        } else {
            if (inProgress) return;
            inProgress = true;
            final int finalMultiplierLevel = multiplierLevel;
            currentTile.registerEntityModifier(new ScaleAtModifier(0.3f, 0f, 1f, MainGrid.getRectangleTileSizeInPixels() / 2, MainGrid.getRectangleTileSizeInPixels() / 2, new IEntityModifier.IEntityModifierListener() {
                @Override
                public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {

                    currentTile.setTileType(object.getTileType());
                    currentTile.setAge(object.getAge());
                    currentTile.getBonusSources().clear();
                    currentTile.setMultiplierColor(object.getMultiplierColor());
                    playSound("place_tile");
                    drawWorld();
                }

                @Override
                public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                    applyEntityUpdate(x, y, object, finalMultiplierLevel, placeOnly);
                }
            }));
        }
    }

    private ArrayList<GridSquare> getAdjacentTiles(int x, int y) {
        ArrayList<GridSquare> results = new ArrayList<GridSquare>();

        if (x > 0 && !mainGrid.getSquareAt(x - 1, y).isBustedOrEmpty()) {
            results.add(mainGrid.getSquareAt(x - 1, y));
        }

        if (y > 0 && !mainGrid.getSquareAt(x, y - 1).isBustedOrEmpty()) {
            results.add(mainGrid.getSquareAt(x, y - 1));
        }

        if (x < gridWidth - 1 && !mainGrid.getSquareAt(x + 1, y).isBustedOrEmpty()) {
            results.add(mainGrid.getSquareAt(x + 1, y));
        }

        if (y < gridHeight - 1 && !mainGrid.getSquareAt(x, y + 1).isBustedOrEmpty()) {
            results.add(mainGrid.getSquareAt(x, y + 1));
        }

        return results;
    }

    private void applyEntityUpdate(int x, int y, NextObject object, int multiplierLevel, boolean placeOnly) {
        int prevScore = mainGrid.getScore();
        mainGrid.rechargeMeterMark();
        boolean checkValidMoves = true;
        if (!placeOnly) {
            GridSquare currentTile = mainGrid.getSquareAt(x, y);

            if ((object.getTileType() == GameMatrix.MULTIPLIERX2) || (object.getTileType() == GameMatrix.MULTIPLIERX4_COLORED)) {

                ArrayList<Pair<Integer, Integer>> updateList = new ArrayList<Pair<Integer, Integer>>();
                if (object.getTileType() == GameMatrix.MULTIPLIERX4_COLORED) {
                    updateAdjacent(x, y, object.getMultiplierColor(), 1, updateList, getVisitMap());
                } else {
                    updateAdjacent(x, y, GameMatrix.BLUE_BLOCK, 1, updateList, getVisitMap());
                    updateAdjacent(x, y, GameMatrix.GREEN_BLOCK, 1, updateList, getVisitMap());
                    updateAdjacent(x, y, GameMatrix.RED_BLOCK, 1, updateList, getVisitMap());
                }

                currentTile.setupMultiplier();

                for (Pair p : updateList) {
                    Log.d(TAG, "Updating " + p.first + " " + p.second);
                    GridSquare gridSquare = mainGrid.getSquareAt((Integer) p.first, (Integer) p.second);
                    if (gridSquare.isColoredTile()) {
                        int multiplierFactor = 4;

                        if (object.getTileType() == GameMatrix.MULTIPLIERX2) {
                            multiplierFactor = 2;
                        }
                        int previous = gridSquare.getTotalPoints();
                        int current = gridSquare.addBonus(currentTile, gridSquare.getPoints() * multiplierFactor);
                        gridSquare.animateScore(previous, current, multiplierLevel);
                    }
                }
                inProgress = false;
            } else if (currentTile.isColoredTile()) {
                if (mainGrid.isRGB(x, y, object.getTileType())) {
                    checkValidMoves = processRGBSequence(x, y, multiplierLevel, object);
                } else {
                    for (GridSquare square : getAdjacentTiles(x, y)) {
                        if (square.isColoredTile() && square.getTileType() != object.getTileType()) {
                            if (mainGrid.isRGB(square.getBoardPositionX(), square.getBoardPositionY(), square.getTileType())) {
                                NextObject proxyNextObject = new NextObject();
                                proxyNextObject.setTileType(currentTile.getTileType());
                                proxyNextObject.setMultiplierColor(currentTile.getMultiplierColor());
                                proxyNextObject.setAge(currentTile.age);
                                square.setTileType(currentTile.getTileType());
                                if (!processRGBSequence(square.getBoardPositionX(), square.getBoardPositionY(), multiplierLevel, proxyNextObject)) {
                                    checkValidMoves = false;
                                };
                                break;
                            }
                        }
                    }
                }

                inProgress = false;
            } else {
                inProgress = false;
            }
        }

        Log.d(TAG, "score = " + mainGrid.getScore());
        final String highScore = StringUtils.leftPad(Integer.toString(getHighScore()), 4, "0");

        if (prevScore != mainGrid.getScore()) {
            ScoreIncrementer scoreAnimator = new ScoreIncrementer(scene, 0.03f, prevScore, mainGrid.getScore(), getHighScore(), new ITimerCallback() {

                @Override
                public void onTimePassed(TimerHandler pTimerHandler) {
                    mainGrid.updateScoreText(highScore);
                    mainGrid.animateRechargeMeter();
                }
            });
            scene.registerUpdateHandler(scoreAnimator);
        }

        drawWorld();

        if (checkValidMoves && !hasValidMoves()) {
            if (mainGrid.hasRechargeMeter()) {
                mainGrid.useRechargeMeter();
                if (!hasValidMoves()) {
                    mainGrid.showGameOver();
                }
            } else {
                mainGrid.showGameOver();
            }
        }
    }

    private boolean processRGBSequence(int x, int y, final int multiplierLevel, NextObject object) {
        ArrayList<Pair<Integer, Integer>> updateList = new ArrayList<Pair<Integer, Integer>>();
        mainGrid.addScore(1);
        updateAdjacent(x, y, object.getTileType(), 0, updateList, getVisitMap());

        final ArrayList<GridSquare> secondaryEvents = new ArrayList<GridSquare>();

        if (updateList.size() > 0 && multiplierLevel > 1) {
            mainGrid.showChainBonus(multiplierLevel);
        }

        for (Pair p : updateList) {
            Log.d(TAG, "Updating " + p.first + " " + p.second);
            GridSquare gridSquare = mainGrid.getSquareAt((Integer) p.first, (Integer) p.second);
            if (gridSquare.isColoredTile()) {
                mainGrid.addScore( (gridSquare.getPoints() + gridSquare.getBonus()) * multiplierLevel);
                gridSquare.setTileType(object.getTileType());

                int previous = gridSquare.getTotalPoints();
                gridSquare.incrementAge();
                if (gridSquare.getTileType() != GameMatrix.BUSTED) {
                    gridSquare.animateColorFlip(object.getTileType());
                }

                gridSquare.animateScore(previous, gridSquare.getTotalPoints(), multiplierLevel);
                playSound("cascade");

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

        if (mainGrid.getScore() > getHighScore()) {
            saveHighScore(mainGrid.getScore());
        }

        if (secondaryEvents.size() > 0) {
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {

                    for (GridSquare secondaryTriggers : secondaryEvents) {
                        NextObject generatedObject = new NextObject();
                        generatedObject.setTileType(secondaryTriggers.getTileType() - 6);
                        generatedObject.setAge(0);
                        inProgress = false;
                        updateWorld(secondaryTriggers.getBoardPositionX(), secondaryTriggers.getBoardPositionY(),
                                generatedObject, multiplierLevel + 1, false);
                    }
                }
            }, 1000);
            return false;
        }

        return true;
    }

    private void playSound(String sound) {
        soundAssets.get(sound).play();
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

        GridSquare currentCell = mainGrid.getSquareAt(target_x, target_y);
        if (currentCell.isBustedOrEmpty()) return;

        if (level == 0) {

            if (currentCell.getTileType() == MULTIPLIERX4_COLORED) {
                color = currentCell.getMultiplierColor();
            } else {
                color = currentCell.getTileType();
            }

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
            if ( (currentCell.getTileType() == MULTIPLIERX4_COLORED) && (parentObjectColor != currentCell.getMultiplierColor())) {
                return;
            } else if (currentCell.getTileType() != parentObjectColor) return;
        }

        if (visitMap[target_x][target_y] == 1) return;
        visitMap[target_x][target_y] = 1;

        updateList.add(new Pair(target_x, target_y));

        updateAdjacent(target_x, target_y, color, level + 1, updateList, visitMap);
    }


    public NextObject getNextObject(boolean noMultiplier) {
        int tileType = random.nextInt(3) + 1;
        NextObject nextObject = new NextObject();
        nextObject.setTileType(tileType);
        int useMaxAge = random.nextInt(6);

        if (useMaxAge == 1) {
            nextObject.setAge(2);
        } else {
            nextObject.setAge(0);
        }

        if (!noMultiplier) {
            int multiplier = random2.nextInt(20);
            if (multiplier == 1) {
                nextObject.setTileType(GameMatrix.MULTIPLIERX2);
                nextObject.setAge(0);
            } else if (multiplier == 4) {
                nextObject.setTileType(GameMatrix.MULTIPLIERX4_COLORED);
                nextObject.setMultiplierColor(tileType);
                nextObject.setAge(0);
            } else if (multiplier == 2 || multiplier == 3) {
                nextObject.setTileType(tileType + 6);
                nextObject.setAge(0);
            }
        }

        return nextObject;
    }

    public synchronized void drawWorld() {
        mainGrid.updateSelf();

        //Draw queue
        for (int i = 0; i < MainGrid.QUEUE_SIZE; i++) {
            GridSquare rectangle = mainGrid.getQueueRect(i);
            NextObject currentQueue = blockQueue.get(i);

            rectangle.setTileType(currentQueue.getTileType());
            rectangle.setMultiplierColor(currentQueue.getMultiplierColor());
            rectangle.setAge(currentQueue.getAge());
            rectangle.setBonus(0);
            rectangle.updateSelf();
        }
    }


    public void onTouch(TouchEvent pSceneTouchEvent) {
        mainGrid.onTouch(pSceneTouchEvent);
    }

    public boolean hasValidMoves() {
        for (int i = 0; i < gridWidth; i++) {
            for (int i2 = 0; i2 < gridHeight; i2++) {
                if (mainGrid.getSquareAt(i, i2).getTileType() == EMPTY) return true;
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

    public MainGrid getMainGrid() {
        return this.mainGrid;
    }
}
