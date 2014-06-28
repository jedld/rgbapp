package com.rgb.matrix.storymode;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.dayosoft.tiletron.app.MainActivity;
import com.dayosoft.tiletron.app.SoundWrapper;
import com.rgb.matrix.ColorConstants;
import com.rgb.matrix.EmptyBoundedEntity;
import com.rgb.matrix.GameManager;
import com.rgb.matrix.GameMatrix;
import com.rgb.matrix.GameOver;
import com.rgb.matrix.GridSquare;
import com.rgb.matrix.GroupedEntity;
import com.rgb.matrix.MainGrid;
import com.rgb.matrix.MatrixOptions;
import com.rgb.matrix.NextObject;
import com.rgb.matrix.ObjectDimensions;
import com.rgb.matrix.RectangleButton;
import com.rgb.matrix.Utils;
import com.rgb.matrix.interfaces.BoundedEntity;
import com.rgb.matrix.interfaces.GridEventCallback;
import com.rgb.matrix.interfaces.GridEventListener;
import com.rgb.matrix.interfaces.OnLevelMenuShownListener;
import com.rgb.matrix.interfaces.OnTextDisplayedListener;
import com.rgb.matrix.menu.MainMenu;
import com.rgb.matrix.menu.MenuItem;
import com.rgb.matrix.menu.OnBackListener;
import com.rgb.matrix.menu.OnMenuSelectedListener;

import org.andengine.audio.sound.Sound;
import org.andengine.entity.Entity;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.modifier.EntityModifier;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.modifier.SequenceEntityModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.ScreenCapture;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.color.Color;
import org.andengine.util.modifier.IModifier;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * Created by joseph on 5/5/14.
 */

class CurrentBlock {

    int opIndex = 0;
    private boolean aTransient = false;

    public CurrentBlock(CurrentBlock parent) {
        this.parent = parent;
        this.aTransient = false;
        opIndex = 0;
    }

    public CurrentBlock getParent() {
        return parent;
    }

    public void setParent(CurrentBlock parent) {
        this.parent = parent;
    }

    CurrentBlock parent;
    ArrayList<Operation> operations;

    public ArrayList<Operation> getOperations() {
        return operations;
    }

    public void setOperations(ArrayList<Operation> operations) {
        this.operations = operations;
    }

    Operation getCurrentOperation() {
        if (opIndex >= operations.size()) {
            return null;
        }
        return operations.get(opIndex);
    }

    public void increment() {
        opIndex++;
    }

    public void setTransient(boolean aTransient) {
        this.aTransient = aTransient;
    }

    public boolean isTransient() {
        return aTransient;
    }


    public CurrentBlock clone() {
        CurrentBlock block = new CurrentBlock(parent);
        block.setOperations(operations);
        block.setTransient(isTransient());
        block.opIndex = opIndex;
        return block;
    }
}

public class StoryMode extends GameManager implements GridEventListener{

    public static final String FIRST_LEVEL = "level_1";
    private static final String TAG = StoryMode.class.getName();
    private static final float TOUCH_INDICATOR_HEIGHT = 15;
    private static final float TOUCH_INDICATOR_SQUARE = 5;
    private static final float TOUCH_INDICATOR_SQUARE_MARGIN = 10;
    private static final float MARGIN_WAIT_FOR_TOUCH = 10;
    private static final float WAIT_FOR_TOUCH_WIDTH = 54;
    private static final float WAIT_FOR_TOUCH_HEIGHT = 54;

    private final MainMenu mainMenu;
    private final HashMap<String, Font> fontDictionary;
    private final HashMap<String, SoundWrapper> soundAsssets;
    private final Scene mScene;
    private final VertexBufferObjectManager vertexBufferObjectManager;
    private final int offset_x;
    private final float canvasWidth;
    private final float canvasHeight;
    private final LevelMenu levelMenu;
    Level currentLevel;
    private GameMatrix matrix;
    static StoryMode instance;

    private LinkedList<CurrentBlock> stack = new LinkedList<CurrentBlock>();
    private int opIndex = 0;
    private boolean waitForTouchOperation = false;
    HashMap<Integer, LevelInfo> levelHashMap = new HashMap<Integer, LevelInfo>();
    private ArrayList<RectangleButton> conversationText = new ArrayList<RectangleButton>();
    private EmptyBoundedEntity emptyBoundedEntity;
    private boolean waitForValidMove = false;
    private HashMap<String, SavedGameState> savedStates = new HashMap<String, SavedGameState>();
    private GroupedEntity waitFoTapContainer;
    private Sprite fastForwardSprite;
    private GroupedEntity yourMoveContainer;

