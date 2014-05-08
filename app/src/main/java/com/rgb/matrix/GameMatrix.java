package com.rgb.matrix;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;

import com.dayosoft.tiletron.app.SoundWrapper;
import com.rgb.matrix.interfaces.GridEventCallback;
import com.rgb.matrix.interfaces.GridEventListener;
import com.rgb.matrix.interfaces.OnSequenceFinished;
import com.rgb.matrix.menu.MainMenu;

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
import java.util.HashSet;
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
    private final MatrixOptions options;
    private int totalMoves = 0;
    private final Random random;
    private final SharedPreferences sharedPrefs;
    private boolean disableMoves = false;
    HashMap<String, ArrayList<GridEventCallback>> gridEventCallbacks = new HashMap<String, ArrayList<GridEventCallback>>();

    HashSet<String> triggerCollection = new HashSet<String>();

    public synchronized boolean setAndGetinProgress() {
        if (inProgress > 0) return false;
        triggerCollection.clear();
        inProgress++;
        return true;
    }

    public synchronized void decrementInProgress() {
        if (inProgress == 0) return;
        inProgress--;
        if (inProgress == 0) {
            if (!triggerCollection.isEmpty()) {
                for(String events : triggerCollection) {
                    triggerCallback(events);
                }
            }
            if (!triggerCollection.contains("trigger_rgb")) {
                triggerCallback("no_rgb");
            }
            if (blockQueue.size() == 0) {
                triggerCallback("queue_empty");
            }
        }
    }

    public synchronized void triggerEvent(String event) {
        triggerCollection.add(event);
    }

    public boolean isInProgress() {
        return inProgress > 0;
    }


    private int inProgress = 0;
    private Random random2;

    public VertexBufferObjectManager getVertexBuffer() {
        return vertexBuffer;
    }

    private final VertexBufferObjectManager vertexBuffer;
    Context context;
    Rectangle rectangles[][];

    RectangleButton newGameButton;

    public Vector<NextObject> getBlockQueue() {
        return blockQueue;
    }

    public void setBlockQueue(Vector<NextObject> blockQueue) {
        this.blockQueue = blockQueue;
    }

    Vector<NextObject> blockQueue = new Vector<NextObject>();
    HashMap<String, Font> fontDictionary = new HashMap<String, Font>();

    MainGrid mainGrid;

    public GameMatrix(Context context, GridEventListener listener, Scene scene, MainMenu mainMenu,
                      HashMap<String, Font> fontDictionary, HashMap<String, SoundWrapper> soundAssets,
                      VertexBufferObjectManager vertexBuffer, int gridWidth, int gridHeight,
                      int offset_x, int offset_y, float sceneWidth, float sceneHeight, float tileSize, MatrixOptions options) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.context = context;
        this.scene = scene;
        this.mainMenu = mainMenu;
        this.soundAssets = soundAssets;
        this.listener = listener;
        this.options = options;
        sharedPrefs = context.getSharedPreferences("high_score", Context.MODE_PRIVATE);
        this.mainGrid = new MainGrid(offset_x, offset_y + ObjectDimensions.szMainGridPaddingTop,
                sceneWidth, sceneHeight,
                gridWidth, gridHeight, tileSize, this, mainMenu, fontDictionary, soundAssets, vertexBuffer, listener,
                options);


        this.fontDictionary = fontDictionary;
        this.vertexBuffer = vertexBuffer;

        random = new Random(System.nanoTime());
        random2 = new Random(System.nanoTime() + 2);
        setupWorld();
    }

    public void fillQueue() {
        if (options.isShouldUseRandomQueue()) {
            int fillSize = MainGrid.QUEUE_SIZE - blockQueue.size();
            for (int i = 0; i < fillSize; i++) {
                blockQueue.add(getNextObject(false));
            }
        }
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
        if (options.isShouldUseRandomQueue()) {
            fillQueue();
        } else {
            listener.populateQueue(blockQueue);
        }
        this.mainGrid.setScore(0);
        if (options.isShouldPrepopulate()) {
            populateInitial();
        }
        listener.onSetupWorld(mainGrid);
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
                if (square.getTileType() == GridSquare.BUSTED) {
                    square.reset();
                    square.animateEmpty();
                }
            }
        }
    }

    public void disableMoves() {
        this.disableMoves = true;
    }

    public void enableMoves() {
        this.disableMoves = false;
    }

    public void registerEventCallback(String key, GridEventCallback gridEventCallback) {
        if (!gridEventCallbacks.containsKey(key)) {
            gridEventCallbacks.put(key,new ArrayList<GridEventCallback>());
        }

        ArrayList<GridEventCallback>callbacks = gridEventCallbacks.get(key);
        callbacks.add(gridEventCallback);
    }

    public void triggerCallback(String key) {
        if (gridEventCallbacks.containsKey(key)) {
            ArrayList<GridEventCallback>callbacks = gridEventCallbacks.get(key);
            for(GridEventCallback c : callbacks) {
                c.onEventTriggered(key);
            }
        }
    }

    public boolean testColor(int testColor) {
        for(int i = 0 ; i < gridWidth; i++) {
            for (int i2 = 0; i2 < gridHeight; i2++) {
                GridSquare square = mainGrid.getSquareAt(i,i2);
                if (square.getTileType() == GridSquare.EMPTY ||
                        square.getTileType() == GridSquare.BUSTED ||
                        square.getTileType() == testColor) {
                    continue;
                } else {
                    return false;
                }
            }
        }
        return true;
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

    public void restoreWorldState(SavedGridBundle bundle) {
        for (int i = 0; i < gridWidth; i++) {
            for (int i2 = 0; i2 < gridHeight; i2++) {
                GridSquare current = mainGrid.getWorld()[i][i2];
                GridSquare saved = bundle.getWorld()[i][i2];
                current.setTileType(saved.tileType);
                current.setMultiplierColor(saved.getMultiplierColor());
                current.setAge(saved.age);
                current.setBonus(saved.getBonus());
            }
        }
        blockQueue.clear();
        for(NextObject nextObject : bundle.getQueue()) {
            blockQueue.add(nextObject.clone());
        }
        drawWorld();
    }

    public SavedGridBundle saveWorldState() {
        SavedGridBundle savedState = new SavedGridBundle();
        GridSquare world[][] = new GridSquare[gridWidth][gridHeight];
        for (int i = 0; i < gridWidth; i++) {
            for (int i2 = 0; i2 < gridHeight; i2++) {
                GridSquare clone = mainGrid.getSquareAt(i,i2).clone();
                world[i][i2] = clone;
            }
        }
        Vector<NextObject> newQueue = new Vector<NextObject>();
        for(NextObject next: getBlockQueue()) {
            newQueue.add(next.clone());
        }
        savedState.setWorld(world);
        savedState.setQueue(newQueue);
        return savedState;
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
            final int finalMultiplierLevel = multiplierLevel;
            inProgress++;
            currentTile.registerEntityModifier(new ScaleAtModifier(0.3f, 0f, 1f, mainGrid.getRectangleTileSizeInPixels() / 2, mainGrid.getRectangleTileSizeInPixels() / 2, new IEntityModifier.IEntityModifierListener() {
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
                    decrementInProgress();
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

            if ((object.getTileType() == GridSquare.MULTIPLIERX2) || (object.getTileType() == GridSquare.MULTIPLIERX4_COLORED)) {

                ArrayList<Pair<Integer, Integer>> updateList = new ArrayList<Pair<Integer, Integer>>();
                if (object.getTileType() == GridSquare.MULTIPLIERX4_COLORED) {
                    updateAdjacent(x, y, object.getMultiplierColor(), 1, updateList, getVisitMap());
                } else {
                    updateAdjacent(x, y, GridSquare.BLUE_BLOCK, 1, updateList, getVisitMap());
                    updateAdjacent(x, y, GridSquare.GREEN_BLOCK, 1, updateList, getVisitMap());
                    updateAdjacent(x, y, GridSquare.RED_BLOCK, 1, updateList, getVisitMap());
                }

                currentTile.setupMultiplier();

                for (Pair p : updateList) {
                    Log.d(TAG, "Updating " + p.first + " " + p.second);
                    GridSquare gridSquare = mainGrid.getSquareAt((Integer) p.first, (Integer) p.second);
                    if (gridSquare.isColoredTile()) {
                        int multiplierFactor = 4;

                        if (object.getTileType() == GridSquare.MULTIPLIERX2) {
                            multiplierFactor = 2;
                        }
                        int previous = gridSquare.getTotalPoints();
                        int current = gridSquare.addBonus(currentTile, gridSquare.getPoints() * multiplierFactor);
                        gridSquare.animateScore(previous, current, multiplierLevel);
                    }
                }
            } else if (currentTile.isColoredTile()) {
                if (mainGrid.isRGB(x, y, object.getTileType())) {
                    checkValidMoves = processRGBSequence(x, y, multiplierLevel, object);
                    triggerEvent("trigger_rgb");
                } else {
                    for (GridSquare square : getAdjacentTiles(x, y)) {
                        if (square.isColoredTile() && square.getTileType() != object.getTileType()) {
                            if (mainGrid.isRGB(square.getBoardPositionX(), square.getBoardPositionY(), square.getTileType())) {
                                triggerEvent("trigger_rgb");
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
                mainGrid.addScore( (1 + gridSquare.getPoints() + gridSquare.getBonus()) * multiplierLevel);
                gridSquare.setTileType(object.getTileType());

                int previous = gridSquare.getTotalPoints();
                gridSquare.incrementAge();
                if (gridSquare.getTileType() != GridSquare.BUSTED) {
                    gridSquare.animateColorFlip(object.getTileType());
                }

                gridSquare.animateScore(previous, gridSquare.getTotalPoints(), multiplierLevel);
                playSound("cascade");

                if (!gridSquare.getBonusSources().isEmpty()) {
                    for (GridSquare bonusSource : gridSquare.getBonusSources()) {
                        bonusSource.setTileType(GridSquare.BUSTED);
                    }
                    gridSquare.getBonusSources().clear();
                }
            } else if (gridSquare.isRepeater()) {
                gridSquare.animateRepeaterExpand(new OnSequenceFinished() {
                    @Override
                    public void completed() {

                    }
                });
                secondaryEvents.add(gridSquare);
            }
        }

        if (mainGrid.getScore() > getHighScore()) {
            saveHighScore(mainGrid.getScore());
        }

        if (secondaryEvents.size() > 0) {
            inProgress++;
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    for (GridSquare secondaryTriggers : secondaryEvents) {
                        NextObject generatedObject = new NextObject();
                        generatedObject.setTileType(secondaryTriggers.getTileType() - 6);
                        generatedObject.setAge(0);
                        updateWorld(secondaryTriggers.getBoardPositionX(), secondaryTriggers.getBoardPositionY(),
                                generatedObject, multiplierLevel + 1, false);
                    }
                    decrementInProgress();
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

            if (currentCell.getTileType() == GridSquare.MULTIPLIERX4_COLORED) {
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
            if ( (currentCell.getTileType() == GridSquare.MULTIPLIERX4_COLORED) && (parentObjectColor != currentCell.getMultiplierColor())) {
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
                nextObject.setTileType(GridSquare.MULTIPLIERX2);
                nextObject.setAge(0);
            } else if (multiplier == 4) {
                nextObject.setTileType(GridSquare.MULTIPLIERX4_COLORED);
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
        for (int i = 0; i < MainGrid.QUEUE_SIZE ; i++) {
            GridSquare rectangle = mainGrid.getQueueRect(i);
            if (i < blockQueue.size()) {
                NextObject currentQueue = blockQueue.get(i);
                rectangle.setTileType(currentQueue.getTileType());
                rectangle.setMultiplierColor(currentQueue.getMultiplierColor());
                rectangle.setAge(currentQueue.getAge());
                rectangle.setBonus(0);
            } else {
                rectangle.reset();
            }
            rectangle.updateSelf();
        }
    }


    public boolean onTouch(TouchEvent pSceneTouchEvent) {
        return mainGrid.onTouch(pSceneTouchEvent, disableMoves);
    }

    public boolean hasValidMoves() {
        for (int i = 0; i < gridWidth; i++) {
            for (int i2 = 0; i2 < gridHeight; i2++) {
                if (mainGrid.getSquareAt(i, i2).getTileType() == GridSquare.EMPTY) return true;
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