    public StoryMode(final MainActivity context, Scene mScene, float canvasWidth, float canvasHeight,
                     VertexBufferObjectManager vertexBufferObjectManager,
                     HashMap<String, Font> fontDictionary, HashMap<String, SoundWrapper> soundAssets) {
        super(context);
        this.mainMenu = new MainMenu(0, 0, fontDictionary, vertexBufferObjectManager);
        mainMenu.setVisible(false);
        this.fontDictionary = fontDictionary;
        this.soundAsssets = soundAssets;
        this.mScene = mScene;
        this.offset_x = 0;
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.vertexBufferObjectManager = vertexBufferObjectManager;
        this.instance = this;
        this.levelMenu = new LevelMenu(context, 0, 0, canvasWidth, canvasHeight, 3, 5, fontDictionary, loadLevels(0), vertexBufferObjectManager);
        levelMenu.setVisible(false);

        mainMenu.setOnBackListener(new OnBackListener() {
            @Override
            public void onBackPressed(MainMenu mainMenu) {
                mainMenu.animateHide();
            }
        });

        mainMenu.addMenuItem("Restart Level", new OnMenuSelectedListener() {
            @Override
            public void onMenuItemSelected(MenuItem item) {
                mainMenu.setVisible(false);
                onRestart(item);
            }
        });

        mainMenu.addMenuItem("Choose Level", new OnMenuSelectedListener() {
            @Override
            public void onMenuItemSelected(MenuItem item) {
                mainMenu.setVisible(false);
                showLevelChooser(context, null);
            }
        });

        mainMenu.addMenuItem("Back to Title", new OnMenuSelectedListener() {
            @Override
            public void onMenuItemSelected(MenuItem item) {
                mainMenu.setVisible(false);
                stopMusic();
                context.popCurrentManager();
            }
        });

        boolean defaultMusicState = true, defaultSoundState = true;

        defaultMusicState = getMusicState();
        defaultSoundState = getSoundState();

        mainMenu.addMenuItem("Music", true, defaultMusicState, new OnMenuSelectedListener() {

            @Override
            public void onMenuItemSelected(MenuItem item) {
                item.setState(!item.getState());
                toggleMusic(item.getState());
            }
        });

        mainMenu.addMenuItem("Sounds", true, defaultSoundState, new OnMenuSelectedListener() {

            @Override
            public void onMenuItemSelected(MenuItem item) {
                item.setState(!item.getState());
                toggleSounds(item.getState());
            }
        });

        setupTouchIndicator();
        setupYourMoveIndicator();
    }

    public static StoryMode getInstance() {
        return instance;
    }

    public void renderLevel(final Level level, final BaseGameActivity context) {
        this.currentLevel = level;
        levelMenu.setVisible(false);
        mScene.detachChildren();
        savedStates.clear();
        final Text levelText = new Text(0, canvasHeight / 2, fontDictionary.get("title"), level.getName(), vertexBufferObjectManager);
        final Text levelSubText = new Text(0, canvasHeight / 2 + levelText.getWidth() + 10, fontDictionary.get("story_text"), level.getSubName(), vertexBufferObjectManager);
        levelText.setColor(Color.BLACK);
        levelSubText.setColor(Color.BLACK);
        levelText.setX(canvasWidth / 2 - levelText.getWidth() / 2);
        levelSubText.setX(canvasWidth / 2 - levelSubText.getWidth() / 2);

        GroupedEntity textContainer = new GroupedEntity(0f,0f);
        textContainer.attachChild(levelText);
        textContainer.attachChild(levelSubText);

        mScene.attachChild(textContainer);

        levelText.setAlpha(0f);


        IEntityModifier modifier =  new SequenceEntityModifier(new AlphaModifier(1f, 0f, 1f), new AlphaModifier(1f, 1f, 0f, new IEntityModifier.IEntityModifierListener() {
            @Override
            public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {

            }

            @Override
            public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                startLevel(level, context);
            }
        }));

        textContainer.registerEntityModifier(modifier);
    }

    public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
        if (pSceneTouchEvent.isActionDown()) {
            if (waitFoTapContainer.isVisible() && Utils.withinTouchBounds(fastForwardSprite, pSceneTouchEvent)) {
                Log.d(TAG,"fast forwarded...");
                waitForTouchOperation = false;
                waitFoTapContainer.hide();
                processOpSequence(true);
            } else if (levelMenu.isVisible()) {
                levelMenu.onTouch(pSceneTouchEvent);
            } else {
                if (waitForTouchOperation) {
                    waitForTouchOperation = false;
                    waitFoTapContainer.hide();
                    processOpSequence();
                } else {
                    if (matrix!=null && matrix.onTouch(pSceneTouchEvent)) {
                        if (waitForValidMove) {
                            waitForValidMove = false;
                            processOpSequence();
                        }
                    }
                }
            }
        }
        return false;
    }

    public void testLevel(String levelStr) {
        try {
            Level level = parseLevel(levelStr);
            renderLevel(level, context);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void startLevel(Level level, final BaseGameActivity context) {
        try {
            levelMenu.setVisible(false);
            MatrixOptions options = new MatrixOptions();
            options.setShouldPrepopulate(false);

            if (!level.isRechargeMeter()) {
                options.setShouldShowRechargeMeter(false);
            }

            if (!level.isUseQueue()) {
                options.setShouldUseRandomQueue(false);
            }
            if (!level.isScoreVisible()) {
                options.setScoreVisible(false);
            }


            options.setAllowedObjects(level.getAllowedQueueObjects());
            options.setRechargeMeterInitialValue(level.getRechargeMeterInitial());
            matrix = new GameMatrix(context, this, mScene, mainMenu, fontDictionary, soundAsssets,
                    vertexBufferObjectManager, level.getGridWidth(), level.getGridHeight(), offset_x, 10,
                    canvasWidth, canvasHeight, ObjectDimensions.STORY_MODE_TILE_SIZE, options);


            MainGrid grid = matrix.getMainGrid();

            matrix.drawWorld();

            emptyBoundedEntity = new EmptyBoundedEntity(0, 0, canvasWidth, canvasHeight);
            emptyBoundedEntity.attachChild(grid);
            mScene.attachChild(emptyBoundedEntity);


            setupTouchIndicator();
            setupYourMoveIndicator();

            //Reattach menu
            mainMenu.detachSelf();
            mainMenu.setVisible(false);
            mScene.attachChild(mainMenu);
            //start music
            startMusic();

            CurrentBlock block = new CurrentBlock(null);
            block.setOperations(level.getOperations());
            stack.add(block);
            processOpSequence();

        } catch (final Exception e) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context,"Error while loading level " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            e.printStackTrace();
        }
    }


    private void setupYourMoveIndicator() {
        yourMoveContainer = new GroupedEntity(0, canvasHeight - WAIT_FOR_TOUCH_HEIGHT - MARGIN_WAIT_FOR_TOUCH);
        Rectangle backgroundRectangle = new Rectangle(0, 0, canvasWidth, WAIT_FOR_TOUCH_HEIGHT, vertexBufferObjectManager);
        backgroundRectangle.setColor(Color.BLACK);
        backgroundRectangle.setAlpha(0.3f);

        Text waitForTapText = new Text(0,0, fontDictionary.get("touch_indicator"),"Your move.", vertexBufferObjectManager);
        waitForTapText.setX(backgroundRectangle.getWidth()/2 - waitForTapText.getWidth()/2);
        waitForTapText.setY(backgroundRectangle.getHeight()/2 - waitForTapText.getHeight()/2);
        waitForTapText.setColor(Color.WHITE);
        backgroundRectangle.attachChild(waitForTapText);
        yourMoveContainer.attachChild(backgroundRectangle);
        yourMoveContainer.setVisible(false);
        yourMoveContainer.mark();
        mScene.attachChild(yourMoveContainer);
    }

    private void setupTouchIndicator() {

        Sprite waitForTouchSprite = Utils.getInstance().getSprite("single_tap");
        fastForwardSprite = Utils.getInstance().getSprite("ic_action_fast_forward");

        waitFoTapContainer = new GroupedEntity(0, canvasHeight - WAIT_FOR_TOUCH_HEIGHT - MARGIN_WAIT_FOR_TOUCH);

        float touchX = canvasWidth - WAIT_FOR_TOUCH_WIDTH - MARGIN_WAIT_FOR_TOUCH;
        float touchY = canvasHeight - WAIT_FOR_TOUCH_HEIGHT - MARGIN_WAIT_FOR_TOUCH;
        float boxWidthSize =  waitForTouchSprite.getWidth() + MARGIN_WAIT_FOR_TOUCH;

        fastForwardSprite.setWidth(WAIT_FOR_TOUCH_WIDTH);
        fastForwardSprite.setHeight(WAIT_FOR_TOUCH_HEIGHT);
        fastForwardSprite.setPosition(0,0);

        waitForTouchSprite.setWidth(WAIT_FOR_TOUCH_WIDTH);
        waitForTouchSprite.setHeight(WAIT_FOR_TOUCH_HEIGHT);
        waitForTouchSprite.setX(touchX + MARGIN_WAIT_FOR_TOUCH);
        waitForTouchSprite.setY(MARGIN_WAIT_FOR_TOUCH);


        Rectangle rectangle = new Rectangle(touchX, 0, boxWidthSize, WAIT_FOR_TOUCH_HEIGHT, vertexBufferObjectManager);
        rectangle.setColor(Color.BLACK);
        rectangle.setAlpha(0.8f);

        Rectangle backgroundRectangle = new Rectangle(0, 0, canvasWidth, WAIT_FOR_TOUCH_HEIGHT, vertexBufferObjectManager);
        backgroundRectangle.setColor(Color.BLACK);
        backgroundRectangle.setAlpha(0.3f);

        Text waitForTapText = new Text(0,0, fontDictionary.get("touch_indicator"),"Tap anywhere to continue ...", vertexBufferObjectManager);
        waitForTapText.setX(backgroundRectangle.getWidth()/2 - waitForTapText.getWidth()/2);
        waitForTapText.setY(backgroundRectangle.getHeight()/2 - waitForTapText.getHeight()/2);
        waitForTapText.setColor(Color.WHITE);

        waitFoTapContainer.attachChild(backgroundRectangle);
        backgroundRectangle.attachChild(fastForwardSprite);
        backgroundRectangle.attachChild(waitForTapText);


        waitFoTapContainer.attachChild(rectangle);
        waitFoTapContainer.attachChild(waitForTouchSprite);
        waitFoTapContainer.setVisible(false);
        waitFoTapContainer.mark();
        mScene.attachChild(waitFoTapContainer);
    }

    private void processOpSequence() {
        processOpSequence(false);
    }

    private void processOpSequence(boolean fastForward) {
        try {

            if (stack.size() == 0) return;

            Operation op = null;
            CurrentBlock currentBlock = stack.getLast();

            while ((op = currentBlock.getCurrentOperation()) != null) {
                Log.d(TAG, "process op " + op.opCode);
                if (op.opCode.equals("hide_grid")) {
                    matrix.getMainGrid().setVisible(false);
                } else if (op.opCode.equals("show_grid")) {
                    matrix.getMainGrid().setVisible(true);
                } else if (op.opCode.equals("disable_grid")) {

                    if (op.opDetails.getBoolean("value") == true) {
                        matrix.disableMoves();
                    } else {
                        matrix.enableMoves();
                    }

                } else if (op.opCode.equals("show_text")) {
//                    final SoundWrapper typeSound = Utils.getInstance().getSound("typing");

//                    typeSound.play();
                    processOpShowText(op, new OnTextDisplayedListener() {
                        @Override
                        public void onComplete() {

                        }

                        @Override
                        public void onSequenceComplete() {
//                            typeSound.stop();
                        }
                    });
                } else if (!fastForward && op.opCode.equals("babble")) {
                    final CurrentBlock finalCurrentBlock1 = currentBlock;
//                    final SoundWrapper typeSound = Utils.getInstance().getSound("typing");
//                    typeSound.play();
                    processOpShowText(op, new OnTextDisplayedListener() {
                        @Override
                        public void onComplete() {

                        }

                        @Override
                        public void onSequenceComplete() {
                            processOpWaitForTouch(finalCurrentBlock1);
//                            typeSound.stop();
                            yourMoveContainer.hide();
                            waitFoTapContainer.show();
                        }
                    });

                    return;
                } else if (!fastForward && op.opCode.equals("wait_for_touch")) {
                    processOpWaitForTouch(currentBlock);
                    yourMoveContainer.hide();
                    waitFoTapContainer.show();
                    return;
                } else if (op.opCode.equals("update_queue")) {
                    JSONArray queueValue = op.opDetails.getJSONArray("value");
                    matrix.getBlockQueue().clear();
                    for (int i = 0; i < queueValue.length(); i++) {
                        NextObject nextObject = new NextObject();
                        nextObject.setTileType(queueValue.getInt(i));
                        matrix.getBlockQueue().add(nextObject);
                    }
                    matrix.drawWorld();
                } else if (op.opCode.equals("map")) {
                    JSONArray boardArray = op.opDetails.getJSONArray("map");
                    for (int i = 0; i < boardArray.length(); i++) {
                        JSONArray rowArray = boardArray.getJSONArray(i);
                        for (int i2 = 0; i2 < rowArray.length(); i2++) {
                            int tileType = rowArray.getInt(i2);
                            GridSquare square = matrix.getMainGrid().getSquareAt(i2, i);
                            square.reset();
                            square.setTileType(tileType);
                        }
                    }
                    matrix.drawWorld();
                } else if (op.opCode.equals("wait_for_valid_move")) {
                    matrix.enableMoves();
                    waitForValidMove = true;
                    waitForTouchOperation = false;
                    currentBlock.increment();
                    //Register event handling
                    if (op.opDetails != null) {
                        JSONArray events = op.opDetails.optJSONArray("events");
                        if (events != null) {
                            for (int i = 0; i < events.length(); i++) {
                                JSONObject event = events.getJSONObject(i);
                                Iterator itr = event.keys();
                                while (itr.hasNext()) {
                                    String key = (String) itr.next();
                                    final ArrayList<Operation> operationArrayList = new ArrayList<Operation>();
                                    getOperationsFromJsonArray(operationArrayList, event.getJSONArray(key));
                                    final CurrentBlock finalCurrentBlock = currentBlock;
                                    matrix.registerEventCallback(key, new GridEventCallback() {
                                        @Override
                                        public void onEventTriggered(String key) {
                                            yourMoveContainer.hide();
                                            CurrentBlock newBlock = new CurrentBlock(finalCurrentBlock);
                                            newBlock.setTransient(true);
                                            newBlock.setOperations(operationArrayList);
                                            stack.add(newBlock);
                                            processOpSequence();
                                        }
                                    });
                                }
                            }

                        }
                    }
                    Log.d(TAG, "Show your move");
                    yourMoveContainer.show();
                    return;
                } else if (op.opCode.equals("unlock_next_exit")) {
                    showLevelChooser(context, new OnLevelMenuShownListener() {
                        @Override
                        public void onComplete(LevelMenu levelMenu) {
                            Log.d(TAG, "onComplete called for unlock and exit");
                            levelMenu.animateLevelUnlock(currentLevel.getNextLevel(), new IEntityModifier.IEntityModifierListener() {
                                @Override
                                public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
                                }

                                @Override
                                public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                                    Utils.setLocked(context, currentLevel.getNextLevel(), false);
                                }
                            });
                        }
                    });
                    return;
                } else if (op.opCode.equals("unlock_next")) {
                    Utils.setLocked(context, currentLevel.getNextLevel(), false);
                } else if (op.opCode.equals("exit")) {
                    showLevelChooser(context, null);
                    return;
                } else if (op.opCode.equals("restore_state")) {
                    String stateName = op.opDetails.getString("name");
                    if (this.savedStates.containsKey(stateName)) {
                        LinkedList<CurrentBlock> newStack = savedStates.get(stateName).getStack();
                        //overwrite the current stack with this one
                        stack = newStack;
                        currentBlock = stack.getLast();
                        currentBlock.opIndex++;
                        Log.d(TAG, "restoring world data");
                        matrix.restoreWorldState(savedStates.get(stateName).getSavedGridBundle());
                        continue;
                    } else {
                        Log.d(TAG, "unknown saved state " + stateName);
                    }
                } else if (op.opCode.equals("save_state")) {
                    SavedGameState saveState = new SavedGameState();
                    saveState.setSavedGridBundle(matrix.saveWorldState());
                    LinkedList<CurrentBlock> savedStack = new LinkedList<CurrentBlock>();
                    for (CurrentBlock b : stack) {
                        savedStack.add(b.clone());
                    }
                    saveState.setStack(savedStack);
                    String stateName = op.opDetails.getString("name");
                    this.savedStates.put(stateName, saveState);
                } else if (op.opCode.equals("if")) {
                    JSONObject testFunctions = op.opDetails.getJSONObject("test");
                    Iterator itr = testFunctions.keys();
                    boolean passed = true;
                    while (itr.hasNext()) {
                        String key = (String) itr.next();
                        if (key.equals("current_level")) {
                           int level = matrix.getCurrentLevel();
                           if (level!=testFunctions.getInt(key)) {
                               passed= false;
                           }
                        }

                        if (key.equals("queue_empty")) {
                            boolean result = matrix.getBlockQueue().isEmpty();
                            if (result != testFunctions.getBoolean(key)) {
                                passed = false;
                            }
                        }

                        if (key.equals("all_color")) {
                            int testColor = testFunctions.getInt(key);
                            if (!matrix.testColor(testColor)) {
                                passed = false;
                            }
                        }

                        if (key.equals("min_color")) {
                            JSONObject getOptions = testFunctions.getJSONObject(key);
                            int color = getOptions.getInt("color");
                            int min = getOptions.getInt("min");
                            if (!matrix.testColor(color, min)) {
                                passed = false;
                            }
                        }
                    }
                    if (passed) {
                        Log.d(TAG, "if operation passed");
                        ArrayList<Operation> blockOperations = getOperations(op.opDetails);
                        currentBlock = spawnNewBlock(currentBlock, blockOperations);
                        continue;
                    } else {
                        if (op.opDetails.has("else")) {
                            ArrayList<Operation> blockOperations = getOperations(op.opDetails.getJSONArray("else"));
                            currentBlock = spawnNewBlock(currentBlock, blockOperations);
                            continue;
                        }
                    }
                } else if (op.opCode.equals("repeat_block")) {
                    while (currentBlock.isTransient()) {

                        currentBlock = currentBlock.getParent();
                    }
                    currentBlock.opIndex = 0; //reset block
                    continue;
                } else if (op.opCode.equals("block")) {
                    Log.d(TAG, "inside block");
                    ArrayList<Operation> blockOperations = getOperations(op.opDetails);
                    currentBlock = spawnNewBlock(currentBlock, blockOperations);
                    continue;
                }
                currentBlock.increment();
            }
            stack.removeLast();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void processOpWaitForTouch(CurrentBlock currentBlock) {
        waitForTouchOperation = true;
        Log.d(TAG, "waiting for touch");
        currentBlock.increment();
    }

    private void processOpShowText(Operation op, OnTextDisplayedListener listener) throws JSONException {
        JSONObject messageDetails = op.opDetails.getJSONObject("value");
        final Object message = messageDetails.get("message");
        int messageDelay = messageDetails.optInt("delay", 100);
        waitFoTapContainer.hide();
        if (!conversationText.isEmpty()) {
            for (final RectangleButton conversation : conversationText) {
                context.runOnUpdateThread(new Runnable() {
                    @Override
                    public void run() {
                    /* Now it is save to remove the entity! */
                        conversation.detachSelf();
                    }
                });
            }
            conversationText.clear();
            processText(message, 0, messageDelay, listener);
        } else {
            processText(message, 0, messageDelay, listener);
        }

        if (message instanceof String) {
            if (listener!=null) {
                listener.onSequenceComplete();
            }
        }
    }

    private CurrentBlock spawnNewBlock(CurrentBlock currentBlock, ArrayList<Operation> blockOperations) {
        CurrentBlock newBlock = new CurrentBlock(currentBlock);
        newBlock.setOperations(blockOperations);
        stack.add(newBlock);
        currentBlock.increment();
        currentBlock = newBlock;
        return currentBlock;
    }

    private void processText(Object messageObj, final int position, final int messageDelay, final OnTextDisplayedListener listener) {
        if (messageObj instanceof String) {
            String message = (String) messageObj;
            RectangleButton textBox = new RectangleButton(0, 0, 0, 0, vertexBufferObjectManager,
                    Utils.getInstance().getFont("story_text"), message);

            textBox.setColor(ColorConstants.BLUE);
            textBox.setTextColor(Color.WHITE);
            textBox.autoWidth(ObjectDimensions.szStoryTextMargins);

            textBox.setAlpha(0f);
            emptyBoundedEntity.attachChild(textBox);
            textBox.centerInParent(BoundedEntity.CENTER_HORIZONTAL);
            textBox.setY(680);
            for (RectangleButton previousConversation : conversationText) {
                previousConversation.setY(previousConversation.getY() - (textBox.getHeight() + ObjectDimensions.szStoryTextMargins));
            }

            textBox.registerEntityModifier(new AlphaModifier(1f, 0f, 1f, new IEntityModifier.IEntityModifierListener() {
                @Override
                public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {

                }

                @Override
                public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                    if (listener != null) {
                        Timer t = new Timer();
                        t.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                listener.onComplete();
                            }
                        }, messageDelay);

                    }
                }
            }));
            conversationText.add(textBox);
        } else if (messageObj instanceof JSONArray) {
            final JSONArray messageGroup = (JSONArray) messageObj;
            try {
                if (position < messageGroup.length()) {
                    String message = messageGroup.getString(position);
                    processText(message, position, messageDelay, new OnTextDisplayedListener() {
                        @Override
                        public void onComplete() {
                            processText(messageGroup, position + 1, messageDelay, listener);
                            if (listener!=null) {
                                if (position + 1 >= messageGroup.length()) {
                                    listener.onSequenceComplete();
                                }
                            }
                        }

                        @Override
                        public void onSequenceComplete() {
                            Log.d(TAG,"Squence complete.");
                            listener.onSequenceComplete();
                        }
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public Level loadLevel() {
        return loadLevel(FIRST_LEVEL);
    }

    public Episode loadLevels(int episodeIndex) {
        Episode episode = new Episode();

        ArrayList<LevelInfo> result = new ArrayList<LevelInfo>();
        InputStream is = null;
        try {
            is = context.getAssets().open("levels/levels.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder levelsString = new StringBuilder();

            while (reader.ready()) {
                levelsString.append(reader.readLine());
            }
            reader.close();
            is.close();

            try {
                JSONObject episodesObject = new JSONObject(levelsString.toString());
                JSONArray episodes = episodesObject.getJSONArray("episodes");
                JSONObject jsonObject = episodes.getJSONObject(episodeIndex);
                String episodeName = jsonObject.getString("name");
                episode.setName(episodeName);
                JSONArray levelObjects = jsonObject.getJSONArray("levels");
                for (int i = 0; i < levelObjects.length(); i++) {
                    JSONObject obj = levelObjects.getJSONObject(i);

                    LevelInfo l = new LevelInfo();
                    l.setId(obj.getInt("id"));
                    if (l.getId() != 1) {
                        if (Utils.isLocked(context, l.getId())) {
                            l.setLocked(true);
                        } else {
                            l.setLocked(false);
                        }
                    }
                    l.setTitle(obj.getString("title"));
                    if (!obj.has("coming_soon")) {
                        l.setFilePath(obj.getString("file"));
                        l.setNextLevel(obj.getString("next"));
                        l.setComingSoon(false);
                    } else {
                        l.setIsComingSoon(true);
                    }
                    Log.d(TAG, "Adding level.");
                    levelHashMap.put(l.getId(), l);
                    result.add(l);
                }
                episode.setLevels(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return episode;
    }

    public Level loadLevel(String levelName) {


        try {
            InputStream is = context.getAssets().open("levels/" + levelName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder levelString = new StringBuilder();

            while (reader.ready()) {
                levelString.append(reader.readLine());
            }
            reader.close();
            is.close();

            Level level = parseLevel(levelString.toString());

            return level;
        } catch (IOException e) {
            e.printStackTrace();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Level parseLevel(String levelString) throws JSONException {
        Level level = new Level();
        JSONObject jsonObject = new JSONObject(levelString);
        level.setName(jsonObject.getString("title"));
        level.setSubName(jsonObject.optString("sub_title",""));
        level.setId(jsonObject.getInt("id"));
        level.setNextLevel(jsonObject.getInt("next_level"));
        level.setGridWidth(jsonObject.getInt("grid_width"));
        level.setGridHeight(jsonObject.getInt("grid_height"));

        if (jsonObject.has("options")) {
            JSONObject options = jsonObject.getJSONObject("options");
            level.setRechargeMeter(options.optBoolean("recharge_meter", true));
            level.setRechargeMeterInitial(options.optInt("recharge_meter_initial", 0));
            level.setUseQueue(options.optBoolean("queue", true));
            JSONArray tileSets = options.optJSONArray("queue_allowed_tiles");

            if (tileSets!=null) {
                ArrayList<TileSet> objectSets = new ArrayList<TileSet>();
                for (int i = 0; i < tileSets.length(); i++) {
                    JSONObject set = tileSets.getJSONObject(i);
                    ArrayList<Integer> allowedObjects = new ArrayList<Integer>();
                    JSONArray setArray = set.getJSONArray("set");
                    for (int i2 = 0; i2 < setArray.length(); i2++) {
                        allowedObjects.add(setArray.getInt(i2));
                    }
                    TileSet tileSet = new TileSet();
                    tileSet.setAllowedObjects(allowedObjects);
                    tileSet.setWeight(set.getDouble("w"));
                    objectSets.add(tileSet);
                }
                level.setAllowedQueueObjects(objectSets);
            }

            level.setScoreVisible(options.optBoolean("scores", false));
        }

        if (jsonObject.has("map")) {
            NextObject map[][] = new NextObject[level.getGridWidth()][level.getGridHeight()];
            JSONArray rows = jsonObject.getJSONArray("map");
            for (int i = 0; i < rows.length(); i++) {
                JSONArray row = rows.getJSONArray(i);
                for (int i2 = 0; i2 < row.length(); i2++) {
                    Object currentCell = row.get(i2);
                    NextObject object = new NextObject();
                    if (currentCell instanceof JSONObject) {

                        JSONObject currentCellJSONObject = (JSONObject) currentCell;

                        object.setTileType(currentCellJSONObject.getInt("t"));
                        if (currentCellJSONObject.has("age")) {
                            object.setAge(currentCellJSONObject.getInt("age"));
                        }
                        map[i2][i] = object;
                    } else {
                        int cell = row.getInt(i2);
                        object.setTileType(cell);
                        map[i2][i] = object;
                    }
                }
            }
            level.setMap(map);
        }

        if (jsonObject.has("queue")) {
            JSONArray queueMap = jsonObject.getJSONArray("queue");
            int queue[] = new int[queueMap.length()];
            for (int i = 0; i < queue.length; i++) {
                queue[i] = queueMap.getInt(i);
            }
            level.setQueue(queue);
        }

        ArrayList<Operation> operationArrayList = getOperations(jsonObject);
        level.setOperations(operationArrayList);
        return level;
    }

    private ArrayList<Operation> getOperations(JSONObject jsonObject) throws JSONException {
        ArrayList<Operation> operationArrayList = new ArrayList<Operation>();
        if (jsonObject.has("sequence")) {
            JSONArray operations = jsonObject.getJSONArray("sequence");

            getOperationsFromJsonArray(operationArrayList, operations);

        }
        return operationArrayList;
    }

    private ArrayList<Operation> getOperations(JSONArray jsonObject) throws JSONException {
        ArrayList<Operation> operationArrayList = new ArrayList<Operation>();
        getOperationsFromJsonArray(operationArrayList, jsonObject);
        return operationArrayList;
    }


    private void getOperationsFromJsonArray(ArrayList<Operation> operationArrayList, JSONArray operations) throws JSONException {
        for (int i = 0; i < operations.length(); i++) {
            Operation op = new Operation();
            JSONObject opDetails = operations.getJSONObject(i);
            op.opCode = opDetails.optString("op");
            if (op.opCode != null) {
                op.opDetails = opDetails.optJSONObject("details");
                operationArrayList.add(op);
            }
        }
    }

    @Override
    public void onScreenCaptureHighScore(GameOver gameOverText, ScreenCapture screenCapture) {

    }

    @Override
    public void onExitGrid(MenuItem item) {
        showLevelChooser(context, null);
    }

    @Override
    public void onSetupWorld(MainGrid mainGrid) {
        NextObject map[][] = currentLevel.getMap();
        if (map != null) {
            for (int i = 0; i < currentLevel.getGridWidth(); i++) {
                for (int i2 = 0; i2 < currentLevel.getGridHeight(); i2++) {
                    GridSquare gridSquare = mainGrid.getSquareAt(i, i2);
                    gridSquare.reset();
                    gridSquare.setTileType(map[i][i2].getTileType());
                    gridSquare.setAge(map[i][i2].getAge());
                    gridSquare.updateSelf();
                }
            }
        }
    }

    @Override
    public void populateQueue(Vector<NextObject> blockQueue) {
        int[] queue = currentLevel.getQueue();
        for (int i = 0; i < queue.length; i++) {
            NextObject nextObject = new NextObject();
            nextObject.setTileType(queue[i]);
            blockQueue.add(nextObject);
        }
    }

    @Override
    public void onRestart(MenuItem item) {
        stack.clear();
        waitForTouchOperation = false;
        waitForValidMove = false;
        renderLevel(currentLevel, context);
    }

    @Override
    public void onGameOver() {
        mainMenu.setVisible(true);
    }

    public int getOpIndex() {
        return stack.getLast().opIndex;
    }

    public void setOpIndex(int opIndex) {
        this.opIndex = opIndex;
    }

    public boolean isLevelMenuShown() {
        return levelMenu.isVisible();
    }

    public void hideLevelMenu() {
        levelMenu.setVisible(false);
    }

    public void showLevelChooser(final MainActivity mainActivity, OnLevelMenuShownListener listener) {
        mScene.detachChildren();
        mScene.attachChild(levelMenu);
        levelMenu.setVisible(true);
        levelMenu.setEpisodeName(loadLevels(0).getName());
        levelMenu.setLevelInfos(loadLevels(0).getLevels());
        levelMenu.updateSelf();
        levelMenu.setListener(new OnLevelSelectedListener() {
            @Override
            public void onLevelSelected(int tag) {
                Level level = loadLevel(levelHashMap.get(tag).getFilePath());
                renderLevel(level, mainActivity);
            }
        });
        if (listener!=null) {
            listener.onComplete(levelMenu);
        }
        stopMusic();
    }

    @Override
    public void show(Scene scene) {
        showLevelChooser(context, null);
    }

    @Override
    public void hide() {

    }

    @Override
    public void onResumeGame() {

    }

    @Override
    public void onPauseGame() {

    }

    @Override
    public boolean onBackPressed() {
        if (levelMenu.isVisible()) {
            return false;
        } else {
            if (!waitForTouchOperation) {
                if (mainMenu.isVisible()) {
                    mainMenu.animateHide();
                } else {
                    mainMenu.animateShow();
                }
            }
        }
        return true;
    }
}
